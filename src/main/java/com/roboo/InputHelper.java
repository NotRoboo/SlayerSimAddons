package com.roboo;

import net.minecraft.client.Minecraft;

public class InputHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean rightClickHeld = false;

    public static void holdRightClick(boolean hold) {
        if (mc.options == null) return;

        if (hold && !rightClickHeld) {
            OptionsHelper.disableToggleUse();
        } else if (!hold && rightClickHeld) {
            OptionsHelper.restoreToggleUse();
        }

        rightClickHeld = hold;
        mc.options.keyUse.setDown(hold);
    }

    public static void stopAll() {
        rightClickHeld = false;

        if (mc.options == null) return;
        mc.options.keyUse.setDown(false);
    }
}