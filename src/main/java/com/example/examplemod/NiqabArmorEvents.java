package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import com.example.examplemod.entity.SheikhEntity;

import java.util.List;
import org.joml.Vector3f;

public class NiqabArmorEvents {

    private static final int DASH_DURATION_TICKS = 40;
    private static final int DASH_COOLDOWN_TICKS = 180;
    private static final double DASH_SPEED = 1.35D;
    private static final DustParticleOptions DASH_PARTICLES = new DustParticleOptions(
            new Vector3f(0.63F, 0.13F, 0.94F), 1.2F);
    private static final DustParticleOptions SHEMAGH_PARTICLES = new DustParticleOptions(
            new Vector3f(0.85F, 0.18F, 0.04F), 1.25F);
    private static final DustParticleOptions SHEMAGH_GOLD_PARTICLES = new DustParticleOptions(
            new Vector3f(1.0F, 0.68F, 0.08F), 1.1F);
            private static final DustParticleOptions SHEMAGH_CLOUD_PARTICLES = new DustParticleOptions(
            new Vector3f(0.72F, 0.42F, 0.16F), 1.8F);
            private static final DustParticleOptions NIQAB_CLOUD_PARTICLES = new DustParticleOptions(
                new Vector3f(0.34F, 0.08F, 0.72F), 1.8F);
    private static final TagKey<Biome> DESERT_BIOMES = TagKey.create(
            Registries.BIOME, ResourceLocation.withDefaultNamespace("is_desert"));
    private static final java.util.Map<java.util.UUID, DashState> ACTIVE_DASHES = new java.util.HashMap<>();

    public static final TagKey<EntityType<?>> BOSSES_TAG = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", "bosses"));

