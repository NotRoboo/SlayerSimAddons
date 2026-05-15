package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ParryHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int DROP_DELAY_TICKS = 4;

    private static int dropTicks = 0;
    private static boolean queued = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    
    // EXTERNAL TRIGGER
    public static void trigger() {
        if (queued) return;

        dropTicks = DROP_DELAY_TICKS;
        queued = true;
    }

    
    // TICK
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

    
    // DROP ACTION
    private static void executeDrop() {
        if (mc.player == null || mc.gameMode == null) return;

        mc.execute(() -> {
            if (mc.player == null || mc.gameMode == null) return;

            mc.player.drop(false);
        });
    }

    public static void reset() {
        dropTicks = 0;
        queued = false;
    }
}