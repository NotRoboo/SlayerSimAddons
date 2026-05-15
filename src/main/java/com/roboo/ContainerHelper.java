package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class ContainerHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean waitingForSlayer = false;
    private static boolean waitingForWarp   = false;
    private static boolean modTriggeredWarp = false;

    private static long pendingWarpTime = 0;
    private static final long COMMAND_DELAY_MS = 1000;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

        ClientReceiveMessageEvents.GAME.register((msg, overlay) ->
                handleMessage(msg.getString()));

        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) ->
                handleMessage(msg.getString()));
    }

    private static void onTick() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();

        if (waitingForSlayer && ModConfig.isAutoReconnectEnabled()) {
            String title = getContainerTitle();
            if (title != null && title.contains("xSublimity's Houses")) {
                var menu = mc.player.containerMenu;
                if (menu != null) {
                    for (int i = 0; i < menu.slots.size(); i++) {
                        ItemStack stack = menu.slots.get(i).getItem();
                        if (stack.isEmpty()) continue;
                        if (stack.getHoverName().getString().contains("ＳＬＡＹＥＲ ＳＩＭＵＬＡＴＯＲ")) {
                            clickSlot(i, ClickType.PICKUP, 0);
                            waitingForSlayer = false;
                            return;
                        }
                    }
                }
            }
        }

        if (!ModConfig.isCrescentTowerEnabled() && !ModConfig.isVolcanoEnabled()) return;

        if (pendingWarpTime > 0 && now >= pendingWarpTime) {
            pendingWarpTime = 0;
            modTriggeredWarp = true;
            runCommand("warp");
        }

        if (waitingForWarp && modTriggeredWarp) {
            String title = getContainerTitle();
            if (title != null && title.contains("Ghast Travel")) {
                var menu = mc.player.containerMenu;
                if (menu != null) {
                    String destination = getDestination();
                    if (destination == null) {
                        waitingForWarp = false;
                        modTriggeredWarp = false;
                        return;
                    }

                    for (int i = 0; i < menu.slots.size(); i++) {
                        ItemStack stack = menu.slots.get(i).getItem();
                        if (stack.isEmpty()) continue;
                        if (stack.getHoverName().getString().contains(destination)) {
                            clickSlot(i, ClickType.PICKUP, 0);
                            waitingForWarp = false;
                            modTriggeredWarp = false;

                            if (destination.equals("Crescent Tower")) {
                                CrescentTowerHelper.trigger();
                            }

                            return;
                        }
                    }
                }
            }
        }
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        if (!ModConfig.isCrescentTowerEnabled() && !ModConfig.isVolcanoEnabled()) return;

        String clean = msg.toLowerCase(Locale.ROOT);

        if (clean.contains("sending you to ｓｌａｙｅｒ ｓｉｍｕｌａｔｏｒ")) {
            pendingWarpTime = System.currentTimeMillis() + COMMAND_DELAY_MS;
            waitingForWarp = true;
        }
    }

    private static String getDestination() {
        if (ModConfig.isCrescentTowerEnabled()) return "Crescent Tower";
        if (ModConfig.isVolcanoEnabled()) return "Ancient Volcano";
        return null;
    }

    public static void waitForSlayerMenu() {
        waitingForSlayer = true;
    }

    public static void reset() {
        waitingForSlayer   = false;
        waitingForWarp     = false;
        modTriggeredWarp   = false;
        pendingWarpTime    = 0;
    }

    private static String getContainerTitle() {
        if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
            return screen.getTitle().getString();
        }
        return null;
    }

    private static void clickSlot(int slotId, ClickType actionType, int button) {
        if (mc.player == null || mc.gameMode == null) return;
        var handler = mc.player.containerMenu;
        if (handler == null) return;
        mc.gameMode.handleInventoryMouseClick(
                handler.containerId,
                slotId,
                button,
                actionType,
                mc.player
        );
    }

    private static void runCommand(String command) {
        if (mc.player == null) return;
        mc.player.connection.sendCommand(command);
    }
}