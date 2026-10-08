package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlungeAttackPayload(boolean plunging) implements CustomPacketPayload {
    public static final Type<PlungeAttackPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "plunge_attack"));

    public static final StreamCodec<FriendlyByteBuf, PlungeAttackPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlungeAttackPayload::plunging,
            PlungeAttackPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
