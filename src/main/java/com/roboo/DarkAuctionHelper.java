package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.Locale;

public class DarkAuctionHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double DA_X = -83;
    private static final double DA_Y = 106;
    private static final double DA_Z = -48;
    private static final double HOLOGRAM_RADIUS = 5;

    private static final long TWO_HOURS_SERVER_TICKS = 2 * 60 * 60 * 20L;

    private static final long ANCHOR_THRESHOLD_MS = 1500;

    private enum State { IDLE, COUNTDOWN, ACTIVE }

    private static State state = State.IDLE;
    private static long auctionEndTime = -1;
    private static int lastHologramSecs = -1;
    private static String currentItem = null;
    private static long daStartedAt = -1;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handleMessage(msg.getString()));
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) -> handleMessage(msg.getString()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("slayersimaddons", "dark_auction_hud"),
                (graphics, tickCounter) -> renderHud(graphics)
        );
    }

    private static void onTick() {
        if (!ModConfig.isDarkAuctionHudEnabled()) return;
        if (mc.player == null) return;

        if (state == State.ACTIVE) return;

        int hologramSecs = readHologramSeconds();

        if (hologramSecs > 0) {
            long projectedEnd = System.currentTimeMillis()
                    + (long)(hologramSecs * TpsTracker.getMsPerServerSecond());

            if (state == State.IDLE) {
                auctionEndTime = projectedEnd;
                lastHologramSecs = hologramSecs;
                state = State.COUNTDOWN;
            } else if (hologramSecs != lastHologramSecs
                    || Math.abs(projectedEnd - auctionEndTime) > ANCHOR_THRESHOLD_MS) {
                auctionEndTime = projectedEnd;
                lastHologramSecs = hologramSecs;
            }
        }

        if (state == State.COUNTDOWN && auctionEndTime > 0
                && System.currentTimeMillis() >= auctionEndTime) {
            state = State.IDLE;
            auctionEndTime = -1;
            lastHologramSecs = -1;
        }
    }

    private static void handleMessage(String msg) {
        if (msg == null || !ModConfig.isDarkAuctionHudEnabled()) return;

        String clean = msg.replaceAll("§.", "").trim();
        String lower = clean.toLowerCase(Locale.ROOT);

        if (lower.contains("the dark auction has started")) {
            state = State.ACTIVE;
            daStartedAt = System.currentTimeMillis();
            currentItem = null;
            auctionEndTime = -1;
            lastHologramSecs = -1;
            return;
        }

        if (lower.contains("the item is") && lower.contains("bidding will last for 60 seconds")) {
            long now = System.currentTimeMillis();
            if (daStartedAt > 0 && Math.abs(now - daStartedAt) <= 10_000) {
                currentItem = parseItemName(clean);
                state = State.ACTIVE;
                auctionEndTime = now + 60_000L;
            }
            return;
        }

        if (lower.contains("the dark auction is over")) {
            state = State.COUNTDOWN;
            currentItem = null;
            auctionEndTime = System.currentTimeMillis()
                    + (long)(TWO_HOURS_SERVER_TICKS * (TpsTracker.getMsPerServerSecond() / 20.0));
            lastHologramSecs = -1;
            daStartedAt = -1;
        }
    }

    private static String parseItemName(String raw) {
        int start = raw.indexOf("The item is ");
        if (start == -1) start = raw.indexOf("the item is ");
        if (start == -1) return "Unknown";
        String after = raw.substring(start + "The item is ".length());
        int dot = after.indexOf('.');
        if (dot != -1) after = after.substring(0, dot);
        return after.trim();
    }

    private static int readHologramSeconds() {
        if (mc.level == null) return -1;

        AABB box = new AABB(
                DA_X - HOLOGRAM_RADIUS, DA_Y - HOLOGRAM_RADIUS, DA_Z - HOLOGRAM_RADIUS,
                DA_X + HOLOGRAM_RADIUS, DA_Y + HOLOGRAM_RADIUS, DA_Z + HOLOGRAM_RADIUS
        );

        for (ArmorStand stand : mc.level.getEntitiesOfClass(ArmorStand.class, box,
                s -> s.isInvisible() && s.hasCustomName())) {
            String name = cleanName(stand);
            if (name.toLowerCase(Locale.ROOT).contains("next auction:")) {
                String numPart = name.replaceAll("[^0-9]", "").trim();
                if (!numPart.isEmpty()) {
                    try { return Integer.parseInt(numPart); } catch (NumberFormatException ignored) {}
                }
            }
        }
        return -1;
    }

    private static String cleanName(ArmorStand stand) {
        var custom = stand.getCustomName();
        if (custom == null) return "";
        return custom.getString().replaceAll("§[0-9a-fk-or]", "").trim();
    }

    private static void renderHud(net.minecraft.client.gui.GuiGraphics graphics) {
        if (!ModConfig.isDarkAuctionHudEnabled() || mc.player == null) return;

        String line;

        if (state == State.ACTIVE) {
            String name = currentItem != null ? currentItem : "Unknown";
            line = "§7Dark Auction: §aActive §f(" + name + ")";
        } else if (state == State.COUNTDOWN && auctionEndTime > 0) {
            long msLeft = Math.max(0, auctionEndTime - System.currentTimeMillis());
            long totalSecs = msLeft / 1000;
            long mins = totalSecs / 60;
            long secs = totalSecs % 60;
            line = String.format("§7Dark Auction: §e%dm %02ds", mins, secs);
        } else {
            line = "§7Dark Auction: §cVisit DA for time";
        }

        graphics.drawString(mc.font, line, ModConfig.getDarkAuctionHudX(), ModConfig.getDarkAuctionHudY(), 0xFFFFFFFF, true);
    }

    public static void reset() {
        state = State.IDLE;
        auctionEndTime = -1;
        currentItem = null;
        daStartedAt = -1;
        lastHologramSecs = -1;
    }
}