package com.example.muslimmod.entity.ik.worm;

import com.example.muslimmod.entity.ModEntities;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WormHeadSegment extends WormSegment {
    private static final double HEAD_DAMAGE_MULTIPLIER = 2.0;
    private static final Vec3 HEAD_KNOCKBACK = new Vec3(5.0, 2.0, 5.0);

    public WormHeadSegment(EntityType<? extends WormHeadSegment> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public WormHeadSegment(Level level) {
        super(ModEntities.WORM_HEAD_SEGMENT.get(), level);
        this.noPhysics = true;
    }

    @Override
    public Vec3 getVisualScale() {
        Vec3 baseScale = super.getVisualScale();
        return baseScale.scale(1.35);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        Vec3 scale = this.getVisualScale();
        float width = (float) Math.max(scale.x, scale.z) * 1.5F;
        float height = (float) scale.y * 1.5F;
        return EntityDimensions.scalable(width, height);
    }

    @Override
    public float getLength() {
        return Math.max(super.getLength(), 1.75F * 1.5F);
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();

        if (this.parent == null) {
            this.parent = this.getOwner();
        }

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        Vec3 aimDir = null;
        if (this.getDirectionVector().lengthSqr() > 0.001) {
            aimDir = this.getDirectionVector();
        } else if (this.getDeltaMovement().lengthSqr() > 0.01) {
            aimDir = this.getDeltaMovement().normalize();
        } else if (this.parent != null && this.parent.aggroTargetEntity != null && this.parent.aggroTargetEntity.isAlive()) {
            aimDir = this.parent.aggroTargetEntity.getEyePosition().subtract(this.getEyePosition());
        }

        if (aimDir != null && aimDir.lengthSqr() > 0.0001) {
            double horizontalDist = Math.sqrt(aimDir.x * aimDir.x + aimDir.z * aimDir.z);
            float targetYaw = (float) (Mth.atan2(aimDir.z, aimDir.x) * (180.0F / (float) Math.PI)) - 90.0F;
            float targetPitch = (float) (-(Mth.atan2(aimDir.y, horizontalDist) * (180.0F / (float) Math.PI)));
            this.setYRot(Mth.rotLerp(0.3F, this.getYRot(), targetYaw));
            this.setXRot(Mth.rotLerp(0.3F, this.getXRot(), targetPitch));
        }
    }

    @Override
    protected double getDamage() {
        return super.getDamage() * HEAD_DAMAGE_MULTIPLIER;
    }

    @Override
    protected Vec3 getKB() {
        return HEAD_KNOCKBACK;
    }
}
