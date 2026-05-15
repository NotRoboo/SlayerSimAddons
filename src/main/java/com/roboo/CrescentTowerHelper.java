package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class CrescentTowerHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double TARGET_X_BACK = -99.5;
    private static final double X_BACK_TOLERANCE = 0.1;   // tighter — short move

    private static final double TARGET_Z_LEFT = -8.5;
    private static final double Z_TOLERANCE = 0.15;

    private static final double TARGET_X_FWD = -105.7;
    private static final double X_FWD_TOLERANCE = 0.2;

    private static final double TARGET_Z_RIGHT = -13.5;
    private static final double Z_RIGHT_TOLERANCE = 0.10;

    private static final float INIT_YAW = 90f;
    private static final float INIT_PITCH = 0f;
    private static final float FINAL_YAW = -90f;
    private static final float FINAL_PITCH = 0f;
    private static final float ROTATION_TOLERANCE = 0.3f;

    private static final long COMMAND_DELAY_MS = 1000;;

    // Sequence state
    private enum Stage {
        IDLE,
        INIT_ROTATE,
        MOVE_BACK,
        MOVE_LEFT,
        MOVE_FORWARD,
        MOVE_RIGHT,
        ROTATE,
        PENDING_MBAG,
        VIS_TOGGLE
    }

    private static Stage stage = Stage.IDLE;
    private static boolean modTriggered = false;
    private static long pendingMbagTime = 0;
    private static long pendingVisTime = 0;

    // =========================
    // INIT
    // =========================
    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) ->
                handleMessage(msg.getString()));

        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) ->
                handleMessage(msg.getString()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    // =========================
    // TRIGGER
    // =========================
    public static void trigger() {
        modTriggered = true;
    }

    // =========================
    // CHAT
    // =========================
    private static void handleMessage(String msg) {
        if (msg == null) return;
        if (!ModConfig.isCrescentTowerEnabled()) return;
        if (!modTriggered) return;

        String clean = msg.toLowerCase(Locale.ROOT);

        if (clean.contains("warping...")) {
            stage = Stage.INIT_ROTATE;
            modTriggered = false;
            log("§eWarping detected — starting movement sequence");
        }
    }

    // =========================
    // TICK
    // =========================
    private static void onTick() {
        if (mc.player == null || !ModConfig.isCrescentTowerEnabled()) return;

        long now = System.currentTimeMillis();

        switch (stage) {
            case IDLE -> {
            }

            case INIT_ROTATE -> {
                boolean done = RotationHelper.lookAt(INIT_YAW, INIT_PITCH);
                float yawDiff = Math.abs(wrapDegrees(mc.player.getYRot() - INIT_YAW));
                float pitchDiff = Math.abs(mc.player.getXRot() - INIT_PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.MOVE_BACK;
                    log("§eInitial rotation done — moving back");
                }
            }

            case MOVE_BACK -> {
                double px = mc.player.getX();
                if (Math.abs(px - TARGET_X_BACK) <= X_BACK_TOLERANCE) {
                    mc.options.keyDown.setDown(false);
                    stage = Stage.MOVE_LEFT;
                    log("§eReached X back target — moving left");
                } else {
                    mc.options.keyDown.setDown(true);
                }
            }

            case MOVE_LEFT -> {
                double pz = mc.player.getZ();
                if (Math.abs(pz - TARGET_Z_LEFT) <= Z_TOLERANCE) {
                    mc.options.keyLeft.setDown(false);
                    stage = Stage.MOVE_FORWARD;
                    log("§eReached Z left target — moving forward");
                } else {
                    mc.options.keyLeft.setDown(true);
                }
            }

            case MOVE_FORWARD -> {
                double px = mc.player.getX();
                if (Math.abs(px - TARGET_X_FWD) <= X_FWD_TOLERANCE) {
                    mc.options.keyUp.setDown(false);
                    stage = Stage.MOVE_RIGHT;
                    log("§eReached X forward target — moving right");
                } else {
                    mc.options.keyUp.setDown(true);
                }
            }

            case MOVE_RIGHT -> {
                double pz = mc.player.getZ();
                if (Math.abs(pz - TARGET_Z_RIGHT) <= Z_RIGHT_TOLERANCE) {
                    mc.options.keyRight.setDown(false);
                    stage = Stage.ROTATE;
                    log("§eReached Z right target — rotating");
                } else {
                    mc.options.keyRight.setDown(true);
                }
            }

            case ROTATE -> {
                boolean done = RotationHelper.lookAt(FINAL_YAW, FINAL_PITCH);
                float yawDiff = Math.abs(wrapDegrees(mc.player.getYRot() - FINAL_YAW));
                float pitchDiff = Math.abs(mc.player.getXRot() - FINAL_PITCH);
                if (done || (yawDiff < ROTATION_TOLERANCE && pitchDiff < ROTATION_TOLERANCE)) {
                    stage = Stage.PENDING_MBAG;
                    pendingMbagTime = System.currentTimeMillis() + COMMAND_DELAY_MS;
                    log("§eFinal rotation done — queuing /mbag toggle");
                }
            }

            case PENDING_MBAG -> {
                if (now >= pendingMbagTime) {
                    runCommand("mbag toggle");
                    stage = Stage.VIS_TOGGLE;
                    pendingVisTime = System.currentTimeMillis() + COMMAND_DELAY_MS;
                    log("§aRan: /mbag toggle");
                }
            }

            case VIS_TOGGLE -> {
                if (now >= pendingVisTime) {
                    runCommand("visibility 0");
                    stage = Stage.IDLE;
                    log("§aRan: /visibility 0");
                }
            }
        }
    }

    // =========================
    // RESET
    // =========================
    public static void reset() {
        stage = Stage.IDLE;
        modTriggered = false;
        pendingMbagTime = 0;
        mc.options.keyLeft.setDown(false);
        mc.options.keyUp.setDown(false);
    }

    // =========================
    // HELPERS
    // =========================
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

    private static void log(String text) {
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(
                    Component.literal("§e[CrescentTower] " + text)
            );
        }
    }
}