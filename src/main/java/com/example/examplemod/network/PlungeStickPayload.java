package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlungeStickPayload(int victimEntityId, boolean stuck, double offsetX, double offsetY, double offsetZ) implements CustomPacketPayload {
    public static final Type<PlungeStickPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "plunge_stick"));

    public static final StreamCodec<FriendlyByteBuf, PlungeStickPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlungeStickPayload::victimEntityId,
            ByteBufCodecs.BOOL, PlungeStickPayload::stuck,
            ByteBufCodecs.DOUBLE, PlungeStickPayload::offsetX,
            ByteBufCodecs.DOUBLE, PlungeStickPayload::offsetY,
            ByteBufCodecs.DOUBLE, PlungeStickPayload::offsetZ,
            PlungeStickPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
