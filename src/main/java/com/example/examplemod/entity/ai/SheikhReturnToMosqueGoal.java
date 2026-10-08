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

public class SheikhReturnToMosqueGoal extends Goal {
    private final SheikhEntity sheikh;
    private final double speedModifier;
    private int checkCooldown = 0;
    private final Set<BlockPos> openedDoors = new HashSet<>();

    public SheikhReturnToMosqueGoal(SheikhEntity sheikh, double speedModifier) {
        this.sheikh = sheikh;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.sheikh.getTarget() != null && this.sheikh.isAggressionTriggered()) {
            return false; // Prioritize combat if provoked
        }
        SheikhInteractionState state = this.sheikh.getInteractionState();
        if (state == SheikhInteractionState.WALKING_OUTSIDE || state == SheikhInteractionState.OUTSIDE_MOSQUE) {
            return false; // Do not interrupt outside trading session
        }

        BlockPos home = this.sheikh.getMosquePos();
        if (home == null) return false;

        if (state == SheikhInteractionState.RETURNING_TO_MOSQUE) {
            return this.sheikh.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > 4.0D;
        }

        // Inside mosque state: Return home if he wanders farther than 10 blocks away
        return this.sheikh.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > 100.0D;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.sheikh.getTarget() != null && this.sheikh.isAggressionTriggered()) {
            return false;
        }
        SheikhInteractionState state = this.sheikh.getInteractionState();
        if (state == SheikhInteractionState.WALKING_OUTSIDE || state == SheikhInteractionState.OUTSIDE_MOSQUE) {
            return false;
        }

        BlockPos home = this.sheikh.getMosquePos();
        if (home == null) return false;

        return !this.sheikh.getNavigation().isDone() && this.sheikh.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > 4.0D;
    }

    @Override
    public void start() {
        this.openedDoors.clear();
        BlockPos home = this.sheikh.getMosquePos();
        if (home != null) {
            this.sheikh.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, this.speedModifier);
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
        BlockPos home = this.sheikh.getMosquePos();
        if (home == null) return;

        // Auto-open doors as he enters the mosque
        this.handleDoorInteractions();

        double distSq = this.sheikh.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        if (distSq <= 4.0D || (this.sheikh.getInteractionState() == SheikhInteractionState.RETURNING_TO_MOSQUE && this.sheikh.isCurrentlyInsideMosque())) {
            this.sheikh.setInteractionState(SheikhInteractionState.INSIDE_MOSQUE);
            this.sheikh.setTargetOutsidePos(null);
            this.sheikh.getNavigation().stop();
            return;
        }

        if (--this.checkCooldown <= 0) {
            this.checkCooldown = 30; // Recalculate path periodically
            this.sheikh.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, this.speedModifier);
        }
    }

    private void handleDoorInteractions() {
        BlockPos sheikhPos = this.sheikh.blockPosition();

        // 1. Open any closed door within 2.5 blocks
        for (BlockPos p : BlockPos.betweenClosed(sheikhPos.offset(-2, -1, -2), sheikhPos.offset(2, 2, 2))) {
            BlockState state = this.sheikh.level().getBlockState(p);
            if (state.getBlock() instanceof DoorBlock doorBlock) {
                if (!state.getValue(DoorBlock.OPEN)) {
                    doorBlock.setOpen(this.sheikh, this.sheikh.level(), state, p, true);
                    this.openedDoors.add(p.immutable());
                }
            }
        }

        // 2. Close doors once farther than 3 blocks away
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
