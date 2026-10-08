package com.example.muslimmod.block.entity;

import com.example.muslimmod.ExampleMod;
import com.example.muslimmod.entity.ModEntities;
import com.example.muslimmod.entity.ik.worm.WormChainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.AABB;

public class SandWormAltarBlockEntity extends BlockEntity {
    private boolean summoned = false;

    public SandWormAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.SAND_WORM_ALTAR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SandWormAltarBlockEntity blockEntity) {
        if (level.isClientSide() || blockEntity.summoned) {
            return;
        }

        AABB detectionBox = new AABB(blockEntity.worldPosition).inflate(85.0, 90.0, 85.0);
        Player nearestPlayer = level.getEntitiesOfClass(Player.class, detectionBox, p -> !p.isCreative() && !p.isSpectator())
            .stream().findFirst().orElse(null);

        if (nearestPlayer != null) {
            blockEntity.summonBoss(level, pos);
        }
    }

    public void summonBoss(Level level, BlockPos pos) {
        if (this.summoned) {
            return;
        }
        this.summoned = true;

        // a) Spawn WormChainEntity at worldPosition
        WormChainEntity worm = ModEntities.SAND_WORM.get().create(level);
        if (worm == null) {
            worm = new WormChainEntity(ModEntities.SAND_WORM.get(), level);
        }

        worm.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        // b) Bind arenaCenter to worldPosition (with the 60-block arenaRadius leash)
        worm.setArenaCenter(pos);

        // Add the worm to the level
        level.addFreshEntity(worm);

        // e) Play a deep rumble sound or particle effect at the altar
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5.0F, 0.5F);
        level.playSound(null, pos, SoundEvents.SAND_FALL, SoundSource.BLOCKS, 5.0F, 0.4F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 1.0, 1.0, 1.0, 0.0);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 120, 2.0, 1.5, 2.0, 0.2);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 40, 1.5, 1.0, 1.5, 0.05);
        }

        // f) Remove the altar block: replace this block with Blocks.SAND.defaultBlockState() or Blocks.AIR to prevent repeated spawns
        level.setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.summoned = tag.getBoolean("Summoned");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Summoned", this.summoned);
    }
}
