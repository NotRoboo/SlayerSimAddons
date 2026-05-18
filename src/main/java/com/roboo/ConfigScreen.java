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
            "None", "AutoFish", "Squid", "Megalodon", "Sea Dragon",
            "Blaze Warden", "Demon", "Magma", "Crystal Op", "Echo",
            "Vampire", "Priest", "Elf"
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


        // DARK AUCTION HUD
        ConfigCategory hud = builder.getOrCreateCategory(Component.literal("HUDs"));

        hud.addEntry(entry
                .startBooleanToggle(Component.literal("Show Dark Auction HUD"), ModConfig.isDarkAuctionHudEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Shows DA countdown, active item, or prompt to visit DA"))
                .setSaveConsumer(ModConfig::setDarkAuctionHudEnabled)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("HUD X Position"), ModConfig.getDarkAuctionHudX())
                .setDefaultValue(10)
                .setTooltip(Component.literal("Horizontal position of the Dark Auction HUD"))
                .setSaveConsumer(ModConfig::setDarkAuctionHudX)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("HUD Y Position"), ModConfig.getDarkAuctionHudY())
                .setDefaultValue(30)
                .setTooltip(Component.literal("Vertical position of the Dark Auction HUD"))
                .setSaveConsumer(ModConfig::setDarkAuctionHudY)
                .build());
        
        hud.addEntry(entry
                .startIntField(Component.literal("Deposit Timer (seconds)"), ModConfig.getAutoStoreDelay())
                .setDefaultValue(1200)
                .setTooltip(Component.literal("How often AutoStore runs (use /mbag timer to change in-game)"))
                .setSaveConsumer(ModConfig::setAutoStoreDelay)
                .build());

        hud.addEntry(entry
                .startBooleanToggle(Component.literal("Show AutoStore HUD"), ModConfig.isAutoStoreHudEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setAutoStoreHudEnabled)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("AutoStore HUD X"), ModConfig.getAutoStoreHudX())
                .setDefaultValue(10)
                .setSaveConsumer(ModConfig::setAutoStoreHudX)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("AutoStore HUD Y"), ModConfig.getAutoStoreHudY())
                .setDefaultValue(40)
                .setSaveConsumer(ModConfig::setAutoStoreHudY)
                .build());

        hud.addEntry(entry
                .startBooleanToggle(Component.literal("Show Sprint HUD"), ModConfig.isSprintHudEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setSprintHudEnabled)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("Sprint HUD X"), ModConfig.getSprintHudX())
                .setDefaultValue(10)
                .setSaveConsumer(ModConfig::setSprintHudX)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("Sprint HUD Y"), ModConfig.getSprintHudY())
                .setDefaultValue(10)
                .setSaveConsumer(ModConfig::setSprintHudY)
                .build());

        hud.addEntry(entry
                .startBooleanToggle(Component.literal("Show Right Click HUD"), ModConfig.isUseHudEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setUseHudEnabled)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("Right Click HUD X"), ModConfig.getUseHudX())
                .setDefaultValue(10)
                .setSaveConsumer(ModConfig::setUseHudX)
                .build());

        hud.addEntry(entry
                .startIntField(Component.literal("Right Click HUD Y"), ModConfig.getUseHudY())
                .setDefaultValue(20)
                .setSaveConsumer(ModConfig::setUseHudY)
                .build());


        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }
}