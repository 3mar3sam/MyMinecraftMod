package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SheikhDialoguePayload(int entityId, String sheikhName, int dialogueType) implements CustomPacketPayload {
    public static final int TYPE_TRADE_OUTSIDE = 0;
    public static final int TYPE_QUEST_VOW = 1;

    public static final Type<SheikhDialoguePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sheikh_dialogue"));

    public static final StreamCodec<FriendlyByteBuf, SheikhDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SheikhDialoguePayload::entityId,
            ByteBufCodecs.STRING_UTF8, SheikhDialoguePayload::sheikhName,
            ByteBufCodecs.VAR_INT, SheikhDialoguePayload::dialogueType,
            SheikhDialoguePayload::new
    );

    public SheikhDialoguePayload(int entityId, String sheikhName) {
        this(entityId, sheikhName, TYPE_TRADE_OUTSIDE);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
