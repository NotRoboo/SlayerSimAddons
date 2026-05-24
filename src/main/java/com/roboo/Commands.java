package com.roboo;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Commands {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("mbag")

                        .then(ClientCommandManager.literal("toggle")
                                .executes(ctx -> {
                                    AutoStoreHelper.toggle();
                                    return 1;
                                })
                        )

                        .then(ClientCommandManager.literal("timer")
                                .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer(5, 6000))
                                        .executes(ctx -> {
                                            int secs = IntegerArgumentType.getInteger(ctx, "seconds");
                                            ModConfig.setAutoStoreDelay(secs);
                                            ConfigManager.save();
                                            msg("§fTimer set to §e" + secs + "s");
                                            return 1;
                                        })
                                )
                        )

                        .then(ClientCommandManager.literal("emptyinv")
                                .executes(ctx -> {
                                    if (EmptyInvHelper.isRunning()) {
                                        EmptyInvHelper.stop();
                                        EmptyInvHelper.msg("§cStopped.");
                                    } else {
                                        EmptyInvHelper.start();
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void msg(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("§e[AutoStore] " + text), false);
    }
}