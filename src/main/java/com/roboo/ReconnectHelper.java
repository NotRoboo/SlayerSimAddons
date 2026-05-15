package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class ReconnectHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private record QueuedCommand(String command, long executeAt) {}

    private static final Queue<QueuedCommand> commandQueue = new LinkedList<>();
    private static final long COOLDOWN_MS = 6000;
    private static final long MIN_DELAY_MS = 3500;
    private static final long MAX_DELAY_MS = 6000;

    private static boolean limboActive = false;
    private static long lastLimboRetry = 0;
    private static final long LIMBO_RETRY_MIN = 7000;
    private static final long LIMBO_RETRY_MAX = 10000;
    private static long limboRetryInterval = LIMBO_RETRY_MIN;

    private static long lastLimboQueued = 0;
    private static long lastHousingQueued = 0;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) ->
                handleMessage(msg.getString()));

        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) ->
                handleMessage(msg.getString()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (mc.player == null) return;
        if (!ModConfig.isAutoReconnectEnabled()) return;

        long now = System.currentTimeMillis();

        if (limboActive && now - lastLimboRetry >= limboRetryInterval) {
            lastLimboRetry = now;
            limboRetryInterval = LIMBO_RETRY_MIN +
                    (long)((LIMBO_RETRY_MAX - LIMBO_RETRY_MIN) * Math.random());
            runCommand("lobby");
        }

        while (!commandQueue.isEmpty()) {
            QueuedCommand next = commandQueue.peek();
            if (now >= next.executeAt()) {
                commandQueue.poll();
                runCommand(next.command());
            } else {
                break;
            }
        }
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        if (!ModConfig.isAutoReconnectEnabled()) return;

        String clean = msg.replaceAll("§.", "").toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();

        if (clean.contains("limbo for more information")) {
            if (now - lastLimboQueued >= COOLDOWN_MS) {
                lastLimboQueued = now;
                limboActive = true;
                lastLimboRetry = now;
                limboRetryInterval = LIMBO_RETRY_MIN;
                commandQueue.clear();
                enqueueAt("lobby", now + randomDelay());
            }
        }

        if (clean.contains("unclaimed leveling reward!")) {
            if (now - lastHousingQueued >= COOLDOWN_MS) {
                lastHousingQueued = now;
                limboActive = false;
                long delay = resolveDelay(now);
                enqueueAt("lobby housing", delay);
                enqueueAt("visit xsublimity", delay + 5000);
            }
        }
    }

    private static void enqueueAt(String command, long executeAt) {
        commandQueue.add(new QueuedCommand(command, executeAt));
    }

    private static long randomDelay() {
        return MIN_DELAY_MS + (long)((MAX_DELAY_MS - MIN_DELAY_MS) * Math.random());
    }

    private static long resolveDelay(long now) {
        long earliest = now + randomDelay();
        if (!commandQueue.isEmpty()) {
            long latestQueued = commandQueue.stream()
                    .mapToLong(QueuedCommand::executeAt)
                    .max()
                    .orElse(now);
            earliest = Math.max(earliest, latestQueued + randomDelay());
        }
        return earliest;
    }

    private static void runCommand(String command) {
        if (mc.player == null) return;
        mc.player.connection.sendCommand(command);

        if (command.equals("visit xsublimity")) {
            ContainerHelper.waitForSlayerMenu();
        }
    }

    public static void reset() {
        limboActive = false;
        lastLimboRetry = 0;
        commandQueue.clear();
    }
}