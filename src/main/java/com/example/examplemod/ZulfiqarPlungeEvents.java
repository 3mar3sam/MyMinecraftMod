package com.example.examplemod;

import com.example.examplemod.item.ZulfiqarItem;
import com.example.examplemod.network.PlungeAttackPayload;
import com.example.examplemod.network.PlungeStickPayload;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ZulfiqarPlungeEvents {

    private static final Set<UUID> plungingPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> stuckPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> stuckVictimIdMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> stuckRelativeOffsetMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> stuckFallbackPosMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> recentPlungeLanding = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> fallDamageApplied = new ConcurrentHashMap<>();

    public static boolean isPlunging(Player player) {
        if (player == null) return false;
        return plungingPlayers.contains(player.getUUID()) || stuckPlayers.contains(player.getUUID());
    }

    public static boolean isStuck(Player player) {
        if (player == null) return false;
        return stuckPlayers.contains(player.getUUID());
    }

    public static boolean hasGroundWithinDistance(Player player, double distance) {
        if (player == null || player.level() == null) {
            return false;
        }
        if (player.onGround()) {
            return true;
        }
        AABB groundBox = new AABB(
                player.getX() - 0.25D,
                player.getY() - distance,
                player.getZ() - 0.25D,
                player.getX() + 0.25D,
                player.getY() + 0.05D,
                player.getZ() + 0.25D
        );
        return player.level().getBlockCollisions(player, groundBox).iterator().hasNext();
    }

    /** Minimum vertical gap between feet and ground to start a plunge (2 blocks). */
    private static final double MIN_PLUNGE_HEIGHT = 2.0D;

    /** True when the player is at least two blocks above the nearest ground below them. */
    public static boolean isAtLeastTwoBlocksAboveGround(Player player) {
        if (player.onGround()) {
            return false;
        }
        // Ground within ~2 blocks below feet means the player has not cleared two full blocks yet.
        if (hasGroundWithinDistance(player, MIN_PLUNGE_HEIGHT - 0.0625D)) {
            return player.fallDistance >= MIN_PLUNGE_HEIGHT;
        }
        return true;
    }

    public static boolean canTriggerPlunge(Player player) {
        if (player == null || player.level() == null) {
            return false;
        }
        if (player.onGround() || player.isInWater() || player.isPassenger() || player.getAbilities().flying) {
            return false;
        }
        return isAtLeastTwoBlocksAboveGround(player);
    }

    public static boolean isMoreThanOneBlockAboveGround(Player player) {
        return canTriggerPlunge(player);
    }

    public static void setPlunging(Player player, boolean plunging) {
        if (player == null) return;
        if (plunging) {
            if (!canTriggerPlunge(player)) {
                return;
            }
            if (plungingPlayers.add(player.getUUID())) {
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                        ExampleMod.PLUNGE_SOUND.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
            }
        } else {
            plungingPlayers.remove(player.getUUID());
            recentPlungeLanding.put(player.getUUID(), player.level().getGameTime());

            if (stuckPlayers.remove(player.getUUID())) {
                player.noPhysics = false;
                Integer victimId = stuckVictimIdMap.remove(player.getUUID());
                stuckRelativeOffsetMap.remove(player.getUUID());
                stuckFallbackPosMap.remove(player.getUUID());

                Entity victim = victimId != null ? player.level().getEntity(victimId) : null;

                // Secondary release damage when player releases button
                if (victim != null && victim.isAlive()) {
                    float releaseDamage = 6.0F;
                    victim.hurt(player.damageSources().playerAttack(player), releaseDamage);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2.0D, victim.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY() + victim.getBbHeight() / 2.0D, victim.getZ(), 3, 0.1D, 0.1D, 0.1D, 0.0D);
                    }
                }

                // Pull back slightly from the enemy
                Vec3 pullDir;
                if (victim != null) {
                    Vec3 away = player.position().subtract(victim.position());
                    Vec3 horizontalAway = new Vec3(away.x, 0, away.z);
                    if (horizontalAway.lengthSqr() > 0.0001D) {
                        pullDir = horizontalAway.normalize().scale(0.35D).add(0, 0.35D, 0);
                    } else {
                        Vec3 look = player.getLookAngle();
                        pullDir = new Vec3(-look.x, 0, -look.z).normalize().scale(0.35D).add(0, 0.35D, 0);
                    }
                } else {
                    Vec3 look = player.getLookAngle();
                    pullDir = new Vec3(-look.x, 0, -look.z).normalize().scale(0.35D).add(0, 0.35D, 0);
                }

                player.setDeltaMovement(pullDir);
                player.hurtMarked = true;

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.2F);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 1.0F, 1.5F);

                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new PlungeStickPayload(0, false, 0, 0, 0));
                }
            }
        }
    }

    public static boolean hadRecentPlunge(Player player) {
        if (player == null) return false;
        Long time = recentPlungeLanding.get(player.getUUID());
        return time != null && (player.level().getGameTime() - time) <= 40L;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        UUID uuid = player.getUUID();

        if (stuckPlayers.contains(uuid)) {
            // Cancel if not holding Zulfiqar or dead
            if (!player.isAlive() || !player.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get())) {
                setPlunging(player, false);
                return;
            }

            Integer victimId = stuckVictimIdMap.get(uuid);
            Entity victim = victimId != null ? player.level().getEntity(victimId) : null;
            if (victim == null || !victim.isAlive() || victim.isRemoved()) {
                setPlunging(player, false);
                return;
            }

            Vec3 offset = stuckRelativeOffsetMap.get(uuid);
            double posX, posY, posZ;
            if (offset != null) {
                posX = victim.getX() + offset.x;
                posY = victim.getY() + offset.y;
                posZ = victim.getZ() + offset.z;
                stuckFallbackPosMap.put(uuid, new Vec3(posX, posY, posZ));
            } else {
                Vec3 fallback = stuckFallbackPosMap.getOrDefault(uuid, player.position());
                posX = fallback.x;
                posY = fallback.y;
                posZ = fallback.z;
            }

            player.noPhysics = true;
            player.setPos(posX, posY, posZ);
            player.setDeltaMovement(0, 0, 0);
            player.hurtMarked = true;
            player.resetFallDistance();
            player.fallDistance = 0.0F;
            return;
        }

        if (isPlunging(player)) {
            // Cancel if not holding Zulfiqar or in water
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get()) || player.isInWater()) {
                setPlunging(player, false);
                return;
            }

            // If player touched ground without hitting mob
            if (player.onGround()) {
                float landingFallDistance = player.fallDistance;
                long gameTime = player.level().getGameTime();
                player.resetFallDistance();
                player.fallDistance = 0.0F;
                setPlunging(player, false);
                if (landingFallDistance > 0.0F
                        && !gameTimeEquals(fallDamageApplied.get(uuid), gameTime)) {
                    player.causeFallDamage(landingFallDistance, 1.0F, player.damageSources().fall());
                }
                fallDamageApplied.remove(uuid);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.1D, player.getZ(), 10, 0.3D, 0.1D, 0.3D, 0.1D);
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);
                return;
            }

            // Direct hitbox contact scan
            AABB playerBox = player.getBoundingBox();
            Vec3 delta = player.getDeltaMovement();
            double expandY = Math.min(delta.y, -0.2D);
            AABB checkRange = playerBox.expandTowards(delta.x, expandY, delta.z).inflate(0.1D);

            List<Entity> candidates = player.level().getEntities(player, checkRange, e -> {
                if (e == player || !e.isAlive() || e.isPassengerOfSameVehicle(player)) {
                    return false;
                }
                if (e.isInvulnerable() || e.isInvulnerableTo(player.damageSources().playerAttack(player))) {
                    return false;
                }
                if (e instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    return false;
                }
                return checkRange.intersects(e.getBoundingBox());
            });

            if (!candidates.isEmpty()) {
                candidates.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(player)));
                Entity primaryVictim = candidates.get(0);

                AABB victimBox = primaryVictim.getBoundingBox();
                double victimTop = victimBox.maxY;
                double victimBottom = victimBox.minY;
                double victimHeight = primaryVictim.getBbHeight();
                double victimWidth = primaryVictim.getBbWidth();

                // 1. Calculate horizontal stab penetration coordinates (stabX, stabZ)
                double stabX;
                double stabZ;
                if (victimWidth <= 1.5D) {
                    // For standard humanoid / animal targets, center directly on victim core
                    stabX = primaryVictim.getX();
                    stabZ = primaryVictim.getZ();
                } else {
                    // For large/wide targets (e.g. WormSegment with 4.5m width), clamp penetration near core
                    double dx = player.getX() - primaryVictim.getX();
                    double dz = player.getZ() - primaryVictim.getZ();
                    double horizDist = Math.sqrt(dx * dx + dz * dz);
                    double maxHorizOffset = Math.min(victimWidth * 0.25D, 0.75D);
                    if (horizDist > maxHorizOffset && horizDist > 0.0001D) {
                        double scale = maxHorizOffset / horizDist;
                        stabX = primaryVictim.getX() + dx * scale;
                        stabZ = primaryVictim.getZ() + dz * scale;
                    } else {
                        stabX = player.getX();
                        stabZ = player.getZ();
                    }
                }

                // 2. Calculate vertical stab depth (stabY)
                // Sword hilt is held at ~player.getY() + 0.85D, blade extends down to ~player.getY() + 0.35D.
                // Sinking player feet to victimTop - 0.85D places hands right on entry wound, burying blade ~50-60cm deep!
                double stabY;
                if (victimHeight > 2.5D) {
                    // Giant mob (e.g. Worm boss segment)
                    double targetY = Math.min(victimTop - 0.85D, player.getY() - 0.6D);
                    stabY = Math.max(victimBottom + 0.3D, targetY);
                } else {
                    // Standard mob: sink blade deeply into upper body/head
                    stabY = Math.max(victimBottom, victimTop - 0.85D);
                }

                Vec3 stabPos = new Vec3(stabX, stabY, stabZ);
                Vec3 relativeOffset = stabPos.subtract(primaryVictim.position());

                ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (ZulfiqarItem.isBorrowed(weapon) && !ZulfiqarItem.isSandWorm(primaryVictim)) {
                    ZulfiqarItem.breakBorrowedSword(weapon, player);
                    setPlunging(player, false);
                    return;
                }

                float fallDist = Math.max(player.fallDistance, 1.0F);
                float damage = 16.0F + (fallDist * 2.0F); // Initial downward stab damage

                for (Entity victim : candidates) {
                    victim.hurt(player.damageSources().playerAttack(player), damage);
                }

                // Visceral puncture effects & sounds right at the entrance wound
                double woundY = Math.min(victimTop, stabY + 0.80D);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, stabX, woundY, stabZ, 25, 0.35D, 0.25D, 0.35D, 0.15D);
                    serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, stabX, woundY, stabZ, 12, 0.25D, 0.2D, 0.25D, 0.1D);
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, stabX, woundY, stabZ, 4, 0.15D, 0.1D, 0.15D, 0.0D);
                }

                // Heavy impale sounds
                player.level().playSound(null, stabX, woundY, stabZ,
                        ExampleMod.PIERCE_SOUND.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                player.level().playSound(null, stabX, woundY, stabZ,
                        SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.4F, 0.75F);
                player.level().playSound(null, stabX, woundY, stabZ,
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.2F, 0.9F);
                player.level().playSound(null, stabX, woundY, stabZ,
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 1.0F);

                weapon = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (!player.getAbilities().instabuild && weapon.is(ExampleMod.ZULFIQAR.get())) {
                    weapon.hurtAndBreak(2, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                }

                // Snap player into deep stab penetration position immediately
                player.noPhysics = true;
                player.setPos(stabX, stabY, stabZ);
                player.setDeltaMovement(0, 0, 0);
                player.hurtMarked = true;
                player.resetFallDistance();
                player.fallDistance = 0.0F;

                plungingPlayers.remove(uuid);
                stuckPlayers.add(uuid);
                stuckVictimIdMap.put(uuid, primaryVictim.getId());
                stuckRelativeOffsetMap.put(uuid, relativeOffset);
                stuckFallbackPosMap.put(uuid, stabPos);

                // Send sync packet to client with exact stab offset
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new PlungeStickPayload(
                            primaryVictim.getId(),
                            true,
                            relativeOffset.x,
                            relativeOffset.y,
                            relativeOffset.z
                    ));
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource().is(DamageTypes.FALL)) {
                fallDamageApplied.put(player.getUUID(), player.level().getGameTime());
            } else if (event.getSource().is(DamageTypes.IN_WALL)) {
                if (isStuck(player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean gameTimeEquals(Long recordedTime, long gameTime) {
        return recordedTime != null && recordedTime == gameTime;
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (isStuck(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (isStuck(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}
