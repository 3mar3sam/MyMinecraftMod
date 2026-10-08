package com.example.examplemod.entity.ik;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KinematicChainEntity extends Entity {
    public float tolerance = 0.01F;
    public Vec3 target = Vec3.ZERO;
    protected Vec3 targetV = Vec3.ZERO;
    public Vec3 goal = null;
    public int segmentCount = 0;
    public List<ChainSegment> segments = new ArrayList<>();
    public List<UUID> segmentsUUIDs = new ArrayList<>();
    public int stage = 0;

    public KinematicChainEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.segmentCount != 0 && this.segments.isEmpty()) {
                AABB searchBox = new AABB(
                    this.position().add(100.0, 100.0, 100.0),
                    this.position().add(-100.0, -100.0, -100.0)
                );
                List<ChainSegment> nearbySegments = this.level().getEntitiesOfClass(ChainSegment.class, searchBox);

                for (int i = 0; i < this.segmentCount; ++i) {
                    UUID uuid = this.segmentsUUIDs.get(i);
                    this.segments.add(nearbySegments.stream()
                        .filter(seg -> seg.getStringUUID().equals(uuid.toString()))
                        .findFirst()
                        .orElse(null));
                }
            }

            if (!this.segments.isEmpty()) {
                ChainSegment firstSeg = this.segments.get(0);
                int lastIdx = Math.min(this.segmentCount - 1, this.segments.size() - 1);
                ChainSegment lastSeg = lastIdx >= 0 ? this.segments.get(lastIdx) : null;

                if (firstSeg != null && !firstSeg.isRemoved()) {
                    this.setPos(firstSeg.position());
                }

                if (firstSeg != null && !firstSeg.isRemoved() && lastSeg != null && !lastSeg.isRemoved()) {
                    Vec3 rootToEnd = lastSeg.position()
                        .subtract(firstSeg.position())
                        .cross(new Vec3(0.0, 0.0, 1.0));

                    for (ChainSegment seg : this.segments) {
                        if (seg != null && !seg.isRemoved()) {
                            seg.setUpVector(rootToEnd);
                        }
                    }
                }
            }

            if (this.segmentCount == 0) {
                for (int i = 0; i < 100; ++i) {
                    this.addSegment(1.75F, new Vec3(0.0, 1.0, 0.0));
                }
            }
        }
    }

    public void fabrik() {
        float totalLength = 0.0F;
        float distToTarget = (float) this.target.subtract(this.position()).length();

        for (int i = 0; i < this.segmentCount && i < this.segments.size(); ++i) {
            ChainSegment seg = this.segments.get(i);
            if (seg != null && !seg.isRemoved()) {
                totalLength += seg.getLength();
            }
        }

        if (distToTarget > totalLength) {
            Vec3 rootToTarget = this.target.subtract(this.position()).normalize();

            for (int i = 0; i < this.segmentCount; ++i) {
                if (i >= this.segments.size()) continue;
                ChainSegment currentSegment = this.segments.get(i);
                if (currentSegment == null || currentSegment.isRemoved()) continue;

                Vec3 lastPosition = this.position();
                for (int prevIdx = i - 1; prevIdx >= 0; --prevIdx) {
                    if (prevIdx < this.segments.size()) {
                        ChainSegment prev = this.segments.get(prevIdx);
                        if (prev != null && !prev.isRemoved()) {
                            lastPosition = prev.position();
                            break;
                        }
                    }
                }
                currentSegment.setPos(lastPosition.add(rootToTarget.scale(currentSegment.getLength())));
                currentSegment.setDirectionVector(rootToTarget);
            }
        } else {
            for (int iter = 0; iter <= 10; ++iter) {
                int lastIdx = Math.min(this.segmentCount - 1, this.segments.size() - 1);
                if (lastIdx < 0) break;
                ChainSegment endSegment = this.segments.get(lastIdx);
                if (endSegment == null || endSegment.isRemoved()) break;
                if (Math.abs(this.target.subtract(endSegment.position()).length()) <= this.tolerance) {
                    break;
                }
                this.fabrikForward();
                this.fabrikBackward();
            }
        }
    }

    public void fabrikForward() {
        for (int i = this.segmentCount - 1; i >= 0; --i) {
            if (i >= this.segments.size()) continue;
            ChainSegment currentSegment = this.segments.get(i);
            if (currentSegment == null || currentSegment.isRemoved()) continue;

            Vec3 lastPosition = this.position();
            for (int prevIdx = i - 1; prevIdx >= 0; --prevIdx) {
                if (prevIdx < this.segments.size()) {
                    ChainSegment prev = this.segments.get(prevIdx);
                    if (prev != null && !prev.isRemoved()) {
                        lastPosition = prev.position();
                        break;
                    }
                }
            }

            if (i == this.segmentCount - 1) {
                currentSegment.setPos(this.target);
            } else {
                ChainSegment nextSegment = (i + 1 < this.segments.size()) ? this.segments.get(i + 1) : null;
                if (nextSegment != null && !nextSegment.isRemoved()) {
                    Vec3 nextTail = nextSegment.position().subtract(nextSegment.getDirectionVector().scale(nextSegment.getLength()));
                    currentSegment.setPos(nextTail);
                }
            }

            Vec3 diff = currentSegment.position().subtract(lastPosition);
            if (diff.lengthSqr() > 0.0001) {
                currentSegment.setDirectionVector(diff);
            }
        }
    }

    public void fabrikBackward() {
        for (int i = 0; i < this.segmentCount; ++i) {
            if (i >= this.segments.size()) continue;
            ChainSegment currentSegment = this.segments.get(i);
            if (currentSegment == null || currentSegment.isRemoved()) continue;

            Vec3 lastPosition = this.position();
            for (int prevIdx = i - 1; prevIdx >= 0; --prevIdx) {
                if (prevIdx < this.segments.size()) {
                    ChainSegment prev = this.segments.get(prevIdx);
                    if (prev != null && !prev.isRemoved()) {
                        lastPosition = prev.position();
                        break;
                    }
                }
            }

            Vec3 diff = currentSegment.position().subtract(lastPosition);
            if (diff.lengthSqr() > 0.0001) {
                currentSegment.setDirectionVector(diff);
            }
            currentSegment.setPos(lastPosition.add(currentSegment.getDirectionVector().scale(currentSegment.getLength())));
        }
    }

    protected void addSegment(float length, Vec3 dirVec) {
    }

    public void applyAcceleration(Vec3 accel) {
        this.targetV = this.targetV.add(accel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.segmentCount = tag.getInt("segment_count");
        this.segmentsUUIDs = new ArrayList<>();

        for (int i = 0; i < this.segmentCount; ++i) {
            this.segmentsUUIDs.add(tag.getUUID("segment_" + i));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("segment_count", this.segmentCount);

        for (int i = 0; i < this.segmentsUUIDs.size(); ++i) {
            tag.putUUID("segment_" + i, this.segmentsUUIDs.get(i));
        }
    }
}
