package com.roboo;

import net.minecraft.client.Minecraft;

public class OptionsHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static Boolean cachedToggleUse = null;
    private static Boolean cachedAutoJump = null;
    private static Boolean cachedToggleSneak = null;

    public static void disableToggleUse() {
        if (mc.options == null) return;
        if (cachedToggleUse == null) {
            cachedToggleUse = mc.options.toggleUse().get();
        }
        if (mc.options.toggleUse().get()) {
            mc.options.toggleUse().set(false);
            mc.options.save();
        }
    }

    public static void restoreToggleUse() {
        if (mc.options == null || cachedToggleUse == null) return;
        mc.options.toggleUse().set(cachedToggleUse);
        mc.options.save();
        cachedToggleUse = null;
    }

    public static void enableAutoJump() {
        if (mc.options == null) return;
        if (cachedAutoJump == null) {
            cachedAutoJump = mc.options.autoJump().get();
        }
        if (!mc.options.autoJump().get()) {
            mc.options.autoJump().set(true);
            mc.options.save();
        }
    }

    public static void restoreAutoJump() {
        if (mc.options == null || cachedAutoJump == null) return;
        mc.options.autoJump().set(cachedAutoJump);
        mc.options.save();
        cachedAutoJump = null;
    }

    public static void disableToggleSneak() {
        if (mc.options == null) return;
        if (cachedToggleSneak == null) {
            cachedToggleSneak = mc.options.toggleCrouch().get();
        }
        if (mc.options.toggleCrouch().get()) {
            mc.options.toggleCrouch().set(false);
            mc.options.save();
        }
    }

    public static void restoreToggleSneak() {
        if (mc.options == null || cachedToggleSneak == null) return;
        mc.options.toggleCrouch().set(cachedToggleSneak);
        mc.options.save();
        cachedToggleSneak = null;
    }

    public static void restoreAll() {
        restoreToggleUse();
        restoreAutoJump();
        restoreToggleSneak();
    }
}