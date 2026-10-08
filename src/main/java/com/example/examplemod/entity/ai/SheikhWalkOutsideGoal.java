package com.example.examplemod.entity.ai;

import com.example.examplemod.entity.SheikhEntity;
import com.example.examplemod.entity.SheikhInteractionState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SheikhWalkOutsideGoal extends Goal {
    private final SheikhEntity sheikh;
    private final double speedModifier;
    private int pathRecalcCooldown = 0;
    private int stuckTicks = 0;
    private final Set<BlockPos> openedDoors = new HashSet<>();

    public SheikhWalkOutsideGoal(SheikhEntity sheikh, double speedModifier) {
        this.sheikh = sheikh;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.sheikh.getTarget() != null && this.sheikh.isAggressionTriggered()) {
            return false;
        }
        return this.sheikh.getInteractionState() == SheikhInteractionState.WALKING_OUTSIDE 
                && this.sheikh.getTargetOutsidePos() != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.sheikh.getTarget() != null && this.sheikh.isAggressionTriggered()) {
            return false;
        }
        return this.sheikh.getInteractionState() == SheikhInteractionState.WALKING_OUTSIDE 
                && this.sheikh.getTargetOutsidePos() != null
                && this.stuckTicks < 600;
    }

    @Override
    public void start() {
        this.stuckTicks = 0;
        this.pathRecalcCooldown = 0;
        this.openedDoors.clear();
        BlockPos target = this.sheikh.getTargetOutsidePos();
        if (target != null) {
            this.sheikh.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.sheikh.getNavigation().stop();
        BlockPos sheikhPos = this.sheikh.blockPosition();
        for (BlockPos doorPos : this.openedDoors) {
            if (doorPos.distSqr(sheikhPos) > 4.0) {
                BlockState state = this.sheikh.level().getBlockState(doorPos);
                if (state.getBlock() instanceof DoorBlock doorBlock && state.getValue(DoorBlock.OPEN)) {
                    doorBlock.setOpen(this.sheikh, this.sheikh.level(), state, doorPos, false);
                }
            }
        }
        this.openedDoors.clear();
    }

    @Override
    public void tick() {
        this.stuckTicks++;
        BlockPos target = this.sheikh.getTargetOutsidePos();
        if (target == null) return;

        // Ensure doors in front of the sheikh are opened smoothly
        this.handleDoorInteractions();

        double distSq = this.sheikh.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        boolean isOutside = !this.sheikh.isCurrentlyInsideMosque();

        if (distSq <= 4.0 || (isOutside && distSq <= 9.0) || (isOutside && this.sheikh.getNavigation().isDone())) {
            this.sheikh.onArrivedOutside();
            return;
        }

        if (--this.pathRecalcCooldown <= 0) {
            this.pathRecalcCooldown = 25;
            this.sheikh.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, this.speedModifier);
        }

        if (this.stuckTicks >= 600) {
            if (isOutside) {
                this.sheikh.onArrivedOutside();
            } else {
                this.sheikh.onWalkOutsideFailed();
            }
        }
    }

    private void handleDoorInteractions() {
        BlockPos sheikhPos = this.sheikh.blockPosition();

        // 1. Open any closed door within 2.5 blocks of the sheikh
        for (BlockPos p : BlockPos.betweenClosed(sheikhPos.offset(-2, -1, -2), sheikhPos.offset(2, 2, 2))) {
            BlockState state = this.sheikh.level().getBlockState(p);
            if (state.getBlock() instanceof DoorBlock doorBlock) {
                if (!state.getValue(DoorBlock.OPEN)) {
                    doorBlock.setOpen(this.sheikh, this.sheikh.level(), state, p, true);
                    this.openedDoors.add(p.immutable());
                }
            }
        }

        // 2. Close doors once the sheikh has walked farther than 3 blocks away
        Iterator<BlockPos> it = this.openedDoors.iterator();
        while (it.hasNext()) {
            BlockPos doorPos = it.next();
            if (doorPos.distSqr(sheikhPos) > 9.0) {
                BlockState state = this.sheikh.level().getBlockState(doorPos);
                if (state.getBlock() instanceof DoorBlock doorBlock && state.getValue(DoorBlock.OPEN)) {
                    doorBlock.setOpen(this.sheikh, this.sheikh.level(), state, doorPos, false);
                }
                it.remove();
            }
        }
    }
}
