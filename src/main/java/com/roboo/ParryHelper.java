package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ParryHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    // =========================
    // CONFIG
    // =========================
    private static final int DROP_DELAY_TICKS = 4; // ~200ms

    // =========================
    // STATE
    // =========================
    private static int dropTicks = 0;
    private static boolean queued = false;

    // =========================
    // INIT (CALL ONCE)
    // =========================
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    // =========================
    // EXTERNAL TRIGGER
    // =========================
    public static void trigger() {
        // prevent spam re-triggering
        if (queued) return;

        dropTicks = DROP_DELAY_TICKS;
        queued = true;
    }

    // =========================
    // TICK
    // =========================
    private static void onTick() {
        if (!queued) return;

        if (dropTicks > 0) {
            dropTicks--;
        }

        if (dropTicks == 0) {
            executeDrop();
            queued = false;
        }
    }

    // =========================
    // DROP ACTION
    // =========================
    private static void executeDrop() {
        if (mc.player == null || mc.gameMode == null) return;

        mc.execute(() -> {
            if (mc.player == null || mc.gameMode == null) return;

            // exact same behavior as your proven version
            mc.player.drop(false);
        });
    }

    // =========================
    // OPTIONAL RESET
    // =========================
    public static void reset() {
        dropTicks = 0;
        queued = false;
    }
}