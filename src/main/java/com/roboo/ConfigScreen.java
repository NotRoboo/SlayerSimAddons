package com.roboo;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {

    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("§e[AutoWither] §fSettings"));

        ConfigEntryBuilder entry = builder.entryBuilder();

        
        // BOSS FEATURES
        ConfigCategory bosses = builder.getOrCreateCategory(Component.literal("Boss Features"));

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Wither Boss"), ModConfig.isAutoWitherBossEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enables all Wither Boss automation (overrides individual toggles when on)"))
                .setSaveConsumer(ModConfig::setAutoWitherBossEnabled)
                .build());

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Dragon Boss"), ModConfig.isAutoDragonBossEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enables all Dragon Boss automation (overrides individual toggles when on)"))
                .setSaveConsumer(ModConfig::setAutoDragonBossEnabled)
                .build());

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Combo Attack / Dark Slash"), ModConfig.isComboAttackEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Dodge + parry when boss uses Combo Attack (Wither) or Dark Slash (Dragon)"))
                .setSaveConsumer(ModConfig::setComboAttackEnabled)
                .build());

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Dodge / Parry"), ModConfig.isAutoDodgeEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enables DodgeHelper and ParryHelper during boss combat"))
                .setSaveConsumer(ModConfig::setAutoDodgeEnabled)
                .build());

        // AFK FEATURES
        ConfigCategory afk = builder.getOrCreateCategory(Component.literal("AFK Features"));

        afk.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Reconnect"), ModConfig.isAutoReconnectEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Automatically run lobby/visit commands on disconnect messages"))
                .setSaveConsumer(ModConfig::setAutoReconnectEnabled)
                .build());

        afk.addEntry(entry
                .startBooleanToggle(Component.literal("Hex Pathfinding"), ModConfig.isHexPathfindingEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Enable auto-routing for Crescent Tower — disable Fishing Pathfinding if using this"))
                .setSaveConsumer(ModConfig::setHexPathfindingEnabled)
                .build());

        afk.addEntry(entry
                .startBooleanToggle(Component.literal("Fishing Pathfinding TBD"), ModConfig.isFishingPathfindingEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Enable auto-routing for Ancient Volcano — disable Hex Pathfinding if using this"))
                .setSaveConsumer(ModConfig::setFishingPathfindingEnabled)
                .build());

        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }
}