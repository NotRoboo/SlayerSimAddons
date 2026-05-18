package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;

import java.util.ArrayDeque;
import java.util.Deque;

public class AutoStoreHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean enabled = false;

    public enum State { IDLE, WAITING_TO_OPEN, OPENING, DRAINING, RESCANNING }
    private static State state = State.IDLE;

    static final Deque<Integer> clickQueue = new ArrayDeque<>();
    static final long[] nextCycleTime = { 0 };

    private static int tickCooldown = 0;
    private static boolean savedToggleUse = false;

    private static String lastContainerTitle = "";
    private static long lastContainerTime = 0;
    private static final long DUPLICATE_WINDOW_MS = 5000;

    public static State getState() { return state; }

    public static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> disableAndCleanup());

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!enabled) return;
            if (!(screen instanceof AbstractContainerScreen<?> cs)) return;

            String title = cs.getTitle().getString();
            if (title.contains("Material Bag")) return;

            ScreenEvents.remove(screen).register(s -> {
                long now = System.currentTimeMillis();
                if (title.equals(lastContainerTitle) && (now - lastContainerTime) < DUPLICATE_WINDOW_MS) {
                    disableAndCleanup();
                    msg("§cDisabled: same container opened twice within 5s");
                    return;
                }
                lastContainerTitle = title;
                lastContainerTime = now;
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    public static void toggle() {
        if (!enabled && ASInventoryHelper.findMaterialBagSlot() == -1) {
            msg("§cNo Material Bag found in hotbar!");
            return;
        }
        enabled = !enabled;
        msg(enabled ? "§aON" : "§cOFF");
        if (enabled) {
            savedToggleUse = mc.options.toggleUse().get();
            if (savedToggleUse) {
                mc.options.toggleUse().set(false);
                mc.options.save();
            }
            nextCycleTime[0] = System.currentTimeMillis() + (ModConfig.getAutoStoreDelay() * 1000L);
            InputHelper.holdRightClick(true);
        } else {
            restoreToggleUse();
            InputHelper.stopAll();
            state = State.IDLE;
            clickQueue.clear();
            tickCooldown = 0;
        }
    }

    private static void onTick() {
        if (mc.player == null) return;

        if (enabled && state == State.IDLE && ASInventoryHelper.findMaterialBagSlot() == -1) {
            disableAndCleanup();
            msg("§cDisabled: Material Bag not found in hotbar");
            return;
        }

        if (enabled && state == State.IDLE && mc.screen == null) {
            InputHelper.holdRightClick(true);
        }

        if (!enabled) return;

        if (tickCooldown > 0) {
            tickCooldown--;
            return;
        }

        switch (state) {
            case IDLE -> {
                if (System.currentTimeMillis() >= nextCycleTime[0]) {
                    InputHelper.holdRightClick(false);
                    state = State.WAITING_TO_OPEN;
                    tickCooldown = 20;
                }
            }
            case WAITING_TO_OPEN -> startCycle();
            case OPENING         -> tryFinishOpen();
            case DRAINING        -> drainOneTick();
            case RESCANNING      -> rescanAndContinue();
        }
    }

    private static void startCycle() {
        int bagSlot = ASInventoryHelper.findMaterialBagSlot();
        if (bagSlot == -1) {
            InputHelper.holdRightClick(true);
            nextCycleTime[0] = System.currentTimeMillis() + 5000;
            state = State.IDLE;
            return;
        }

        ASInventoryHelper.cacheCurrentSlot();
        ASInventoryHelper.selectSlot(bagSlot);
        ASInventoryHelper.useItem();

        clickQueue.clear();
        state = State.OPENING;
        tickCooldown = 6;
    }

    private static void tryFinishOpen() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            tickCooldown = 2;
            return;
        }

        if (!ensureDepositMode()) { tickCooldown = 6; return; }
        if (!ensureAmount64())    { tickCooldown = 6; return; }

        buildClickQueue();

        if (clickQueue.isEmpty()) {
            if (mc.player != null) mc.player.closeContainer();
            finishCycle();
        } else {
            state = State.DRAINING;
            tickCooldown = 1;
        }
    }

    private static boolean ensureDepositMode() {
        if (mc.player == null) return true;
        var handler = mc.player.containerMenu;
        for (int i = 0; i < handler.slots.size(); i++) {
            var stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || !stack.getHoverName().getString().contains("Material Bag Mode")) continue;
            var lore = stack.get(DataComponents.LORE);
            if (lore == null) continue;
            for (var line : lore.lines()) {
                String text = line.getString();
                if (text.contains("Current Mode: Deposit")) return true;
                if (text.contains("Current Mode: Withdraw")) {
                    if (mc.gameMode != null)
                        mc.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.PICKUP, mc.player);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean ensureAmount64() {
        if (mc.player == null) return true;
        var handler = mc.player.containerMenu;
        for (int i = 0; i < handler.slots.size(); i++) {
            var stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || !stack.getHoverName().getString().contains("Set Amount")) continue;
            var lore = stack.get(DataComponents.LORE);
            if (lore == null) continue;
            for (var line : lore.lines()) {
                String text = line.getString();
                if (text.contains("Current amount: 64")) return true;
                if (text.contains("Current amount: 1") || text.contains("Current amount: 8")) {
                    if (mc.gameMode != null)
                        mc.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.PICKUP, mc.player);
                    return false;
                }
            }
        }
        return true;
    }

    private static void buildClickQueue() {
        clickQueue.clear();
        if (mc.player == null) return;
        var inv = mc.player.getInventory();
        var handler = mc.player.containerMenu;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            int fullStacks = stack.getCount() / 64;
            if (fullStacks == 0) continue;
            int guiSlot = findGuiSlotByName(stack.getHoverName().getString(), handler);
            if (guiSlot == -1) continue;
            for (int s = 0; s < fullStacks; s++) clickQueue.add(guiSlot);
        }
    }

    private static void drainOneTick() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            finishCycle();
            return;
        }
        if (clickQueue.isEmpty()) {
            state = State.RESCANNING;
            tickCooldown = 6;
            return;
        }
        if (mc.player != null && mc.gameMode != null) {
            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId, clickQueue.poll(), 0, ClickType.PICKUP, mc.player
            );
        }
        tickCooldown = 5 + mc.player.getRandom().nextInt(4);
    }

    private static void rescanAndContinue() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            finishCycle();
            return;
        }
        buildClickQueue();
        if (clickQueue.isEmpty()) {
            if (mc.player != null) mc.player.closeContainer();
            finishCycle();
        } else {
            state = State.DRAINING;
            tickCooldown = 1;
        }
    }

    private static int findGuiSlotByName(String itemName, net.minecraft.world.inventory.AbstractContainerMenu handler) {
        int bagSlotCount = handler.slots.size() - 36;
        for (int i = 0; i < bagSlotCount; i++) {
            var stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getHoverName().getString().equals(itemName)) return i;
        }
        return -1;
    }

    private static void finishCycle() {
        state = State.IDLE;
        clickQueue.clear();
        tickCooldown = 0;
        ASInventoryHelper.restoreSlot();
        nextCycleTime[0] = System.currentTimeMillis() + (ModConfig.getAutoStoreDelay() * 1000L);
        InputHelper.holdRightClick(true);
    }

    private static void restoreToggleUse() {
        if (savedToggleUse) {
            mc.options.toggleUse().set(true);
            mc.options.save();
            savedToggleUse = false;
        }
    }

    public static void disableAndCleanup() {
        enabled = false;
        restoreToggleUse();
        InputHelper.stopAll();
        state = State.IDLE;
        clickQueue.clear();
        tickCooldown = 0;
    }

    static void msg(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("§e[AutoStore] " + text), false);
    }
}