package com.roboo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayDeque;
import java.util.Deque;

public class EmptyInvHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int FIRST_PAGE  = 5;
    private static final int LAST_PAGE   = 15;

    private enum Phase {
        IDLE,
        OPENING,
        NAVIGATING_TO_START,
        ENSURE_DEPOSIT_MODE,
        ENSURE_AMOUNT,
        SCAN_AND_QUEUE,
        DRAINING,
        RESCANNING,
        NEXT_PAGE,
        CLOSING
    }

    private enum AmountPass { PASS_64, PASS_8, PASS_1, NONE }

    private static Phase     phase       = Phase.IDLE;
    private static AmountPass amountPass = AmountPass.PASS_64;
    private static int       currentPage = -1;
    private static int       tickCooldown = 0;
    private static boolean   needs64     = false;
    private static boolean   needs8      = false;
    private static boolean   needs1      = false;

    private static final Deque<Integer> clickQueue = new ArrayDeque<>();

    public static boolean isRunning() { return phase != Phase.IDLE; }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    public static void start() {
        if (phase != Phase.IDLE) {
            msg("§cAlready running!");
            return;
        }
        int bagSlot = ASInventoryHelper.findMaterialBagSlot();
        if (bagSlot == -1) {
            msg("§cNo Material Bag found in hotbar!");
            return;
        }
        msg("§aStarting to empty (pages " + FIRST_PAGE + "-" + LAST_PAGE + ")...");
        clickQueue.clear();
        currentPage = -1;
        amountPass  = AmountPass.PASS_64;
        phase       = Phase.OPENING;
        tickCooldown = 0;

        ASInventoryHelper.cacheCurrentSlot();
        ASInventoryHelper.selectSlot(bagSlot);
        ASInventoryHelper.useItem();
        tickCooldown = 8;
    }

    public static void stop() {
        clickQueue.clear();
        phase       = Phase.IDLE;
        tickCooldown = 0;
        ASInventoryHelper.restoreSlot();
        if (mc.player != null && mc.screen instanceof AbstractContainerScreen<?>) {
            mc.player.closeContainer();
        }
    }

    private static void onTick() {
        if (phase == Phase.IDLE) return;
        if (mc.player == null) { stop(); return; }

        if (tickCooldown > 0) { tickCooldown--; return; }

        switch (phase) {
            case OPENING              -> handleOpening();
            case NAVIGATING_TO_START  -> handleNavigateToStart();
            case ENSURE_DEPOSIT_MODE  -> handleEnsureDepositMode();
            case ENSURE_AMOUNT        -> handleEnsureAmount();
            case SCAN_AND_QUEUE       -> handleScanAndQueue();
            case DRAINING             -> handleDraining();
            case RESCANNING           -> handleRescanning();
            case NEXT_PAGE            -> handleNextPage();
            case CLOSING              -> handleClosing();
            default                   -> {}
        }
    }

    private static void handleOpening() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            tickCooldown = 3;
            return;
        }
        currentPage = readCurrentPage();
        if (currentPage == -1) {
            tickCooldown = 3;
            return;
        }
        phase = Phase.NAVIGATING_TO_START;
    }

    private static void handleNavigateToStart() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed during navigation.");
            return;
        }
        currentPage = readCurrentPage();
        if (currentPage == -1) { tickCooldown = 3; return; }

        if (currentPage == FIRST_PAGE) {
            amountPass = AmountPass.PASS_64;
            phase = Phase.ENSURE_DEPOSIT_MODE;
            tickCooldown = 4;
            return;
        }

        if (currentPage > FIRST_PAGE) {
            int prevSlot = findSlotByName("Previous Page");
            if (prevSlot == -1) { tickCooldown = 4; return; }
            clickSlot(prevSlot);
            tickCooldown = 6;
        } else {
            int nextSlot = findSlotByName("Next Page");
            if (nextSlot == -1) { tickCooldown = 4; return; }
            clickSlot(nextSlot);
            tickCooldown = 6;
        }
    }

    private static void handleEnsureDepositMode() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed.");
            return;
        }
        var handler = mc.player.containerMenu;
        for (int i = 0; i < handler.slots.size(); i++) {
            var stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || !stack.getHoverName().getString().contains("Material Bag Mode")) continue;
            var lore = stack.get(DataComponents.LORE);
            if (lore == null) continue;
            for (var line : lore.lines()) {
                String text = line.getString();
                if (text.contains("Current Mode: Deposit")) {
                    phase = Phase.ENSURE_AMOUNT;
                    tickCooldown = 3;
                    return;
                }
                if (text.contains("Current Mode: Withdraw")) {
                    clickSlot(i);
                    tickCooldown = 6;
                    return;
                }
            }
        }
        phase = Phase.ENSURE_AMOUNT;
        tickCooldown = 3;
    }

    private static void handleEnsureAmount() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed.");
            return;
        }
        String targetAmount = targetAmountString();
        var handler = mc.player.containerMenu;
        for (int i = 0; i < handler.slots.size(); i++) {
            var stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || !stack.getHoverName().getString().contains("Set Amount")) continue;
            var lore = stack.get(DataComponents.LORE);
            if (lore == null) continue;
            for (var line : lore.lines()) {
                String text = line.getString();
                if (text.contains("Current amount: " + targetAmount)) {
                    phase = Phase.SCAN_AND_QUEUE;
                    tickCooldown = 3;
                    return;
                }
                if (text.contains("Current amount:")) {
                    clickSlot(i);
                    tickCooldown = 6;
                    return;
                }
            }
        }
        phase = Phase.SCAN_AND_QUEUE;
        tickCooldown = 3;
    }

    private static void handleScanAndQueue() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed.");
            return;
        }

        if (amountPass == AmountPass.PASS_64) {
            prescanPage();
            if (!needs64 && !needs8 && !needs1) {
                advancePassOrPage();
                return;
            }
            if (!needs64) {
                amountPass = nextNeededPass();
                phase = Phase.ENSURE_AMOUNT;
                tickCooldown = 4;
                return;
            }
        }

        buildClickQueue();
        if (clickQueue.isEmpty()) {
            advancePassOrPage();
        } else {
            phase = Phase.DRAINING;
            tickCooldown = 1;
        }
    }

    private static void handleDraining() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed during drain.");
            return;
        }
        if (clickQueue.isEmpty()) {
            phase = Phase.RESCANNING;
            tickCooldown = 6;
            return;
        }
        clickSlot(clickQueue.poll());
        tickCooldown = 5 + mc.player.getRandom().nextInt(4);
    }

    private static void handleRescanning() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed during rescan.");
            return;
        }
        buildClickQueue();
        if (clickQueue.isEmpty()) {
            advancePassOrPage();
        } else {
            phase = Phase.DRAINING;
            tickCooldown = 1;
        }
    }

    private static void handleNextPage() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            abort("§cBag closed during page turn.");
            return;
        }
        currentPage = readCurrentPage();
        if (currentPage == -1) { tickCooldown = 4; return; }

        amountPass = AmountPass.PASS_64;
        needs64    = false;
        needs8     = false;
        needs1     = false;
        phase = Phase.ENSURE_AMOUNT;
        tickCooldown = 4;
    }

    private static void handleClosing() {
        if (mc.player != null) mc.player.closeContainer();
        ASInventoryHelper.restoreSlot();
        msg("§aDone!");
        phase = Phase.IDLE;
        clickQueue.clear();
        tickCooldown = 0;
    }

    private static void advancePassOrPage() {
        AmountPass next = nextNeededPassAfter(amountPass);
        if (next != AmountPass.NONE) {
            amountPass = next;
            phase = Phase.ENSURE_AMOUNT;
            tickCooldown = 4;
            return;
        }
        if (currentPage >= LAST_PAGE) {
            phase = Phase.CLOSING;
            tickCooldown = 4;
        } else {
            int nextSlot = findSlotByName("Next Page");
            if (nextSlot == -1) {
                abort("§cCould not find Next Page button.");
                return;
            }
            clickSlot(nextSlot);
            currentPage++;
            phase = Phase.NEXT_PAGE;
            tickCooldown = 8;
        }
    }

    private static AmountPass nextNeededPass() {
        if (needs64) return AmountPass.PASS_64;
        if (needs8)  return AmountPass.PASS_8;
        if (needs1)  return AmountPass.PASS_1;
        return AmountPass.NONE;
    }

    private static AmountPass nextNeededPassAfter(AmountPass current) {
        if (current == AmountPass.PASS_64 && needs8)  return AmountPass.PASS_8;
        if (current == AmountPass.PASS_64 && needs1)  return AmountPass.PASS_1;
        if (current == AmountPass.PASS_8  && needs1)  return AmountPass.PASS_1;
        return AmountPass.NONE;
    }

    private static void prescanPage() {
        needs64 = false;
        needs8  = false;
        needs1  = false;
        if (mc.player == null) return;

        var inv     = mc.player.getInventory();
        var handler = mc.player.containerMenu;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            int count = stack.getCount();
            if (count == 0) continue;
            if (findGuiSlotByName(stack.getHoverName().getString(), handler) == -1) continue;
            if (count >= 64) needs64 = true;
            if (count >= 8)  needs8  = true;
            needs1 = true;
        }

        if (needs64) needs8 = needs8 && (hasRemainderAfter64());
    }

    private static boolean hasRemainderAfter64() {
        if (mc.player == null) return false;
        var inv     = mc.player.getInventory();
        var handler = mc.player.containerMenu;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (findGuiSlotByName(stack.getHoverName().getString(), handler) == -1) continue;
            int remainder = stack.getCount() % 64;
            if (remainder >= 8) return true;
        }
        return false;
    }

    private static void buildClickQueue() {
        clickQueue.clear();
        if (mc.player == null) return;

        var inv     = mc.player.getInventory();
        var handler = mc.player.containerMenu;
        int minCount = minCountForPass();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            int count = stack.getCount();
            if (count < minCount) continue;

            int depositable = count / minCount;
            if (depositable == 0) continue;

            int guiSlot = findGuiSlotByName(stack.getHoverName().getString(), handler);
            if (guiSlot == -1) continue;

            for (int s = 0; s < depositable; s++) clickQueue.add(guiSlot);
        }
    }

    private static int minCountForPass() {
        return switch (amountPass) {
            case PASS_64 -> 64;
            case PASS_8  -> 8;
            case PASS_1  -> 1;
            case NONE    -> 1;
        };
    }

    private static String targetAmountString() {
        return switch (amountPass) {
            case PASS_64 -> "64";
            case PASS_8  -> "8";
            case PASS_1  -> "1";
            case NONE    -> "1";
        };
    }

    private static int readCurrentPage() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> cs)) return -1;
        String title = cs.getTitle().getString().replaceAll("§.", "").trim();
        for (int p = LAST_PAGE; p >= 2; p--) {
            if (title.contains("Page " + p)) return p;
        }
        if (title.contains("Material Bag")) return 1;
        return -1;
    }

    private static int findSlotByName(String name) {
        if (mc.player == null) return -1;
        var handler = mc.player.containerMenu;
        int bagSlotCount = handler.slots.size() - 36;
        for (int i = 0; i < bagSlotCount; i++) {
            var stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getHoverName().getString().contains(name)) return i;
        }
        return -1;
    }

    private static int findGuiSlotByName(String itemName, net.minecraft.world.inventory.AbstractContainerMenu handler) {
        int bagSlotCount = handler.slots.size() - 36;
        for (int i = 0; i < bagSlotCount; i++) {
            var stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getHoverName().getString().equals(itemName)) return i;
        }
        return -1;
    }

    private static void clickSlot(int slotId) {
        if (mc.player == null || mc.gameMode == null) return;
        var handler = mc.player.containerMenu;
        if (handler == null) return;
        mc.gameMode.handleInventoryMouseClick(handler.containerId, slotId, 0, ClickType.PICKUP, mc.player);
    }

    private static void abort(String reason) {
        msg(reason);
        stop();
    }

    static void msg(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("§e[EmptyInv] " + text), false);
    }
}