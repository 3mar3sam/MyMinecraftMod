package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ExampleMod.MODID).versioned("1.0.0");
        registrar.playToServer(
                ParryPayload.TYPE,
                ParryPayload.STREAM_CODEC,
                ModPayloads::handleParry
        );
        registrar.playToServer(
                PlungeAttackPayload.TYPE,
                PlungeAttackPayload.STREAM_CODEC,
                ModPayloads::handlePlungeAttack
        );
        registrar.playToServer(
            NiqabDashPayload.TYPE,
            NiqabDashPayload.STREAM_CODEC,
            ModPayloads::handleNiqabDash
        );
        registrar.playToClient(
                PlungeStickPayload.TYPE,
                PlungeStickPayload.STREAM_CODEC,
                ModPayloads::handlePlungeStickClient
        );
        registrar.playToClient(
                SheikhDialoguePayload.TYPE,
                SheikhDialoguePayload.STREAM_CODEC,
                ModPayloads::handleSheikhDialogueClient
        );
        registrar.playToServer(
                SheikhDialogueResponsePayload.TYPE,
                SheikhDialogueResponsePayload.STREAM_CODEC,
                ModPayloads::handleSheikhDialogueResponse
        );
    }

    private static void handleParry(final ParryPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                if (payload.active()) {
                    ExampleMod.parryTimestamps.put(player.getUUID(), player.level().getGameTime());
                } else {
                    ExampleMod.parryTimestamps.remove(player.getUUID());
                }
            }
        });
    }

    private static void handlePlungeAttack(final PlungeAttackPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                com.example.muslimmod.ZulfiqarPlungeEvents.setPlunging(player, payload.plunging());
            }
        });
    }

    private static void handleNiqabDash(final NiqabDashPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                if (com.example.muslimmod.NiqabArmorEvents.isDashing(player)) {
                    com.example.muslimmod.NiqabArmorEvents.updateDash(player, payload.direction());
                } else {
                    com.example.muslimmod.NiqabArmorEvents.startDash(player, payload.direction());
                }
            }
        });
    }

    private static void handlePlungeStickClient(final PlungeStickPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            com.example.muslimmod.client.ZulfiqarPlungeHandler.handleStickSync(
                payload.victimEntityId(),
                payload.stuck(),
                payload.offsetX(),
                payload.offsetY(),
                payload.offsetZ()
            );
        });
    }

    private static void handleSheikhDialogueClient(final SheikhDialoguePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            com.example.muslimmod.client.gui.SheikhDialogueScreen.open(payload.entityId(), payload.sheikhName(), payload.dialogueType());
        });
    }

    private static void handleSheikhDialogueResponse(final SheikhDialogueResponsePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().getEntity(payload.entityId()) instanceof com.example.muslimmod.entity.SheikhEntity sheikh) {
                if (payload.dialogueType() == SheikhDialoguePayload.TYPE_QUEST_VOW) {
                    sheikh.handleQuestResponse(player, payload.accept());
                } else {
                    sheikh.handleDialogueResponse(player, payload.accept());
                }
            }
        });
    }
}
