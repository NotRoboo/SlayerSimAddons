package com.roboo;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class MovementHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double X_TOLERANCE   = 0.5;
    private static final double Z_TOLERANCE   = 0.5;
    private static final double BRAKE_ZONE    = 0.5;
    private static final float  YAW_TOLERANCE = 10f;

    // =========================
    // MOVE TO X
    // =========================
    public static boolean moveToX(double targetX) {
        return moveToX(targetX, X_TOLERANCE, BRAKE_ZONE);
    }

    public static boolean moveToX(double targetX, double tolerance) {
        return moveToX(targetX, tolerance, BRAKE_ZONE);
    }

    public static boolean moveToX(double targetX, double tolerance, double brakeZone) {
        if (mc.player == null) return false;

        double px   = mc.player.getX();
        double diff = px - targetX;

        if (Math.abs(diff) <= tolerance) {
            stopMovement();
            return true;
        }

        float yaw = mc.player.getYRot();
        boolean inBrakeZone = Math.abs(diff) <= brakeZone;

        KeyMapping forward = mc.options.keyUp;
        KeyMapping back    = mc.options.keyDown;
        KeyMapping left    = mc.options.keyLeft;
        KeyMapping right   = mc.options.keyRight;

        if (isNear(yaw, 0f)) {           // Facing South
            if (diff > 0) { right.setDown(true); left.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
            else          { left.setDown(true);  right.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
        }
        else if (isNear(yaw, 180f)) {    // Facing North
            if (diff > 0) { left.setDown(true);  right.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }  // West = Left
            else          { right.setDown(true); left.setDown(inBrakeZone);  forward.setDown(false); back.setDown(false); }  // East = Right
        }
        else if (isNear(yaw, 90f)) {     // Facing West
            if (diff > 0) { forward.setDown(true);  back.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
            else          { back.setDown(true);     forward.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
        }
        else if (isNear(yaw, -90f)) {    // Facing East
            if (diff > 0) { back.setDown(true);     forward.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
            else          { forward.setDown(true);  back.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
        }

        return false;
    }

    // =========================
    // MOVE TO Z
    // =========================
    public static boolean moveToZ(double targetZ) {
        return moveToZ(targetZ, Z_TOLERANCE, BRAKE_ZONE);
    }

    public static boolean moveToZ(double targetZ, double tolerance) {
        return moveToZ(targetZ, tolerance, BRAKE_ZONE);
    }

    public static boolean moveToZ(double targetZ, double tolerance, double brakeZone) {
        if (mc.player == null) return false;

        double pz   = mc.player.getZ();
        double diff = pz - targetZ;   // Positive diff = need to go more negative Z (North)

        if (Math.abs(diff) <= tolerance) {
            stopMovement();
            return true;
        }

        float yaw = mc.player.getYRot();
        boolean inBrakeZone = Math.abs(diff) <= brakeZone;

        KeyMapping forward = mc.options.keyUp;
        KeyMapping back    = mc.options.keyDown;
        KeyMapping left    = mc.options.keyLeft;
        KeyMapping right   = mc.options.keyRight;

        if (isNear(yaw, 0f)) {           // Facing South (+Z)
            if (diff > 0) { back.setDown(true);     forward.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
            else          { forward.setDown(true);  back.setDown(inBrakeZone);    left.setDown(false); right.setDown(false); }
        }
        else if (isNear(yaw, 180f)) {    // Facing North (-Z)
            if (diff > 0) { forward.setDown(true);  back.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }   // Fixed
            else          { back.setDown(true);     forward.setDown(inBrakeZone); left.setDown(false); right.setDown(false); }
        }
        else if (isNear(yaw, 90f)) {     // Facing West (-X)
            if (diff > 0) { right.setDown(true); left.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
            else          { left.setDown(true);  right.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
        }
        else if (isNear(yaw, -90f)) {    // Facing East (+X)
            if (diff > 0) { left.setDown(true);  right.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
            else          { right.setDown(true); left.setDown(inBrakeZone); forward.setDown(false); back.setDown(false); }
        }

        return false;
    }

    // =========================
    // STOP ALL
    // =========================
    public static void stopMovement() {
        if (mc.player == null) return;
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
    }

    // =========================
    // UTIL
    // =========================
    private static boolean isNear(float yaw, float target) {
        float diff = yaw - target;
        diff = diff % 360f;
        if (diff >= 180f)  diff -= 360f;
        if (diff < -180f) diff += 360f;
        return Math.abs(diff) <= YAW_TOLERANCE;
    }
}