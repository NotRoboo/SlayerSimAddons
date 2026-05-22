package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.Locale;

public class IceDragHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double TARGET_X_1      = -110.5;
    private static final double TARGET_X_2      = -99.8;
    private static final double TARGET_X_3      = -92.7;
    private static final double TARGET_Z        = 90.0;
    private static final double TARGET_X_4      = -102.7;
    private static final double TOLERANCE       = 0.1;

    private static final float YAW_1            = -50f;
    private static final float YAW_2            = -140f;
    private static final float YAW_3            = 180f;
    private static final float YAW_4            = 127f;
    private static final float YAW_5            = -9.5f;
    private static final float PITCH            = 0f;
    private static final float ROTATION_TOLERANCE = 0.1f;

    private static final long COMMAND_DELAY_MS  = 1000;

    private enum Stage {
        IDLE,
        MOVE_1,
        ROTATE_1,
        MOVE_2,
        ROTATE_2,
        MOVE_3,
        ROTATE_3,
        MOVE_4,
        ROTATE_4,
        MOVE_5,
        ROTATE_5,
        PENDING_MBAG,
        VIS_TOGGLE
    }

    private static Stage stage          = Stage.IDLE;
    private static boolean modTriggered = false;
    private static long pendingMbagTime = 0;
    private static long pendingVisTime  = 0;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) ->
                handleMessage(msg.getString()));

        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) ->
                handleMessage(msg.getString()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    public static void trigger() {
        modTriggered = true;
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        if (!ModConfig.getPathfindingMode().equals("Ice Dragon")) return;
        if (!modTriggered) return;

        String clean = msg.toLowerCase(Locale.ROOT);

        if (clean.contains("warping...")) {
            stage = Stage.MOVE_1;
            modTriggered = false;
        }
    }

    private static void onTick() {
        if (mc.player == null || !ModConfig.getPathfindingMode().equals("Ice Dragon")) return;

        long now = System.currentTimeMillis();

        switch (stage) {
            case IDLE -> {}

            case MOVE_1 -> {
                if (MovementHelper.walkForwardToX(TARGET_X_1, TOLERANCE)) {
                    stage = Stage.ROTATE_1;
                }
            }

            case ROTATE_1 -> {
                boolean done    = RotationHelper.lookAt(YAW_1, PITCH);
                float yawDiff   = Math.abs(wrapDegrees(mc.player.getYRot() - YAW_1));
                float pitchDiff = Math.abs(mc.player.getXRot() - PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.MOVE_2;
                }
            }

            case MOVE_2 -> {
                if (MovementHelper.walkForwardToX(TARGET_X_2, TOLERANCE)) {
                    stage = Stage.ROTATE_2;
                }
            }

            case ROTATE_2 -> {
                boolean done    = RotationHelper.lookAt(YAW_2, PITCH);
                float yawDiff   = Math.abs(wrapDegrees(mc.player.getYRot() - YAW_2));
                float pitchDiff = Math.abs(mc.player.getXRot() - PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.MOVE_3;
                }
            }

            case MOVE_3 -> {
                if (MovementHelper.walkForwardToX(TARGET_X_3, TOLERANCE)) {
                    stage = Stage.ROTATE_3;
                }
            }

            case ROTATE_3 -> {
                boolean done    = RotationHelper.lookAt(YAW_3, PITCH);
                float yawDiff   = Math.abs(wrapDegrees(mc.player.getYRot() - YAW_3));
                float pitchDiff = Math.abs(mc.player.getXRot() - PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.MOVE_4;
                }
            }

            case MOVE_4 -> {
                if (MovementHelper.walkForwardToZ(TARGET_Z, TOLERANCE)) {
                    stage = Stage.ROTATE_4;
                }
            }

            case ROTATE_4 -> {
                boolean done    = RotationHelper.lookAt(YAW_4, PITCH);
                float yawDiff   = Math.abs(wrapDegrees(mc.player.getYRot() - YAW_4));
                float pitchDiff = Math.abs(mc.player.getXRot() - PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.MOVE_5;
                }
            }

            case MOVE_5 -> {
                if (MovementHelper.walkForwardToX(TARGET_X_4, TOLERANCE)) {
                    stage = Stage.ROTATE_5;
                }
            }

            case ROTATE_5 -> {
                boolean done    = RotationHelper.lookAt(YAW_5, PITCH);
                float yawDiff   = Math.abs(wrapDegrees(mc.player.getYRot() - YAW_5));
                float pitchDiff = Math.abs(mc.player.getXRot() - PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.PENDING_MBAG;
                    pendingMbagTime = System.currentTimeMillis() + COMMAND_DELAY_MS;
                }
            }

            case PENDING_MBAG -> {
                if (now >= pendingMbagTime) {
                    runCommand("mbag toggle");
                    stage = Stage.VIS_TOGGLE;
                    pendingVisTime = System.currentTimeMillis() + COMMAND_DELAY_MS;
                }
            }

            case VIS_TOGGLE -> {
                if (now >= pendingVisTime) {
                    runCommand("visibility 0");
                    stage = Stage.IDLE;
                }
            }
        }
    }

    public static void reset() {
        stage = Stage.IDLE;
        modTriggered = false;
        pendingMbagTime = 0;
        MovementHelper.stopMovement();
    }

    private static float wrapDegrees(float degrees) {
        degrees = degrees % 360.0f;
        if (degrees >= 180.0f) degrees -= 360.0f;
        if (degrees < -180.0f) degrees += 360.0f;
        return degrees;
    }

    private static void runCommand(String command) {
        if (mc.player == null) return;
        mc.player.connection.sendCommand(command);
    }
}