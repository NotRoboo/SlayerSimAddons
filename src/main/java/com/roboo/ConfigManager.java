package com.roboo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("slayersimaddons.json");

    private static class ConfigData {
        boolean autoWitherBossEnabled = true;
        boolean autoDragonBossEnabled = true;
        boolean comboAttackEnabled    = true;
        boolean autoDodgeEnabled      = true;
        boolean autoReconnectEnabled  = true;
        boolean darkAuctionHudEnabled = true;
        String  pathfindingMode       = "None";
        int     darkAuctionHudX       = 10;
        int     darkAuctionHudY       = 30;
        int     autoStoreDelay        = 1200;
        boolean autoStoreHudEnabled   = true;
        int     autoStoreHudX         = 10;
        int     autoStoreHudY         = 40;
        boolean sprintHudEnabled      = true;
        int     sprintHudX            = 10;
        int     sprintHudY            = 10;
        boolean useHudEnabled         = true;
        int     useHudX               = 10;
        int     useHudY               = 20;
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            save();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) return;

            ModConfig.setAutoWitherBossEnabled(data.autoWitherBossEnabled);
            ModConfig.setAutoDragonBossEnabled(data.autoDragonBossEnabled);
            ModConfig.setComboAttackEnabled(data.comboAttackEnabled);
            ModConfig.setAutoDodgeEnabled(data.autoDodgeEnabled);
            ModConfig.setAutoReconnectEnabled(data.autoReconnectEnabled);
            ModConfig.setDarkAuctionHudEnabled(data.darkAuctionHudEnabled);
            ModConfig.setPathfindingMode(data.pathfindingMode != null ? data.pathfindingMode : "None");
            ModConfig.setDarkAuctionHudX(data.darkAuctionHudX);
            ModConfig.setDarkAuctionHudY(data.darkAuctionHudY);
            ModConfig.setAutoStoreDelay(data.autoStoreDelay);
            ModConfig.setAutoStoreHudEnabled(data.autoStoreHudEnabled);
            ModConfig.setAutoStoreHudX(data.autoStoreHudX);
            ModConfig.setAutoStoreHudY(data.autoStoreHudY);
            ModConfig.setSprintHudEnabled(data.sprintHudEnabled);
            ModConfig.setSprintHudX(data.sprintHudX);
            ModConfig.setSprintHudY(data.sprintHudY);
            ModConfig.setUseHudEnabled(data.useHudEnabled);
            ModConfig.setUseHudX(data.useHudX);
            ModConfig.setUseHudY(data.useHudY);

        } catch (IOException e) {
            System.err.println("[AutoWither] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.autoWitherBossEnabled = ModConfig.isAutoWitherBossEnabled();
        data.autoDragonBossEnabled = ModConfig.isAutoDragonBossEnabled();
        data.comboAttackEnabled    = ModConfig.isComboAttackEnabled();
        data.autoDodgeEnabled      = ModConfig.isAutoDodgeEnabled();
        data.autoReconnectEnabled  = ModConfig.isAutoReconnectEnabled();
        data.darkAuctionHudEnabled = ModConfig.isDarkAuctionHudEnabled();
        data.pathfindingMode       = ModConfig.getPathfindingMode();
        data.darkAuctionHudX       = ModConfig.getDarkAuctionHudX();
        data.darkAuctionHudY       = ModConfig.getDarkAuctionHudY();
        data.autoStoreDelay        = ModConfig.getAutoStoreDelay();
        data.autoStoreHudEnabled   = ModConfig.isAutoStoreHudEnabled();
        data.autoStoreHudX         = ModConfig.getAutoStoreHudX();
        data.autoStoreHudY         = ModConfig.getAutoStoreHudY();
        data.sprintHudEnabled      = ModConfig.isSprintHudEnabled();
        data.sprintHudX            = ModConfig.getSprintHudX();
        data.sprintHudY            = ModConfig.getSprintHudY();
        data.useHudEnabled         = ModConfig.isUseHudEnabled();
        data.useHudX               = ModConfig.getUseHudX();
        data.useHudY               = ModConfig.getUseHudY();

        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("[AutoWither] Failed to save config: " + e.getMessage());
        }
    }
}