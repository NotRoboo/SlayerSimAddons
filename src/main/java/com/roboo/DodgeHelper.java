package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Locale;

public class DodgeHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean dodging = false;
    private static boolean forcedSneak = false;

    private static boolean parryActive = false;

    private static int safeCount = 0;
    private static int requiredSafes = 1;

    private static long dodgeStartTime = 0;
    private static final long MAX_DODGE_TIME = 7000;

    // =========================
    // INIT
    // =========================
    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
        });

        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
        });
    }

    // =========================
    // START
    // =========================
    public static void start() {
        if (dodging) return;

        dodging = true;
        dodgeStartTime = System.currentTimeMillis();
        safeCount = 0;
        parryActive = false;
    }

    public static boolean isDodging() {
        return dodging;
    }

    // =========================
    // TICK
    // =========================
    private static void onTick() {
        if (mc.player == null || !dodging) return;

        KeyMapping sneakKey = mc.options.keyShift;

        long now = System.currentTimeMillis();

        if (now - dodgeStartTime > MAX_DODGE_TIME) {
            stop();
            return;
        }

        if (!sneakKey.isDown()) {
            sneakKey.setDown(true);
            forcedSneak = true;
        }
    }

    // =========================
    // CHAT
    // =========================
    private static void handleMessage(String msg) {
        if (msg == null) return;

        String clean = msg.toLowerCase(Locale.ROOT);

        if (clean.contains("parry") || clean.contains("parried")) {
            parryActive = true;
        }

        if (clean.contains("safe")) {

            if (parryActive) {
                parryActive = false;
                return;
            }

            if (dodging) {
                safeCount++;

                if (safeCount >= requiredSafes) {
                    stop();
                }
            }
        }
    }

    // =========================
    // CONFIG
    // =========================
    public static void setRequiredSafes(boolean parry) {
        requiredSafes = parry ? 2 : 1;
        safeCount = 0;
    }

    // =========================
    // RESET (NEW — IMPORTANT)
    // =========================
    public static void reset() {
        dodging = false;
        forcedSneak = false;
        parryActive = false;
        safeCount = 0;
        requiredSafes = 1;
        dodgeStartTime = 0;
    }

    // =========================
    // STOP (FULL CLEANUP)
    // =========================
    public static void stop() {
        if (mc.player == null) {
            reset();
            return;
        }

        KeyMapping sneakKey = mc.options.keyShift;

        if (forcedSneak || sneakKey.isDown()) {
            sneakKey.setDown(false);
        }

        reset();
    }
}