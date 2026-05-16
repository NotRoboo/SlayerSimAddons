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
            .resolve("autowither.json");

    private static class ConfigData {
        boolean autoWitherBossEnabled = true;
        boolean autoDragonBossEnabled = true;
        boolean comboAttackEnabled    = true;
        boolean autoDodgeEnabled      = true;
        boolean autoReconnectEnabled  = true;
        String  pathfindingMode       = "None";
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
            ModConfig.setPathfindingMode(data.pathfindingMode != null ? data.pathfindingMode : "None");

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
        data.pathfindingMode       = ModConfig.getPathfindingMode();

        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("[AutoWither] Failed to save config: " + e.getMessage());
        }
    }
}