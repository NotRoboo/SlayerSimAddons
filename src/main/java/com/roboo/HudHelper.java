package com.roboo;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class HudHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {

        // AutoStore HUD
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("slayersimaddons", "autostore_hud"),
                (graphics, tickCounter) -> {
                    if (mc.player == null || !ModConfig.isAutoStoreHudEnabled()) return;

                    long remaining = Math.max(0, (AutoStoreHelper.nextCycleTime[0] - System.currentTimeMillis()) / 1000);

                    graphics.drawString(mc.font,
                            "§7AutoStore: " + (AutoStoreHelper.enabled ? "§aON" : "§cOFF"),
                            ModConfig.getAutoStoreHudX(), ModConfig.getAutoStoreHudY(), 0xFFFFFFFF, true);

                    if (AutoStoreHelper.enabled) {
                        String detail = switch (AutoStoreHelper.getState()) {
                            case IDLE, WAITING_TO_OPEN -> "§7Next in §e" + remaining + "s";
                            case OPENING               -> "§7Opening bag...";
                            case DRAINING              -> "§7Depositing... §e(" + AutoStoreHelper.clickQueue.size() + " left)";
                            case RESCANNING            -> "§7Rescanning...";
                        };
                        graphics.drawString(mc.font, detail, ModConfig.getAutoStoreHudX(), ModConfig.getAutoStoreHudY() + 10, 0xFFFFFFFF, true);
                    }
                }
        );

        // Sprint HUD
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("slayersimaddons", "sprint_hud"),
                (graphics, tickCounter) -> {
                    if (mc.player == null || !ModConfig.isSprintHudEnabled()) return;

                    boolean toggled = mc.options.toggleSprint().get();
                    boolean keyDown = mc.options.keySprint.isDown();
                    boolean sprinting = mc.player.isSprinting();

                    String mode = null;
                    boolean active = false;

                    if (toggled && (keyDown || sprinting)) { active = true; mode = "Toggled"; }
                    else if (keyDown || sprinting)          { active = true; mode = "Holding"; }

                    String status = (active ? "§aON" : "§cOFF") + (mode != null ? " §a(" + mode + ")" : "");
                    graphics.drawString(mc.font, "§7Sprint: " + status,
                            ModConfig.getSprintHudX(), ModConfig.getSprintHudY(), 0xFFFFFFFF, true);
                }
        );

        // Right-click HUD
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("slayersimaddons", "use_hud"),
                (graphics, tickCounter) -> {
                    if (mc.player == null || !ModConfig.isUseHudEnabled()) return;

                    boolean toggleUse = mc.options.toggleUse().get();
                    boolean keyDown = mc.options.keyUse.isDown();
                    boolean usingItem = mc.player.isUsingItem();

                    String mode = null;
                    boolean active = false;

                    if (toggleUse && (keyDown || usingItem)) { active = true; mode = "Toggled"; }
                    else if (keyDown || usingItem)            { active = true; mode = "Holding"; }

                    String status = (active ? "§aON" : "§cOFF") + (mode != null ? " §a(" + mode + ")" : "");
                    graphics.drawString(mc.font, "§7Right Click: " + status,
                            ModConfig.getUseHudX(), ModConfig.getUseHudY(), 0xFFFFFFFF, true);
                }
        );
    }
}