    public static boolean hasFullMuslimSet(Player player) {
        if (player == null) return false;
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.NIQAB_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ABAYA_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ABAYA_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ABAYA_BOOTS.get());
    }

    public static boolean hasDashArmor(Player player) {
        if (player == null) return false;
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.NIQAB_HELMET.get())
                || player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SHEMAGH.get());
    }

    private static boolean hasShemagh(Player player) {
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SHEMAGH.get());
    }

    private static boolean hasMaleArmorSet(Player player) {
        if (player == null) return false;
        return hasShemagh(player)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.THOBE_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.THOBE_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ISLAMIC_SANDALS.get());
    }

    private static boolean isDesert(Player player) {
        if (player == null) return false;
        var biome = player.level().getBiome(player.blockPosition());
        if (biome.is(DESERT_BIOMES)) {
            return true;
        }
        return biome.unwrapKey()
                .map(key -> key.location().getPath().contains("desert"))
                .orElse(false);
    }

    private static Item getDashCooldownItem(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SHEMAGH.get())
                ? ModItems.SHEMAGH.get()
                : ModItems.NIQAB_HELMET.get();
    }

    public static void startDash(Player player, Vec3 requestedDirection) {
        Item cooldownItem = getDashCooldownItem(player);
        if (!hasDashArmor(player) || player.isSpectator() || player.isPassenger()
                || player.getCooldowns().isOnCooldown(cooldownItem)) {
            return;
        }

        Vec3 direction = requestedDirection;
        if (direction.lengthSqr() < 1.0E-4D || direction.lengthSqr() > 2.0D) {
            return;
        }

        ACTIVE_DASHES.put(player.getUUID(), new DashState(direction.normalize(), DASH_DURATION_TICKS,
            player.getAbilities().mayfly, player.getAbilities().flying));
        player.getCooldowns().addCooldown(cooldownItem, DASH_COOLDOWN_TICKS);
        player.setNoGravity(true);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, DASH_DURATION_TICKS + 1, 0, true, false, false));
        player.setDeltaMovement(direction.normalize().scale(DASH_SPEED));
        spawnDashParticles(player, direction.normalize(), true);
        if (hasDashArmor(player)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ExampleMod.SHEMAGH_DASH_SOUND.get(), SoundSource.PLAYERS, 1.2F, 0.85F);
        }
        player.hurtMarked = true;
    }

    public static boolean isDashing(Player player) {
        return ACTIVE_DASHES.containsKey(player.getUUID());
    }

    public static void updateDash(Player player, Vec3 requestedDirection) {
        DashState dash = ACTIVE_DASHES.get(player.getUUID());
        if (dash == null || !hasDashArmor(player) || player.isSpectator() || player.isPassenger()) {
            return;
        }

        if (requestedDirection.lengthSqr() > 1.0E-4D && requestedDirection.lengthSqr() <= 1.01D) {
            dash.direction = requestedDirection.normalize();
        } else {
            dash.direction = Vec3.ZERO;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        DashState dash = ACTIVE_DASHES.get(player.getUUID());
        if (dash != null) {
            if (!hasDashArmor(player) || dash.ticksRemaining <= 0 || !player.isAlive()) {
                ACTIVE_DASHES.remove(player.getUUID());
                endDash(player, dash);
            } else {
                dash.ticksRemaining--;
                player.setNoGravity(true);
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 3, 0, true, false, false));
                Vec3 targetVelocity = dash.direction.scale(DASH_SPEED);
                player.setDeltaMovement(player.getDeltaMovement().lerp(targetVelocity, 0.35D));
                spawnDashParticles(player, dash.direction, false);
                player.hurtMarked = true;
            }
        }
        if (hasFullMuslimSet(player)) {
            // Ambient night vision and movement speed boost with visible = false
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, false));
        }
        if ((hasShemagh(player) || hasMaleArmorSet(player)) && isDesert(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false, false));
        }
    }

    private static void endDash(Player player, DashState dash) {
        player.setNoGravity(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.removeEffect(MobEffects.INVISIBILITY);
        player.getAbilities().mayfly = dash.hadMayfly;
        player.getAbilities().flying = dash.wasFlying && dash.hadMayfly;
        player.onUpdateAbilities();
    }

    private static void spawnDashParticles(Player player, Vec3 direction, boolean burst) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        double radius = burst ? 0.7D : 0.55D;
        int ringPoints = burst ? 16 : 5;
        double rotation = player.tickCount * 0.18D;
        double shadowY = player.getY() + 0.03D;
        for (int i = 0; i < ringPoints; i++) {
            double angle = rotation + (Math.PI * 2.0D * i / ringPoints);
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(particleFor(player, i), x, shadowY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        if (hasDashArmor(player)) {
            spawnDashCloud(serverLevel, player, burst);
        }

        Vec3 horizontalDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontalDirection.lengthSqr() > 1.0E-4D) {
            horizontalDirection = horizontalDirection.normalize();
            int trailPoints = burst ? 7 : 2;
            for (int i = 1; i <= trailPoints; i++) {
                Vec3 trailPosition = player.position().subtract(horizontalDirection.scale(i * 0.42D));
                serverLevel.sendParticles(particleFor(player, i + ringPoints), trailPosition.x, shadowY, trailPosition.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void spawnDashCloud(net.minecraft.server.level.ServerLevel serverLevel, Player player,
                                       boolean burst) {
        boolean shemagh = hasShemagh(player);
        DustParticleOptions cloudParticles = shemagh ? SHEMAGH_CLOUD_PARTICLES : NIQAB_CLOUD_PARTICLES;
        DustParticleOptions primaryParticles = shemagh ? SHEMAGH_PARTICLES : DASH_PARTICLES;
        DustParticleOptions accentParticles = shemagh ? SHEMAGH_GOLD_PARTICLES : DASH_PARTICLES;
        double centerY = player.getY() + player.getBbHeight() * 0.52D;
        int cloudCount = burst ? 34 : 10;
        serverLevel.sendParticles(cloudParticles, player.getX(), centerY, player.getZ(),
            cloudCount, 0.48D, 0.78D, 0.48D, 0.035D);
        serverLevel.sendParticles(primaryParticles, player.getX(), centerY + 0.1D, player.getZ(),
            burst ? 16 : 5, 0.62D, 0.9D, 0.62D, 0.025D);
        serverLevel.sendParticles(accentParticles, player.getX(), centerY + 0.2D, player.getZ(),
            burst ? 10 : 3, 0.7D, 0.95D, 0.7D, 0.02D);

        double angleStep = Math.PI * 2.0D / 8.0D;
        double orbitRadius = 0.62D;
        for (int i = 0; i < 8; i++) {
            double angle = player.tickCount * 0.16D + i * angleStep;
            double x = player.getX() + Math.cos(angle) * orbitRadius;
            double z = player.getZ() + Math.sin(angle) * orbitRadius;
            double y = player.getY() + 0.25D + (i % 4) * 0.48D;
                serverLevel.sendParticles(i % 2 == 0 ? cloudParticles : primaryParticles,
                x, y, z, 1, 0.08D, 0.16D, 0.08D, 0.01D);
        }
        }

    private static DustParticleOptions particleFor(Player player, int index) {
        if (!hasShemagh(player)) {
            return DASH_PARTICLES;
        }
        return (index & 1) == 0 ? SHEMAGH_PARTICLES : SHEMAGH_GOLD_PARTICLES;
    }

    private static final class DashState {
        private Vec3 direction;
        private int ticksRemaining;
        private final boolean hadMayfly;
        private final boolean wasFlying;

        private DashState(Vec3 direction, int ticksRemaining, boolean hadMayfly, boolean wasFlying) {
            this.direction = direction;
            this.ticksRemaining = ticksRemaining;
            this.hadMayfly = hadMayfly;
            this.wasFlying = wasFlying;
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target instanceof Player player && hasFullMuslimSet(player)) {
            LivingEntity attacker = event.getEntity();
            if (attacker != null) {
                // Allow attack if the player provoked/attacked this specific mob first
                if (attacker.getLastHurtByMob() == player) {
                    return;
                }

                // Allow attack if attacker is a boss
                if (attacker.getType().is(BOSSES_TAG)
                        || attacker.getType().is(Tags.EntityTypes.BOSSES)
                        || attacker.getType() == EntityType.WITHER
                        || attacker.getType() == EntityType.ENDER_DRAGON
                        || attacker.getType() == EntityType.ELDER_GUARDIAN
                        || attacker.getType() == EntityType.WARDEN) {
                    return;
                }

                // Allow attack if attacker is a Piglin or Zombified Piglin
                if (attacker instanceof AbstractPiglin || attacker.getType() == EntityType.ZOMBIFIED_PIGLIN) {
                    return;
                }

                // Otherwise, cancel targeting (pacifism rule)
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player && hasFullMuslimSet(player)) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player) {
                AABB searchBox = player.getBoundingBox().inflate(18.0D);
                List<Mob> defenders = player.level().getEntitiesOfClass(Mob.class, searchBox,
                        m -> (LivingEntity) m != attacker && (LivingEntity) m != player && m.isAlive() && !(m instanceof TamableAnimal tamed && tamed.isOwnedBy(attacker)));

                for (Mob defender : defenders) {
                    if (defender instanceof SheikhEntity sheikh) {
                        // Bypass the 50% health rule and rush to defend immediately
                        sheikh.triggerDefenseCombat(attacker);
                    } else {
                        defender.setTarget(attacker);
                        defender.setAggressive(true);
                    }
                }
            }
        }
    }
}

