package com.roboo;

import net.minecraft.client.Minecraft;

import java.util.Random;

public class RotationHelper {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();
    private static final float BASE_SPEED = 0.15f;
    private static final float SPEED_VARIANCE = 0.04f;
    private static final float JITTER = 0.05f;
    private static final float MIN_STEP = 0.3f;

    // ROTATE BOTH AXES
    public static boolean lookAt(float targetYaw, float targetPitch) {
        if (mc.player == null) return false;

        float currentYaw   = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float yawDiff   = wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        boolean yawDone   = Math.abs(yawDiff)   < 0.5f;
        boolean pitchDone = Math.abs(pitchDiff) < 0.5f;

        if (!yawDone) {
            float speed = BASE_SPEED + (random.nextFloat() * 2 - 1) * SPEED_VARIANCE;
            float step  = yawDiff * speed;

            if (Math.abs(step) < MIN_STEP) step = Math.copySign(MIN_STEP, yawDiff);
            step += (random.nextFloat() * 2 - 1) * JITTER;

            if (Math.abs(step) > Math.abs(yawDiff)) step = yawDiff;

            mc.player.setYRot(currentYaw + step);
        } else {
            mc.player.setYRot(targetYaw);
        }

        if (!pitchDone) {
            float speed = BASE_SPEED + (random.nextFloat() * 2 - 1) * SPEED_VARIANCE;
            float step  = pitchDiff * speed;

            if (Math.abs(step) < MIN_STEP) step = Math.copySign(MIN_STEP, pitchDiff);

            step += (random.nextFloat() * 2 - 1) * JITTER;

            if (Math.abs(step) > Math.abs(pitchDiff)) step = pitchDiff;

            mc.player.setXRot(currentPitch + step);
        } else {
            mc.player.setXRot(targetPitch);
        }

        return yawDone && pitchDone;
    }

    
    // UTIL
    private static float wrapDegrees(float degrees) {
        degrees = degrees % 360.0F;
        if (degrees >= 180.0F)  degrees -= 360.0F;
        if (degrees < -180.0F) degrees += 360.0F;
        return degrees;
    }
}