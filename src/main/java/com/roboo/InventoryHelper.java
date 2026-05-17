package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class InventoryHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    public static final String WITHER_TOKEN_NAME = "Lord Token";
    public static final String DRAGON_KEY_NAME   = "Dragon's Nest Key";

    private static int cachedSlot       = -1;
    private static int restoreCountdown = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (restoreCountdown < 0) return;
        if (--restoreCountdown == 0) {
            restoreSlotNow();
        }
    }


    // FIND SLOT BY ITEM NAME (HOTBAR)
    public static int findItemSlot(String itemName) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getHoverName().getString().contains(itemName)) {
                return i;
            }
        }
        return -1;
    }

    public static int findTokenSlot() {
        return findItemSlot(WITHER_TOKEN_NAME);
    }

    public static boolean hasSummonItem() {
        return findTokenSlot() != -1;
    }


    // SELECT SLOT
    public static void selectSlot(int slot) {
        if (mc.player == null || slot < 0 || slot > 8) return;
        mc.player.getInventory().setSelectedSlot(slot);
    }


    // CACHE / RESTORE
    public static void cacheSlot() {
        if (mc.player == null) return;
        cachedSlot = mc.player.getInventory().getSelectedSlot();
    }

    public static void restoreSlot() {
        if (cachedSlot == -1) return;
        restoreCountdown = 10;
    }

    private static void restoreSlotNow() {
        if (mc.player == null || cachedSlot == -1) return;
        mc.player.getInventory().setSelectedSlot(cachedSlot);
        cachedSlot       = -1;
        restoreCountdown = -1;
    }

    public static void restoreSlotAfterSummon() {
        restoreSlotNow();
    }


    // USE ITEM BY NAME
    public static boolean useItem(String itemName) {
        if (mc.player == null || mc.gameMode == null) return false;

        int slot = findItemSlot(itemName);
        if (slot == -1) return false;

        if (cachedSlot == -1) cacheSlot();
        restoreCountdown = -1;
        selectSlot(slot);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
        restoreSlot();

        return true;
    }

    public static boolean useDragonKey() {
        if (mc.player == null || mc.gameMode == null) return false;

        int slot = findItemSlot(DRAGON_KEY_NAME);
        if (slot == -1) return false;

        if (cachedSlot == -1) cacheSlot();
        restoreCountdown = -1;
        selectSlot(slot);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);

        return true;
    }

    // wither summon
    public static boolean useSummonItem() {
        return useItem(WITHER_TOKEN_NAME);
    }
}