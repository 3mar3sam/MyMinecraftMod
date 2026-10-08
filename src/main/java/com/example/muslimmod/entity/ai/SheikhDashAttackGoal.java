package com.example.muslimmod.entity.ai;

import com.example.muslimmod.ExampleMod;
import com.example.muslimmod.entity.SheikhEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SheikhDashAttackGoal extends Goal {
    private final SheikhEntity sheikh;
    private LivingEntity target;
    private int dashTicks = 0;
    private final Set<LivingEntity> hitList = new HashSet<>();

    public SheikhDashAttackGoal(SheikhEntity sheikh) {
        this.sheikh = sheikh;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sheikh.getTarget();
        return (this.sheikh.isCombatMode() || this.sheikh.isAggressionTriggered())
            && target != null 
            && target.isAlive() 
            && this.sheikh.dashCooldown <= 0 
            && this.sheikh.distanceTo(target) >= 0.8F 
            && this.sheikh.distanceTo(target) <= 24.0F;
    }

    @Override
    public void start() {
        this.target = this.sheikh.getTarget();
        if (this.target == null) return;

        this.dashTicks = 0;
        this.hitList.clear();
        this.sheikh.dashCooldown = 200; // 10.0 seconds cooldown
        this.sheikh.getLookControl().setLookAt(target, 30.0F, 30.0F);

        Vec3 dir = target.position().subtract(this.sheikh.position());
        Vec3 hDir = new Vec3(dir.x, 0, dir.z).normalize();
        this.sheikh.setDeltaMovement(hDir.x * 2.8, this.sheikh.getDeltaMovement().y, hDir.z * 2.8);
        this.sheikh.hurtMarked = true;

        this.sheikh.level().playSound(null, this.sheikh.blockPosition(), ExampleMod.DASH_SOUND.get(), SoundSource.HOSTILE, 1.2F, 1.0F);

        if (this.sheikh.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, this.sheikh.getX(), this.sheikh.getY() + 1.0D, this.sheikh.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.0D);
        }

        this.sheikh.setDashIntangible(2);
    }

    @Override
    public boolean canContinueToUse() {
        return this.dashTicks < 2; // Ultra-fast 2-tick burst (0.1s)
    }

    @Override
    public void tick() {
        this.dashTicks++;
        this.sheikh.setDashIntangible(Math.max(0, 2 - this.dashTicks));

        if (this.sheikh.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.sheikh.getX(), this.sheikh.getY() + 1.0D, this.sheikh.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        AABB sweepBox = this.sheikh.getBoundingBox()
            .minmax(this.sheikh.getBoundingBox().move(this.sheikh.getDeltaMovement()))
            .inflate(1.8D, 1.0D, 1.8D);

        List<LivingEntity> victims = this.sheikh.level().getEntitiesOfClass(LivingEntity.class, sweepBox, 
            e -> e != this.sheikh && e.isAlive() && !this.sheikh.isAlliedTo(e));

        boolean swung = false;
        for (LivingEntity victim : victims) {
            if (!hitList.contains(victim)) {
                victim.invulnerableTime = 0; // Bypass hurt resistance frames
                victim.hurt(this.sheikh.damageSources().mobAttack(this.sheikh), 22.0F);
                hitList.add(victim);

                if (!swung) {
                    this.sheikh.swing(InteractionHand.MAIN_HAND);
                    swung = true;
                }

                this.sheikh.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
                this.sheikh.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
                if (this.sheikh.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY() + victim.getBbHeight() / 2.0D, victim.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2.0D, victim.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.sheikh.setDeltaMovement(this.sheikh.getDeltaMovement().x * 0.05, this.sheikh.getDeltaMovement().y, this.sheikh.getDeltaMovement().z * 0.05);
        this.sheikh.hurtMarked = true;
        this.sheikh.setDashIntangible(0);
        this.hitList.clear();
    }
}
