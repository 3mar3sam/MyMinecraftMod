package com.example.examplemod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import com.example.examplemod.command.SheikhTradeCommand;
import com.example.examplemod.entity.SheikhEntity;

public class NeoForgeEvents {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SheikhTradeCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player
                && (event.getItem().is(Items.PORKCHOP) || event.getItem().is(Items.COOKED_PORKCHOP))) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 250, 0));
        }
    }

    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Long parryTime = ExampleMod.parryTimestamps.get(player.getUUID());
            if (parryTime != null) {
                long timeParrying = player.level().getGameTime() - parryTime;
                if (timeParrying <= 20) { // 20 ticks / 1 second window
                    if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get())) {
                        Entity directEntity = event.getSource().getDirectEntity();
                        boolean isProjectile = event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                                || directEntity instanceof Projectile;

                        if (isProjectile) {
                            Entity sourceEntity = event.getSource().getEntity();
                            Vec3 toSource = null;
                            if (sourceEntity != null) {
                                toSource = sourceEntity.position().subtract(player.position()).normalize();
                            } else if (directEntity != null) {
                                Vec3 delta = directEntity.getDeltaMovement();
                                if (delta.lengthSqr() > 0.0001D) {
                                    toSource = delta.scale(-1.0D).normalize();
                                } else {
                                    toSource = directEntity.position().subtract(player.position()).normalize();
                                }
                            }

                            if (toSource != null) {
                                Vec3 viewVector = player.getViewVector(1.0F);
                                if (viewVector.dot(toSource) > -0.2D) { // In front of player
                                    event.setCanceled(true);
                                    player.swing(InteractionHand.MAIN_HAND, true);
                                    player.level().playSound(null, player.blockPosition(), ExampleMod.PARRY_SOUND.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
                                    player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.1F);

                                    double kx = (sourceEntity != null ? sourceEntity.getX() : (directEntity != null ? directEntity.getX() : player.getX())) - player.getX();
                                    double kz = (sourceEntity != null ? sourceEntity.getZ() : (directEntity != null ? directEntity.getZ() : player.getZ())) - player.getZ();
                                    player.knockback(0.12F, kx, kz);
                                    player.hurtMarked = true;

                                    if (directEntity instanceof Projectile projectile) {
                                        Vec3 look = player.getViewVector(1.0F);
                                        projectile.setDeltaMovement(look.x * 1.5D, look.y * 1.5D + 0.1D, look.z * 1.5D);
                                        projectile.setYRot(player.getYRot());
                                        projectile.setXRot(player.getXRot());
                                        projectile.hasImpulse = true;
                                        if (projectile instanceof AbstractArrow arrow) {
                                            arrow.setOwner(player);
                                        }
                                    }

                                    if (player.level() instanceof ServerLevel serverLevel) {
                                        serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.25, 0.25, 0.25, 0.15);
                                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.getEntity() instanceof SheikhEntity sheikh) {
            if (sheikh.canParry() && sheikh.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get())) {
                Entity sourceEntity = event.getSource().getEntity();
                if (sourceEntity != null && sheikh.distanceToSqr(sourceEntity) < 16.0) { // melee range
                    if (sheikh.level().random.nextFloat() < 0.40f) { // 40% chance
                        event.setCanceled(true);
                        sheikh.setParryCooldown(30);
                        sheikh.swing(InteractionHand.MAIN_HAND, true);
                        sheikh.level().playSound(null, sheikh.blockPosition(), ExampleMod.PARRY_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
                        sheikh.knockback(0.12F, sourceEntity.getX() - sheikh.getX(), sourceEntity.getZ() - sheikh.getZ());
                        sheikh.hurtMarked = true;
                        if (sheikh.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CRIT, sheikh.getX(), sheikh.getY() + 1.0D, sheikh.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
                        }
                    }
                }
            }
        }
    }
}
