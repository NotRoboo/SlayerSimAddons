package com.roboo;

import net.minecraft.client.Minecraft;

public class RotationHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final float YAW_STEP = 6f;
    private static final float PITCH_STEP = 6f;

    // =========================
    // ROTATE BOTH AXES (FIXED)
    // =========================
    public static boolean lookAt(float targetYaw, float targetPitch) {
        if (mc.player == null) return false;

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float yawDiff = wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        // apply step
        float newYaw = currentYaw + clamp(yawDiff, -YAW_STEP, YAW_STEP);
        float newPitch = currentPitch + clamp(pitchDiff, -PITCH_STEP, PITCH_STEP);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);

        // IMPORTANT: check ORIGINAL distance, not post-step noise
        return Math.abs(yawDiff) < 1f && Math.abs(pitchDiff) < 1f;
    }

    // =========================
    // UTIL
    // =========================
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float wrapDegrees(float degrees) {
        degrees = degrees % 360.0F;

        if (degrees >= 180.0F) degrees -= 360.0F;
        if (degrees < -180.0F) degrees += 360.0F;

        return degrees;
    }
}