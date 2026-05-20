package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class TpsTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int SAMPLE_SIZE = 10;

    private static final long[] intervals = new long[SAMPLE_SIZE];
    private static int head = 0;
    private static int count = 0;

    private static long lastGameTime = -1;
    private static long lastWallTime = -1;

    private static double cachedTps = 20.0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (mc.level == null) {
            lastGameTime = -1;
            lastWallTime = -1;
            return;
        }

        long gameTime = mc.level.getGameTime();

        if (lastGameTime < 0) {
            lastGameTime = gameTime;
            lastWallTime = System.currentTimeMillis();
            return;
        }

        long gameDelta = gameTime - lastGameTime;
        if (gameDelta < 20) return;

        long wallNow = System.currentTimeMillis();
        long wallDelta = wallNow - lastWallTime;

        lastGameTime = gameTime;
        lastWallTime = wallNow;

        if (wallDelta <= 0) return;

        long msPerTick = wallDelta / gameDelta;
        long msPerBatch = msPerTick * 20;

        intervals[head] = msPerBatch;
        head = (head + 1) % SAMPLE_SIZE;
        if (count < SAMPLE_SIZE) count++;

        double avgMs = 0;
        for (int i = 0; i < count; i++) avgMs += intervals[i];
        avgMs /= count;

        cachedTps = Math.min(20.0, 20_000.0 / avgMs);
    }

    public static double getTps() {
        return cachedTps;
    }

    public static double getMsPerServerSecond() {
        return 20_000.0 / cachedTps;
    }

    public static void reset() {
        lastGameTime = -1;
        lastWallTime = -1;
        head = 0;
        count = 0;
        cachedTps = 20.0;
    }
}