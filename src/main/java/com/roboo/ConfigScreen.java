package com.roboo;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class ConfigScreen {

    private static final List<String> PATHFINDING_MODES = Arrays.asList(
            "None",
            "AutoFish",
            "Squid",
            "Megalodon",
            "Sea Dragon",
            "Blaze Warden",
            "Demon",
            "Magma",
            "Crystal Op",
            "Echo",
            "Vampire",
            "Priest",
            "Elf"
    );

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
                .setTooltip(Component.literal("Enables all Wither Boss automation"))
                .setSaveConsumer(ModConfig::setAutoWitherBossEnabled)
                .build());

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Auto Dragon Boss"), ModConfig.isAutoDragonBossEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enables all Dragon Boss automation"))
                .setSaveConsumer(ModConfig::setAutoDragonBossEnabled)
                .build());

        bosses.addEntry(entry
                .startBooleanToggle(Component.literal("Combo Attack / Dark Slash"), ModConfig.isComboAttackEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Dodge + parry on Combo Attack (Wither) or Dark Slash (Dragon)"))
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
                .setTooltip(Component.literal("Auto-runs lobby/visit commands on disconnect"))
                .setSaveConsumer(ModConfig::setAutoReconnectEnabled)
                .build());

        afk.addEntry(entry
                .startDropdownMenu(
                        Component.literal("Auto Pathing"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(
                                ModConfig.getPathfindingMode(),
                                s -> s,
                                Component::literal
                        ),
                        DropdownMenuBuilder.CellCreatorBuilder.of()
                )
                .setDefaultValue("None")
                .setSelections(PATHFINDING_MODES)
                .setTooltip(
                        Component.literal("None (Disabled)"),
                        Component.literal("AutoFish - TBD"),
                        Component.literal("Squid — TBD"),
                        Component.literal("Megalodon — TBD"),
                        Component.literal("Sea Dragon — TBD"),
                        Component.literal("Blaze Warden — TBD"),
                        Component.literal("Demon — TBD"),
                        Component.literal("Magma — TBD"),
                        Component.literal("Crystal Op — TBD"),
                        Component.literal("Echo"),
                        Component.literal("Vampire"),
                        Component.literal("Priest"),
                        Component.literal("Elf")
                )
                .setSaveConsumer(ModConfig::setPathfindingMode)
                .build());

        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }
}