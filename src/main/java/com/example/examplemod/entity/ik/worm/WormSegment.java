package com.example.examplemod.entity.ik.worm;

import com.example.examplemod.entity.ik.ChainSegment;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

import com.example.examplemod.entity.ModEntities;

public class WormSegment extends ChainSegment {
    private static final double DEFAULT_DAMAGE = 10.0;
    private static final Vec3 KNOCKBACK_FACTOR = new Vec3(3.0, 2.0, 3.0);

    private UUID ownerEntityUUID;
    private int discardTimer = 0;

    public WormSegment(EntityType<? extends WormSegment> entityType, Level level) {
        super(entityType, level);
    }

    public WormSegment(Level level) {
        super(ModEntities.WORM_SEGMENT.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!(this instanceof WormHeadSegment)) {
            Vec3 dir = null;
            if (this.leadingSegment != null) {
                dir = this.leadingSegment.position().subtract(this.position());
            } else if (this.getDirectionVector().lengthSqr() > 0.0001) {
                dir = this.getDirectionVector();
            } else {
                dir = new Vec3(this.getX() - this.xo, this.getY() - this.yo, this.getZ() - this.zo);
            }
            if (dir != null && dir.lengthSqr() > 0.0001) {
                double hDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                float segYaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0F / (float) Math.PI)) - 90.0F;
                float segPitch = (float) (-(Mth.atan2(dir.y, hDist) * (180.0F / (float) Math.PI)));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
                this.setYRot(segYaw);
                this.setXRot(segPitch);
            }
        }

        if (!this.level().isClientSide()) {
            WormChainEntity owner = this.getOwner();
            if (owner == null) {
                if (this.discardTimer < 300) {
                    ++this.discardTimer;
                } else {
                    this.discard();
                }
            } else {
                this.discardTimer = 0;
            }

            List<Entity> collidingEntities = this.level().getEntities(this, this.getBoundingBox());
            DamageSource source = this.level().damageSources().generic();

            for (Entity entity : collidingEntities) {
                if (entity instanceof LivingEntity target && !entity.getUUID().equals(this.ownerEntityUUID)) {
                    if (target.invulnerableTime == 0) {
                        Vec3 direction = target.position().subtract(this.position()).normalize();
                        target.hurt(source, (float) this.getDamage());

                        Vec3 kb = this.getKB();
                        target.setDeltaMovement(new Vec3(
                            direction.x * kb.x,
                            direction.y * kb.y,
                            direction.z * kb.z
                        ));
                    }
                }
            }
        }
    }

    protected double getDamage() {
        return DEFAULT_DAMAGE;
    }

    protected Vec3 getKB() {
        return KNOCKBACK_FACTOR;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerEntityUUID = uuid;
    }

    public void setOwnerEntityUUID(UUID uuid) {
        this.setOwnerUUID(uuid);
    }

    public UUID getOwnerUUID() {
        return this.ownerEntityUUID;
    }

    public WormChainEntity getOwner() {
        if (this.parent != null && !this.parent.isRemoved()) {
            return this.parent;
        }
        if (this.ownerEntityUUID == null) return null;

        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ownerEntityUUID);
            if (entity instanceof WormChainEntity worm && !worm.isRemoved()) {
                this.parent = worm;
                return worm;
            }
        }

        AABB searchBox = this.getBoundingBox().inflate(128.0);
        List<WormChainEntity> entities = this.level().getEntitiesOfClass(WormChainEntity.class, searchBox);
        for (WormChainEntity e : entities) {
            if (e.getUUID().equals(this.ownerEntityUUID) && !e.isRemoved()) {
                this.parent = e;
                return e;
            }
        }
        return null;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("chain_entity_UUID")) {
            this.ownerEntityUUID = tag.getUUID("chain_entity_UUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerEntityUUID != null) {
            tag.putUUID("chain_entity_UUID", this.ownerEntityUUID);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.parent == null) {
            this.parent = this.getOwner();
        }
        return super.hurt(source, amount);
    }
}
