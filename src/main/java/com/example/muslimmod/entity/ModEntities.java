package com.example.muslimmod.entity;

import com.example.muslimmod.ExampleMod;
import com.example.muslimmod.entity.ik.worm.WormChainEntity;
import com.example.muslimmod.entity.ik.worm.WormHeadSegment;
import com.example.muslimmod.entity.ik.worm.WormSegment;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ExampleMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WormChainEntity>> WORM_CHAIN = ENTITIES.register("worm_chain",
            () -> EntityType.Builder.of(WormChainEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).fireImmune().build("worm_chain"));

    public static final DeferredHolder<EntityType<?>, EntityType<WormChainEntity>> SAND_WORM = WORM_CHAIN;

    public static final DeferredHolder<EntityType<?>, EntityType<WormSegment>> WORM_SEGMENT = ENTITIES.register("worm_segment",
            () -> EntityType.Builder.<WormSegment>of(WormSegment::new, MobCategory.MISC).sized(4.5F, 4.5F).fireImmune().build("worm_segment"));

    public static final DeferredHolder<EntityType<?>, EntityType<WormHeadSegment>> WORM_HEAD_SEGMENT = ENTITIES.register("worm_head_segment",
            () -> EntityType.Builder.<WormHeadSegment>of(WormHeadSegment::new, MobCategory.MISC).sized(4.5F, 4.5F).fireImmune().build("worm_head_segment"));
}
