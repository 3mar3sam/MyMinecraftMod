package com.example.examplemod;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ArakTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ARAK_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "arak_tree"));

    public static final TreeGrower ARAK_TREE_GROWER = new TreeGrower("arak", Optional.empty(), Optional.of(ARAK_TREE), Optional.empty());

    private ArakTreeFeatures() {
    }
}