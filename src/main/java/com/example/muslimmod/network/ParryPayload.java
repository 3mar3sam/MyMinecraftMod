package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ParryPayload(boolean active) implements CustomPacketPayload {
    public static final Type<ParryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "parry"));
    
    public static final StreamCodec<FriendlyByteBuf, ParryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ParryPayload::active,
            ParryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
