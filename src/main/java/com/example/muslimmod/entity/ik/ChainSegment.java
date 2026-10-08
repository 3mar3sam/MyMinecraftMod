package com.example.muslimmod.entity.ik;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.example.muslimmod.entity.ik.worm.WormChainEntity;
import java.util.List;

public class ChainSegment extends Entity {
    private static final EntityDataAccessor<Vector3f> DIR_VEC = 
        SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> LENGTH = 
        SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> VISUAL_SCALE = 
        SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> UP_VEC = 
        SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);

    public WormChainEntity parent;
    public ChainSegment leadingSegment;
    public int hurtTime = 0;

    public ChainSegment(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LENGTH, 10.0F);
        builder.define(VISUAL_SCALE, new Vector3f(1.0F, 1.0F, 1.0F));
        builder.define(DIR_VEC, new Vector3f(0.0F, 2.0F, 0.0F));
        builder.define(UP_VEC, new Vector3f(0.0F, 1.0F, 0.0F));
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        if (this.hurtTime > 0) {
            this.hurtTime--;
        }
    }

    public float getLength() {
        return this.entityData.get(LENGTH);
    }

    public void setLength(float length) {
        this.entityData.set(LENGTH, length);
    }

    public Vec3 getVisualScale() {
        Vector3f scale = this.entityData.get(VISUAL_SCALE);
        return new Vec3(scale.x(), scale.y(), scale.z());
    }

    public void setVisualScale(Vec3 scaleVec) {
        this.entityData.set(VISUAL_SCALE, scaleVec.toVector3f());
    }

    public Vec3 getDirectionVector() {
        Vector3f dir = this.entityData.get(DIR_VEC);
        return new Vec3(dir.x(), dir.y(), dir.z());
    }

    public void setDirectionVector(Vec3 dirVec) {
        this.entityData.set(DIR_VEC, dirVec.normalize().toVector3f());
    }

    public Vec3 getUpVector() {
        Vector3f up = this.entityData.get(UP_VEC);
        return new Vec3(up.x(), up.y(), up.z());
    }

    public void setUpVector(Vec3 upVec) {
        this.entityData.set(UP_VEC, upVec.normalize().toVector3f());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(LENGTH, tag.getFloat("segment_length"));
        this.entityData.set(DIR_VEC, new Vector3f(
            tag.getFloat("dir_vec_x"),
            tag.getFloat("dir_vec_y"),
            tag.getFloat("dir_vec_z")
        ));
        this.entityData.set(VISUAL_SCALE, new Vector3f(
            tag.getFloat("scale_vec_x"),
            tag.getFloat("scale_vec_y"),
            tag.getFloat("scale_vec_z")
        ));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("segment_length", this.entityData.get(LENGTH));
        
        Vector3f dirVec = this.entityData.get(DIR_VEC);
        tag.putFloat("dir_vec_x", dirVec.x());
        tag.putFloat("dir_vec_y", dirVec.y());
        tag.putFloat("dir_vec_z", dirVec.z());

        Vector3f scaleVec = this.entityData.get(VISUAL_SCALE);
        tag.putFloat("scale_vec_x", scaleVec.x());
        tag.putFloat("scale_vec_y", scaleVec.y());
        tag.putFloat("scale_vec_z", scaleVec.z());
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return !this.isRemoved();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        this.hurtTime = 10;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 1.5F, 0.8F);

        // Forward damage to parent worm entity:
        if (this.parent == null || this.parent.isRemoved()) {
            if (this instanceof com.example.muslimmod.entity.ik.worm.WormSegment ws) {
                this.parent = ws.getOwner();
            } else {
                AABB searchBox = this.getBoundingBox().inflate(128.0);
                List<WormChainEntity> entities = this.level().getEntitiesOfClass(WormChainEntity.class, searchBox);
                if (!entities.isEmpty()) {
                    this.parent = entities.get(0);
                }
            }
        }

        if (this.parent != null && this.parent.isAlive() && !this.parent.isRemoved()) {
            return this.parent.hurt(source, amount);
        }
        return true;
    }
}
