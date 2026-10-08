package com.example.muslimmod.network;

import com.example.muslimmod.ExampleMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NiqabDashPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<NiqabDashPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "niqab_dash"));

    public static final StreamCodec<FriendlyByteBuf, NiqabDashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, NiqabDashPayload::x,
            ByteBufCodecs.DOUBLE, NiqabDashPayload::y,
            ByteBufCodecs.DOUBLE, NiqabDashPayload::z,
            NiqabDashPayload::new);

    public Vec3 direction() {
        return new Vec3(x, y, z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}