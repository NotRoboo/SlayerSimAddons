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

        // =========================
        // COMBAT
        // =========================
        ConfigCategory combat = builder.getOrCreateCategory(Component.literal("Combat"));

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Combo Attack"), ModConfig.isComboAttackEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Dodge when boss uses Combo Attack"))
                .setSaveConsumer(ModConfig::setComboAttackEnabled)
                .build());

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Wither Magic"), ModConfig.isWitherMagicEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Move to safe zone on Wither Magic"))
                .setSaveConsumer(ModConfig::setWitherMagicEnabled)
                .build());

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Demon Magic"), ModConfig.isDemonMagicEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Move to safe zone and dodge on Demon Magic"))
                .setSaveConsumer(ModConfig::setDemonMagicEnabled)
                .build());

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Dodge / Parry"), ModConfig.isAutoDodgeEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enables DodgeHelper and ParryHelper during combat"))
                .setSaveConsumer(ModConfig::setAutoDodgeEnabled)
                .build());

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Summon"), ModConfig.isAutoSummonEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Automatically use Lord Token after kill or death"))
                .setSaveConsumer(ModConfig::setAutoSummonEnabled)
                .build());

        combat.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Reconnect"), ModConfig.isAutoReconnectEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Automatically run lobby/visit commands on disconnect messages"))
                .setSaveConsumer(ModConfig::setAutoReconnectEnabled)
                .build());

        // =========================
        // DUNGEON
        // =========================
        ConfigCategory mobPathing = builder.getOrCreateCategory(Component.literal("Mob Pathing"));

        mobPathing.addEntry(entry
                .startBooleanToggle(Component.literal("Crescent Tower"), ModConfig.isCrescentTowerEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Enable auto-routing for Crescent Tower — disable Volcano if using this"))
                .setSaveConsumer(ModConfig::setCrescentTowerEnabled)
                .build());

        mobPathing.addEntry(entry
                .startBooleanToggle(Component.literal("Ancient Volcano"), ModConfig.isVolcanoEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Enable auto-routing for Ancient Volcano — disable Crescent Tower if using this"))
                .setSaveConsumer(ModConfig::setVolcanoEnabled)
                .build());

        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }
}