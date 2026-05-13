package com.roboo;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class MovementHelper {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final double X_TOLERANCE = 0.5;

    public static boolean moveToX(double targetX) {
        if (mc.player == null) return false;

        double px = mc.player.getX();

        KeyMapping forward = mc.options.keyUp;
        KeyMapping back = mc.options.keyDown;

        double diff = px - targetX;

        // Arrived
        if (Math.abs(diff) <= X_TOLERANCE) {
            stopMovement();
            return true;
        }

        // FIXED DIRECTION LOGIC
        if (diff > 0) {
            // Player X is larger than target → move forward (more negative X)
            forward.setDown(true);
            back.setDown(false);
        } else {
            // Player X is smaller than target → move backward
            forward.setDown(false);
            back.setDown(true);
        }

        return false;
    }

    public static void stopMovement() {
        if (mc.player == null) return;

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
    }
}