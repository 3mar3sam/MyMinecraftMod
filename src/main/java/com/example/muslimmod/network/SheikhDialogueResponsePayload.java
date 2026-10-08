package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SheikhDialogueResponsePayload(int entityId, boolean accept, int dialogueType) implements CustomPacketPayload {
    public static final Type<SheikhDialogueResponsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sheikh_dialogue_response"));

    public static final StreamCodec<FriendlyByteBuf, SheikhDialogueResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SheikhDialogueResponsePayload::entityId,
            ByteBufCodecs.BOOL, SheikhDialogueResponsePayload::accept,
            ByteBufCodecs.VAR_INT, SheikhDialogueResponsePayload::dialogueType,
            SheikhDialogueResponsePayload::new
    );

    public SheikhDialogueResponsePayload(int entityId, boolean accept) {
        this(entityId, accept, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
