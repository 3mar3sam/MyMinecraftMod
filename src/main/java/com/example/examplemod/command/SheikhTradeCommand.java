package com.example.examplemod.command;

import com.example.examplemod.entity.SheikhEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SheikhTradeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sheikh")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("trade_outside")
                    .then(Commands.argument("entityId", IntegerArgumentType.integer())
                        .then(Commands.argument("accept", BoolArgumentType.bool())
                            .executes(context -> {
                                int entityId = IntegerArgumentType.getInteger(context, "entityId");
                                boolean accept = BoolArgumentType.getBool(context, "accept");
                                ServerPlayer player = context.getSource().getPlayer();
                                if (player != null) {
                                    Entity entity = player.serverLevel().getEntity(entityId);
                                    if (entity instanceof SheikhEntity sheikh) {
                                        sheikh.handleDialogueResponse(player, accept);
                                    }
                                }
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("accept_quest")
                    .then(Commands.argument("entityId", IntegerArgumentType.integer())
                        .then(Commands.argument("accept", BoolArgumentType.bool())
                            .executes(context -> {
                                int entityId = IntegerArgumentType.getInteger(context, "entityId");
                                boolean accept = BoolArgumentType.getBool(context, "accept");
                                ServerPlayer player = context.getSource().getPlayer();
                                if (player != null) {
                                    Entity entity = player.serverLevel().getEntity(entityId);
                                    if (entity instanceof SheikhEntity sheikh) {
                                        sheikh.handleQuestResponse(player, accept);
                                    }
                                }
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("reset_quest")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player != null) {
                            SheikhEntity.setQuestAccepted(player, false);
                            SheikhEntity.setQuestCompleted(player, false);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aتمت إعادة ضبط حالة مهمة العهد بنجاح."));
                        }
                        return 1;
                    })
                )
        );
    }
}
