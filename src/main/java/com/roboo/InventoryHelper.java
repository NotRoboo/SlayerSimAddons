package com.roboo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class InventoryHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String TOKEN_NAME = "Lord Token";

    private static int cachedSlot = -1;

    // =========================
    // CHECK IF TOKEN EXISTS IN HOTBAR
    // =========================
    public static boolean hasSummonItem() {
        return findTokenSlot() != -1;
    }

    // =========================
    // FIND TOKEN SLOT (HOTBAR ONLY)
    // =========================
    public static int findTokenSlot() {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.isEmpty()) {
                String name = stack.getHoverName().getString();

                if (name.contains(TOKEN_NAME)) {
                    return i;
                }
            }
        }

        return -1;
    }

    // =========================
    // SELECT SLOT (SAFE MAPPING METHOD)
    // =========================
    public static void selectSlot(int slot) {
        if (mc.player == null) return;
        if (slot < 0 || slot > 8) return;

        mc.player.getInventory().setSelectedSlot(slot);
    }

    // =========================
    // CACHE CURRENT SLOT
    // =========================
    public static void cacheSlot() {
        if (mc.player == null) return;

        cachedSlot = mc.player.getInventory().getSelectedSlot();
    }

    // =========================
    // RESTORE SLOT
    // =========================
    public static void restoreSlot() {
        if (mc.player == null) return;
        if (cachedSlot == -1) return;

        mc.player.getInventory().setSelectedSlot(cachedSlot);
        cachedSlot = -1;
    }

    // =========================
// USE TOKEN (RIGHT CLICK FLOW)
// =========================
    public static boolean useSummonItem() {
        if (mc.player == null || mc.gameMode == null) return false;

        int tokenSlot = findTokenSlot();
        if (tokenSlot == -1) return false;  // Token not found — retry makes sense here

        // save current slot
        cacheSlot();

        // switch to token
        selectSlot(tokenSlot);

        // use item (right click equivalent)
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);

        // restore previous slot
        restoreSlot();

        return true;  // Token was found and use was attempted
    }
}