package com.example.examplemod.entity.ik.worm;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ik.ChainSegment;
import com.example.examplemod.entity.ik.KinematicChainEntity;
import com.example.examplemod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class WormChainEntity extends KinematicChainEntity {
    private static final float SPEED_SCALE = 1.3F;
    private static final int MAX_HEALTH_HITS = 20;
    private static final int DEFAULT_DESPAWN_SECONDS = 30;

    private final double arenaRadius = 60.0;
    private BlockPos arenaCenter;

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.literal("Sand Worm"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private float health = 300.0F;
    private final float maxHealth = 300.0F;

    public float getHealth() {
        return this.health;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public void setHealth(float health) {
        this.health = Math.max(0.0F, Math.min(health, this.maxHealth));
    }

    // Three-attack system: logical cooldown between attacks (280 ticks = 14 seconds)
    private int attackCooldown = 330;
    private int activeAttack = 0; // 0 = Cruising, 1 = Inverted Eruption from below, 2 = High Dolphin Sky Dive, 3 = Horizontal Flank Sweep
    private int nextAttackType = 1; // 1 = Eruption, 2 = Dolphin Dive, 3 = Flank Sweep
    private int attackTicks = 0;
    private boolean isDiving = false;
    private int initialRoamTicks = 160;

    private boolean breaching = false;
    private int soundFrequencyCount = 0;
    public LivingEntity aggroTargetEntity;
    public boolean removed = false;
    private int discardTimer = 0;
    private int noTargetEscapeTimer = 0;
    private boolean escaping = false;
    private int noPlayerDiscardTimer = 0;
    private boolean isChasing = false;
    public int explodedTimes = 0;
    private WormHeadSegment head;
    public Vec3 thumperTarget = null;
    private int despawnTimer = 0;
    private LivingEntity lastAttacker = null;

    public WormChainEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    private void initWorm() {
        if (this.segmentCount == 0 && !this.level().isClientSide()) {
            for (int i = 0; i < 10; ++i) {
                float scaleFactor = (float) (i + 2) / 11.0F;
                this.addWormSegment(1.75F, new Vec3(1.0, 0.0, 0.0), new Vec3(7.5 * scaleFactor, 7.5 * scaleFactor, 5.0));
            }

            for (int i = 0; i < 80; ++i) {
                this.addWormSegment(1.75F, new Vec3(1.0, 0.0, 0.0), new Vec3(7.5, 7.5, 5.0));
            }

            this.addHeadSegment(1.75F * 1.5F, new Vec3(1.0, 0.0, 0.0), new Vec3(7.5, 7.5, 5.0));

            if (this.initialRoamTicks <= 0 && this.aggroTargetEntity == null && this.thumperTarget == null) {
                this.updateBossTarget();
            }

            Vec3 targetedObjectPos = this.getTargetedObjectPos();
            Vec3 lookAt = (targetedObjectPos != null) 
                ? targetedObjectPos.subtract(this.position()).normalize()
                : new Vec3(0.0, 0.0, 1.0);
            for (int i = 0; i < this.segmentCount && i < this.segments.size(); ++i) {
                ChainSegment seg = this.segments.get(i);
                if (seg != null && !seg.isRemoved()) {
                    seg.setDirectionVector(lookAt);
                }
            }
        }
    }

    private int segmentRetryTimer = 0;

    public boolean areSegmentsReady() {
        if (this.segmentCount <= 0 || this.segments.size() < this.segmentCount) {
            return false;
        }
        for (int i = 0; i < this.segmentCount; ++i) {
            ChainSegment seg = this.segments.get(i);
            if (seg == null || seg.isRemoved()) {
                return false;
            }
        }
        return true;
    }

    private void loadSavedSegments() {
        if (this.segmentCount == 0 || this.level().isClientSide()) {
            return;
        }

        while (this.segments.size() < this.segmentCount) {
            this.segments.add(null);
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean anyMissing = false;
        for (int i = 0; i < this.segmentCount && i < this.segmentsUUIDs.size(); ++i) {
            ChainSegment current = this.segments.get(i);
            if (current != null && !current.isRemoved()) {
                continue;
            }

            UUID uuid = this.segmentsUUIDs.get(i);
            if (uuid == null) {
                anyMissing = true;
                continue;
            }

            Entity entity = serverLevel.getEntity(uuid);
            ChainSegment seg = (entity instanceof ChainSegment cs && !cs.isRemoved()) ? cs : null;

            if (seg == null) {
                AABB searchBox = this.getBoundingBox().inflate(160.0);
                List<ChainSegment> nearby = serverLevel.getEntitiesOfClass(ChainSegment.class, searchBox);
                for (ChainSegment s : nearby) {
                    if (s.getUUID().equals(uuid) && !s.isRemoved()) {
                        seg = s;
                        break;
                    }
                }
            }

            if (seg != null) {
                seg.parent = this;
                if (seg instanceof WormHeadSegment headSeg) {
                    headSeg.setLength(Math.max(headSeg.getLength(), 1.75F * 1.5F));
                    this.head = headSeg;
                }
                this.segments.set(i, seg);
            } else {
                anyMissing = true;
            }
        }

        for (int i = 0; i < this.segments.size() - 1; ++i) {
            ChainSegment seg = this.segments.get(i);
            if (seg != null) {
                seg.leadingSegment = this.segments.get(i + 1);
            }
        }

        if (anyMissing) {
            segmentRetryTimer++;
            if (segmentRetryTimer > 100) {
                for (int i = 0; i < this.segmentCount; ++i) {
                    ChainSegment seg = this.segments.get(i);
                    if (seg == null || seg.isRemoved()) {
                        boolean isHead = (i == this.segmentCount - 1);
                        Vec3 dirVec = new Vec3(1.0, 0.0, 0.0);
                        Vec3 spawnPos = (i > 0 && this.segments.get(i - 1) != null)
                            ? this.segments.get(i - 1).position()
                            : this.position();

                        if (isHead) {
                            WormHeadSegment newHead = new WormHeadSegment(ModEntities.WORM_HEAD_SEGMENT.get(), this.level());
                            newHead.setLength(1.75F * 1.5F);
                            newHead.setDirectionVector(dirVec);
                            newHead.setVisualScale(new Vec3(7.5, 7.5, 5.0));
                            newHead.setOwnerUUID(this.getUUID());
                            newHead.parent = this;
                            newHead.setPos(spawnPos);
                            this.segments.set(i, newHead);
                            if (i < this.segmentsUUIDs.size()) {
                                this.segmentsUUIDs.set(i, newHead.getUUID());
                            } else {
                                this.segmentsUUIDs.add(newHead.getUUID());
                            }
                            this.level().addFreshEntity(newHead);
                            this.head = newHead;
                        } else {
                            float scaleFactor = (i < 10) ? (float) (i + 2) / 11.0F : 1.0F;
                            WormSegment newSeg = new WormSegment(ModEntities.WORM_SEGMENT.get(), this.level());
                            newSeg.setLength(1.75F);
                            newSeg.setDirectionVector(dirVec);
                            newSeg.setVisualScale(new Vec3(7.5 * scaleFactor, 7.5 * scaleFactor, 5.0));
                            newSeg.setOwnerUUID(this.getUUID());
                            newSeg.parent = this;
                            newSeg.setPos(spawnPos);
                            this.segments.set(i, newSeg);
                            if (i < this.segmentsUUIDs.size()) {
                                this.segmentsUUIDs.set(i, newSeg.getUUID());
                            } else {
                                this.segmentsUUIDs.add(newSeg.getUUID());
                            }
                            this.level().addFreshEntity(newSeg);
                        }
                    }
                }
                segmentRetryTimer = 0;
            }
        } else {
            segmentRetryTimer = 0;
        }
    }

    private int despawnBehavior() {
        return 0;
    }

    private void fikBehavior() {
        if (!this.segments.isEmpty()) {
            ChainSegment first = this.segments.get(0);
            int lastIdx = Math.min(this.segmentCount - 1, this.segments.size() - 1);
            ChainSegment last = lastIdx >= 0 ? this.segments.get(lastIdx) : null;

            for (int i = 0; i < this.segmentCount - 1 && i < this.segments.size() - 1; ++i) {
                ChainSegment seg = this.segments.get(i);
                if (seg != null && seg.leadingSegment == null) {
                    seg.leadingSegment = this.segments.get(i + 1);
                }
            }

            if (first != null && !first.isRemoved()) {
                this.setPos(first.position());
            }

            if (first != null && !first.isRemoved() && last != null && !last.isRemoved()) {
                Vec3 rootToEnd = last.position().subtract(first.position()).cross(new Vec3(0.0, 0.0, 1.0));
                for (ChainSegment seg : this.segments) {
                    if (seg != null && !seg.isRemoved()) {
                        seg.setUpVector(rootToEnd);
                    }
                }
            }
        }
    }

    private void vfxSfxBehavior() {
        if (this.head == null) return;

        boolean inGround = !this.level().noCollision(null, this.head.getBoundingBox());

        if (inGround) {
            Vec3 targetPos = this.getTargetedObjectPos();
            double distSq = (targetPos != null) ? targetPos.distanceToSqr(this.head.position()) : 400.0;
            float intensity = (float) Math.pow(1.0 + Math.pow(1.1, Math.sqrt(distSq) - 17.5), -1.0) + 0.2F;

            if ((float) this.soundFrequencyCount >= 10.0F - intensity * 10.0F) {
                this.level().playSound(null, this.head.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 4.0F * intensity, 0.6F);
                this.soundFrequencyCount = 0;
            } else {
                ++this.soundFrequencyCount;
            }

            int surfY = this.level().getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) this.head.getX(), (int) this.head.getZ()
            );

            // Sand-wave telegraph: simulate the ground rippling above the buried head.
            if (this.tickCount % 2 == 0 && this.level() instanceof ServerLevel serverLvl
                    && Math.abs(this.head.getY() - surfY) <= 8.0) {
                this.spawnUndergroundWave(serverLvl, surfY);
            }
        }

        boolean willBreach = this.predictBreach(this.level(), this.head);
        if (!this.breaching && willBreach) {
            this.breaching = true;
            Vec3 particlePos = this.head.position().add(this.head.getDirectionVector().scale(8.0));
            this.spawnSandEmergeParticles(particlePos);
            this.level().playSound(null, this.head.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5.0F, 0.7F);
        } else if (this.breaching && !willBreach) {
            this.breaching = false;
            Vec3 particlePos = this.head.position().add(this.head.getDirectionVector().scale(8.0));
            this.spawnSandEmergeParticles(particlePos);
            this.level().playSound(null, this.head.blockPosition(), SoundEvents.SAND_FALL, SoundSource.HOSTILE, 5.0F, 0.5F);
        }
    }

    private void spawnUndergroundWave(ServerLevel serverLevel, int surfaceY) {
        Vec3 movement = this.targetV;
        if (movement == null || movement.horizontalDistanceSqr() < 0.001) {
            movement = this.head.getDirectionVector();
        }

        Vec3 forward = new Vec3(movement.x, 0.0, movement.z);
        if (forward.lengthSqr() < 0.001) {
            forward = new Vec3(1.0, 0.0, 0.0);
        } else {
            forward = forward.normalize();
        }
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
        BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());

        // Several moving ridges make the buried head readable before it breaches.
        for (int ridge = -2; ridge <= 2; ridge++) {
            double forwardOffset = ridge * 2.2;
            Vec3 ridgeCenter = this.head.position().add(forward.scale(forwardOffset));
            for (int point = -2; point <= 2; point++) {
                double sideOffset = point * 1.8;
                Vec3 particlePos = ridgeCenter.add(side.scale(sideOffset));
                double crest = Math.max(0.0, 1.0 - Math.abs(sideOffset) / 4.0);
                double phase = (this.tickCount * 0.35) - ridge * 0.9;
                double lift = 0.12 + crest * (0.18 + 0.12 * Math.sin(phase));
                serverLevel.sendParticles(sand, particlePos.x, surfaceY + lift, particlePos.z,
                    2, 0.45, 0.08 + crest * 0.12, 0.45, 0.025);
            }
        }
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        if (!this.level().isClientSide()) {
            if (this.arenaCenter == null) {
                BlockPos pos = this.blockPosition();
                this.arenaCenter = (pos != null && !pos.equals(BlockPos.ZERO)) ? pos : new BlockPos(69, 56, 23);
            }
            this.bossEvent.setVisible(true);
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            if (this.level() instanceof ServerLevel serverLevel) {
                BlockPos center = this.arenaCenter != null ? this.arenaCenter : this.blockPosition();
                AABB bossBox = new AABB(center).inflate(85.0, 90.0, 85.0);
                List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, bossBox);
                for (ServerPlayer player : serverLevel.players()) {
                    if (nearbyPlayers.contains(player)) {
                        this.bossEvent.addPlayer(player);
                    } else {
                        this.bossEvent.removePlayer(player);
                    }
                }
            }

            this.loadSavedSegments();
            if (this.despawnBehavior() == 1) {
                return;
            }

            this.initWorm();
            if (!this.areSegmentsReady()) {
                return;
            }
            this.fikBehavior();

            if (!this.segments.isEmpty()) {
                int lastIdx = Math.min(this.segmentCount - 1, this.segments.size() - 1);
                ChainSegment lastSeg = lastIdx >= 0 ? this.segments.get(lastIdx) : null;
                if (this.head == null && lastSeg instanceof WormHeadSegment headSeg) {
                    this.head = headSeg;
                }

                if (this.head != null && !this.head.isRemoved()) {
                    if (this.initialRoamTicks > 0) {
                        this.initialRoamTicks--;
                        this.aggroTargetEntity = null;
                    } else {
                        this.updateBossTarget();
                    }

                    Vec3 targetPos = (this.aggroTargetEntity != null && this.aggroTargetEntity.isAlive()) 
                        ? this.aggroTargetEntity.position() 
                        : null;

                    int surfaceY = this.level().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 
                        (int) this.head.getX(), 
                        (int) this.head.getZ()
                    );

                    boolean inGround = !this.level().noCollision(null, this.head.getBoundingBox());

                    // Arena Leash Boundary Enforcement (60-block boundary)
                    double dx = this.head.getX() - this.arenaCenter.getX();
                    double dz = this.head.getZ() - this.arenaCenter.getZ();
                    double horizontalDist = Math.hypot(dx, dz);

                    if (horizontalDist > this.arenaRadius) {
                        // Cancel active sky leaps or dive pursuits
                        this.activeAttack = 0;
                        this.isDiving = false;
                        this.attackTicks = 0;
                        this.attackCooldown = 280;

                        // Direct target destination strictly towards (arenaCenter.getX(), this.head.getY(), arenaCenter.getZ())
                        this.goal = new Vec3(this.arenaCenter.getX(), this.head.getY(), this.arenaCenter.getZ());

                        // Interpolate the head's rotation and velocity smoothly inward so the body segments follow without snapping or desyncing
                        Vec3 toCenter = this.goal.subtract(this.head.position());
                        Vec3 inwardDir = new Vec3(toCenter.x, 0.0, toCenter.z);
                        if (inwardDir.lengthSqr() > 0.0001) {
                            inwardDir = inwardDir.normalize();
                        } else {
                            inwardDir = new Vec3(1.0, 0.0, 0.0);
                        }

                        Vec3 currentVel = (this.targetV != null && this.targetV.lengthSqr() > 0.001) 
                            ? this.targetV 
                            : inwardDir.scale(SPEED_SCALE);
                        Vec3 currentDir = currentVel.normalize();

                        Vec3 steerDir;
                        if (currentDir.dot(inwardDir) < -0.95) {
                            Vec3 perpendicular = new Vec3(-inwardDir.z, 0.0, inwardDir.x);
                            steerDir = currentDir.add(perpendicular.scale(0.35)).lerp(inwardDir, 0.12).normalize();
                        } else {
                            steerDir = currentDir.lerp(inwardDir, 0.12).normalize();
                        }

                        double currentSpeed = currentVel.length();
                        double targetSpeed = SPEED_SCALE;
                        double smoothSpeed = Mth.lerp(0.08, currentSpeed, targetSpeed);
                        this.targetV = steerDir.scale(smoothSpeed);

                        if (!inGround && this.head.getY() > surfaceY) {
                            this.targetV = this.targetV.add(0.0, -0.06, 0.0);
                        }

                        // Smoothly interpolate head direction vector and rotation angles
                        Vec3 headDir = this.head.getDirectionVector();
                        if (headDir != null && headDir.lengthSqr() > 0.0001) {
                            this.head.setDirectionVector(headDir.lerp(steerDir, 0.12).normalize());
                        } else {
                            this.head.setDirectionVector(steerDir);
                        }

                        float targetYaw = (float) (Mth.atan2(steerDir.z, steerDir.x) * (180.0F / (float) Math.PI)) - 90.0F;
                        float targetPitch = (float) (-(Mth.atan2(this.targetV.y, Math.hypot(this.targetV.x, this.targetV.z)) * (180.0F / (float) Math.PI)));
                        this.head.setYRot(Mth.rotLerp(0.12F, this.head.getYRot(), targetYaw));
                        this.head.setXRot(Mth.rotLerp(0.12F, this.head.getXRot(), targetPitch));
                    } else if (this.activeAttack == 1) {
                        // =========================================================================
                        // ATTACK 1: INVERTED HOMING SURGE FROM BELOW (نفس السقوط لكن معكوس من أسفل)
                        // (تغوص عميقاً ثم تنقض صاعدة بسرعة خارقة من أسفل اللاعب وتقذفه للسماء)
                        // =========================================================================
                        this.attackTicks++;

                        // Telegraph the eruption at the target's ground position before the burst.
                        if (targetPos != null && this.attackTicks <= 40 && this.level() instanceof ServerLevel serverLevel) {
                            int targetSurfaceY = this.level().getHeight(
                                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                Mth.floor(targetPos.x), Mth.floor(targetPos.z)
                            );
                            if (this.attackTicks % 2 == 0) {
                                serverLevel.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                                    targetPos.x, targetSurfaceY + 0.15D, targetPos.z,
                                    18, 1.6D, 0.15D, 1.6D, 0.08D
                                );
                            }
                        }

                        if (!this.isDiving) {
                            // Phase 1A: Homing surge directly UPWARD into the player from below!
                            if (targetPos != null) {
                                Vec3 toTarget = targetPos.subtract(this.head.position());
                                if (toTarget.lengthSqr() > 0.01) {
                                    Vec3 desiredVel = toTarget.normalize().scale(2.4);
                                    Vec3 steer = desiredVel.subtract(this.targetV);
                                    this.applyAcceleration(steer.scale(0.22));
                                }
                            } else {
                                this.applyAcceleration(new Vec3(0.0, 0.15, 0.0));
                            }

                            // If head breaks through surface or reaches player height, transition to airborne crest
                            if (targetPos != null && this.head.getY() >= targetPos.y - 1.0 || this.attackTicks >= 25) {
                                this.isDiving = true;
                            }
                        } else {
                            // Phase 1B: Airborne crest & gravity curve
                            if (!inGround) {
                                this.applyAcceleration(new Vec3(0.0, -0.065, 0.0));
                            }
                        }

                        // Player collision on upward burst: deals 20 damage and launches player into the sky!
                        if (this.attackTicks <= 22) {
                            AABB launchBox = this.head.getBoundingBox().inflate(6.0, 4.0, 6.0);
                            List<LivingEntity> hitEntities = this.level().getEntitiesOfClass(LivingEntity.class, launchBox, LivingEntity::isAlive);
                            for (LivingEntity target : hitEntities) {
                                if (target instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                                target.hurt(this.level().damageSources().generic(), 20.0F);
                                // Launch player high into the sky!
                                target.setDeltaMovement(new Vec3(target.getDeltaMovement().x * 0.2, 1.9, target.getDeltaMovement().z * 0.2));
                                target.hurtMarked = true;
                            }
                        }

                        // Re-entry / Dive back into ground after cresting (with lag-free smooth transition)
                        if (this.attackTicks > 25 && this.targetV.y < 0.0 && inGround) {
                            this.spawnSandEmergeParticles(this.head.position());
                            this.level().playSound(null, this.head.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5.0F, 0.65F);
                            this.level().playSound(null, this.head.blockPosition(), SoundEvents.SAND_FALL, SoundSource.HOSTILE, 4.0F, 0.5F);

                            // Smooth horizontal redirection: instantly eliminates the hitch/lag upon re-entry!
                            Vec3 forward = new Vec3(this.targetV.x, 0.0, this.targetV.z);
                            if (forward.lengthSqr() < 0.01) forward = this.head.getDirectionVector();
                            if (forward.lengthSqr() < 0.01) forward = new Vec3(1.0, 0.0, 0.0);
                            this.targetV = forward.normalize().scale(SPEED_SCALE).add(0.0, -0.15, 0.0);

                            // Reset attack state and set 14-second logical cooldown (280 ticks)
                            this.activeAttack = 0;
                            this.isDiving = false;
                            this.attackTicks = 0;
                            this.attackCooldown = 280;
                            this.goal = null;
                        } else if (this.attackTicks > 110) {
                            this.activeAttack = 0;
                            this.isDiving = false;
                            this.attackTicks = 0;
                            this.attackCooldown = 280;
                            this.goal = null;
                        }
                    } else if (this.activeAttack == 2) {
                        // =========================================================================
                        // ATTACK 2: HIGH DOLPHIN BREACH SKY DIVE (الانقضاض الشاهق من أعلى)
                        // (تخرج من مسافة بعيدة 40 بلوكة، تعلو 40 بلوكة في السماء، وتنفسخ نحو اللاعب)
                        // =========================================================================
                        this.attackTicks++;

                        // Detect crest / apex: vertical velocity drops or crest tick window reached
                        if (!this.isDiving && (this.targetV.y <= 0.1 || this.attackTicks >= 35)) {
                            this.isDiving = true;
                        }

                        if (!this.isDiving) {
                            // Phase 2A: High Majestic Ascent (reaches ~38-42 blocks high across long distance)
                            if (!inGround) {
                                this.applyAcceleration(new Vec3(0.0, -0.055, 0.0));
                            }
                            if (targetPos != null) {
                                Vec3 toTarget = targetPos.subtract(this.head.position());
                                Vec3 horizSteer = new Vec3(toTarget.x, 0.0, toTarget.z);
                                if (horizSteer.lengthSqr() > 1.0) {
                                    this.applyAcceleration(horizSteer.normalize().scale(0.012));
                                }
                            }
                        } else {
                            // Phase 2B: Predatory Dive / Swoop (الانقضاض الموجه) directly onto target
                            if (targetPos != null) {
                                Vec3 toTarget = targetPos.subtract(this.head.position());
                                double dist = toTarget.length();
                                if (dist > 0.001) {
                                    Vec3 desiredVel = toTarget.normalize().scale(2.4);
                                    Vec3 steer = desiredVel.subtract(this.targetV);
                                    this.applyAcceleration(steer.scale(0.20));
                                }
                            } else {
                                this.applyAcceleration(new Vec3(0.0, -0.065, 0.0));
                            }
                        }

                        // Phase 2C: Impact & Re-entry Slam (with lag-free smooth transition)
                        boolean hitTargetProximity = (targetPos != null && this.head.position().distanceTo(targetPos) <= 4.5);
                        if (this.isDiving && (this.attackTicks > 30 && this.targetV.y < 0.0 && inGround || hitTargetProximity)) {
                            this.spawnSandEmergeParticles(this.head.position());
                            this.level().playSound(null, this.head.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5.0F, 0.6F);
                            this.level().playSound(null, this.head.blockPosition(), SoundEvents.SAND_FALL, SoundSource.HOSTILE, 5.0F, 0.4F);

                            AABB splashBox = this.head.getBoundingBox().inflate(8.0);
                            List<LivingEntity> hitEntities = this.level().getEntitiesOfClass(LivingEntity.class, splashBox, LivingEntity::isAlive);
                            for (LivingEntity target : hitEntities) {
                                if (target instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                                target.hurt(this.level().damageSources().generic(), 35.0F);
                                Vec3 kb = target.position().subtract(this.head.position()).normalize();
                                target.setDeltaMovement(kb.scale(1.6).add(0.0, 0.6, 0.0));
                            }

                            // Smooth horizontal redirection: instantly eliminates the hitch/lag upon re-entry!
                            Vec3 forward = new Vec3(this.targetV.x, 0.0, this.targetV.z);
                            if (forward.lengthSqr() < 0.01) forward = this.head.getDirectionVector();
                            if (forward.lengthSqr() < 0.01) forward = new Vec3(1.0, 0.0, 0.0);
                            this.targetV = forward.normalize().scale(SPEED_SCALE).add(0.0, -0.15, 0.0);

                            // Finish leap and set 14-second logical cooldown (280 ticks)
                            this.activeAttack = 0;
                            this.isDiving = false;
                            this.attackTicks = 0;
                            this.attackCooldown = 280;
                            this.goal = null;
                        } else if (this.attackTicks > 140) {
                            this.activeAttack = 0;
                            this.isDiving = false;
                            this.attackTicks = 0;
                            this.attackCooldown = 280;
                            this.goal = null;
                        }
                    } else if (this.activeAttack == 3) {
                        // =========================================================================
                        // ATTACK 3: HORIZONTAL FLANK SWEEP / SIDE AMBUSH (الاجتياح الأفقي من الجانب)
                        // (هجوم أفقي مباغت من جانب اللاعب يكتسح الكثبان الرملية ويقذف اللاعب جانباً)
                        // =========================================================================
                        this.attackTicks++;

                        // Skim along the surface horizontally toward target
                        if (targetPos != null) {
                            Vec3 toTarget = targetPos.subtract(this.head.position());
                            if (toTarget.lengthSqr() > 0.01) {
                                Vec3 desiredVel = new Vec3(toTarget.x, (targetPos.y - this.head.getY()) * 0.4, toTarget.z).normalize().scale(2.3);
                                Vec3 steer = desiredVel.subtract(this.targetV);
                                this.applyAcceleration(steer.scale(0.18));
                            }
                        }

                        if (!inGround && this.head.getY() > surfaceY + 2.0) {
                            this.applyAcceleration(new Vec3(0.0, -0.06, 0.0));
                        }

                        // Sweeping hit: deals 30 damage and sideways knockback
                        AABB sweepBox = this.head.getBoundingBox().inflate(6.0, 3.5, 6.0);
                        List<LivingEntity> hitEntities = this.level().getEntitiesOfClass(LivingEntity.class, sweepBox, LivingEntity::isAlive);
                        for (LivingEntity target : hitEntities) {
                            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                            target.hurt(this.level().damageSources().generic(), 30.0F);
                            Vec3 sweepKb = new Vec3(this.targetV.x, 0.0, this.targetV.z).normalize();
                            target.setDeltaMovement(sweepKb.scale(1.8).add(0.0, 0.45, 0.0));
                            target.hurtMarked = true;
                        }

                        // Dive back into dunes on the other side
                        if (this.attackTicks > 25 && inGround) {
                            this.spawnSandEmergeParticles(this.head.position());
                            this.level().playSound(null, this.head.blockPosition(), SoundEvents.SAND_FALL, SoundSource.HOSTILE, 5.0F, 0.5F);

                            // Smooth horizontal redirection: eliminates hitch/lag
                            Vec3 forward = new Vec3(this.targetV.x, 0.0, this.targetV.z);
                            if (forward.lengthSqr() < 0.01) forward = this.head.getDirectionVector();
                            if (forward.lengthSqr() < 0.01) forward = new Vec3(1.0, 0.0, 0.0);
                            this.targetV = forward.normalize().scale(SPEED_SCALE).add(0.0, -0.15, 0.0);

                            this.activeAttack = 0;
                            this.attackTicks = 0;
                            this.attackCooldown = 280; // 14 seconds
                            this.goal = null;
                        } else if (this.attackTicks > 100) {
                            this.activeAttack = 0;
                            this.attackTicks = 0;
                            this.attackCooldown = 280;
                            this.goal = null;
                        }
                    } else {
                        // =========================================================================
                        // NORMAL UNDERGROUND CRUISING (14s logical cooldown between attacks)
                        // =========================================================================
                        if (this.attackCooldown > 0) {
                            this.attackCooldown--;
                        } else if (targetPos != null) {
                            // Cooldown elapsed! Launch the next attack in the 3-attack cycle
                            Vec3 toTarget = targetPos.subtract(this.head.position());
                            Vec3 horizDir = new Vec3(toTarget.x, 0.0, toTarget.z);
                            if (horizDir.lengthSqr() < 0.001) {
                                horizDir = new Vec3(1.0, 0.0, 0.0);
                            } else {
                                horizDir = horizDir.normalize();
                            }

                            if (this.nextAttackType == 1) {
                                // ATTACK 1: Inverted Eruption from below (homing upward from depth)
                                this.targetV = horizDir.scale(0.5).add(0.0, 2.35, 0.0);
                                this.activeAttack = 1;
                                this.isDiving = false;
                                this.attackTicks = 0;
                            } else if (this.nextAttackType == 2) {
                                // ATTACK 2: Long-Range Dolphin Breach Sky Dive (wide wavelength ~40 blocks away)
                                this.targetV = horizDir.scale(1.35).add(0.0, 2.15, 0.0);
                                this.activeAttack = 2;
                                this.isDiving = false;
                                this.attackTicks = 0;
                            } else {
                                // ATTACK 3: Horizontal Flank Sweep from side (fast surface skimming)
                                this.targetV = horizDir.scale(2.2).add(0.0, 0.45, 0.0);
                                this.activeAttack = 3;
                                this.attackTicks = 0;
                            }

                            // Advance exactly once per launched attack: 1 -> 2 -> 3 -> 1.
                            this.nextAttackType = this.nextAttackType % 3 + 1;
                        }

                        if (this.activeAttack == 0) {
                            // Underground cruising depth & positioning based on upcoming attack:
                            // If preparing for Eruption (1): stalk deep underneath at depth -14
                            // If preparing for Sky Dive (2): wide runway at depth -7, 42 blocks away
                            // If preparing for Flank Sweep (3): shallow flank at depth -5, 28 blocks away
                            double depthY;
                            double patrolRadius;
                            double speedFactor;

                            if (this.nextAttackType == 1) {
                                depthY = (targetPos != null) ? targetPos.y - 14.0 : surfaceY - 12.0;
                                patrolRadius = 8.0;
                                speedFactor = 0.05;
                            } else if (this.nextAttackType == 2) {
                                depthY = (targetPos != null) ? targetPos.y - 7.0 : surfaceY - 8.0;
                                patrolRadius = 42.0;
                                speedFactor = 0.035;
                            } else {
                                depthY = (targetPos != null) ? targetPos.y - 5.0 : surfaceY - 6.0;
                                patrolRadius = 28.0;
                                speedFactor = 0.04;
                            }

                            if (targetPos != null) {
                                if (this.goal == null || this.head.position().distanceTo(this.goal) < 8.0 || this.tickCount % 50 == 0) {
                                    double angle = this.tickCount * speedFactor;
                                    double wpX = targetPos.x + Math.cos(angle) * patrolRadius;
                                    double wpZ = targetPos.z + Math.sin(angle) * patrolRadius;
                                    this.goal = new Vec3(wpX, depthY, wpZ);
                                }

                                Vec3 steer = this.goal.subtract(this.head.position()).normalize();
                                this.applyAcceleration(steer.scale(0.08 * SPEED_SCALE));

                                if (this.head.getY() > depthY + 2.0) {
                                    this.applyAcceleration(new Vec3(0.0, -0.04 * SPEED_SCALE, 0.0));
                                } else if (this.head.getY() < depthY - 4.0) {
                                    this.applyAcceleration(new Vec3(0.0, 0.04 * SPEED_SCALE, 0.0));
                                }
                            } else {
                                // Atmospheric Circular Patrol within the 60-block sand arena
                                BlockPos center = this.arenaCenter != null ? this.arenaCenter : this.blockPosition();
                                double cx = center.getX() + 0.5;
                                double cz = center.getZ() + 0.5;
                                double circleRadius = 38.0; // Graceful wide patrol inside the 60-block arena

                                // Calculate tangent waypoint along circular path around arenaCenter
                                double currentAngle = Math.atan2(this.head.getZ() - cz, this.head.getX() - cx);
                                double nextAngle = currentAngle + 0.35; // Advance ~20 degrees ahead on the circle
                                double wpX = cx + Math.cos(nextAngle) * circleRadius;
                                double wpZ = cz + Math.sin(nextAngle) * circleRadius;

                                // Swimming depth beneath the arena sand dunes with gentle wave undulation
                                double waveY = Math.sin(this.tickCount * 0.05) * 2.0;
                                double targetY = (surfaceY - 6.0) + waveY;

                                this.goal = new Vec3(wpX, targetY, wpZ);

                                Vec3 toGoal = this.goal.subtract(this.head.position());
                                if (toGoal.lengthSqr() > 0.01) {
                                    Vec3 desiredVel = toGoal.normalize().scale(SPEED_SCALE);
                                    Vec3 steer = desiredVel.subtract(this.targetV);
                                    this.applyAcceleration(steer.scale(0.12));
                                }

                                // Depth maintenance
                                if (this.head.getY() > targetY + 2.0) {
                                    this.applyAcceleration(new Vec3(0.0, -0.04 * SPEED_SCALE, 0.0));
                                } else if (this.head.getY() < targetY - 2.0) {
                                    this.applyAcceleration(new Vec3(0.0, 0.04 * SPEED_SCALE, 0.0));
                                }
                            }
                        }
                    }

                    // Ensure targetV is valid and not zero
                    if (this.targetV == null || this.targetV.lengthSqr() < 0.001 || Double.isNaN(this.targetV.x)) {
                        Vec3 fwd = this.head.getDirectionVector();
                        if (fwd.lengthSqr() < 0.01) fwd = new Vec3(1.0, 0.0, 0.0);
                        this.targetV = fwd.normalize().scale(SPEED_SCALE * 0.7);
                    }

                    this.target = this.head.position().add(this.targetV);
                }

                this.vfxSfxBehavior();
                this.fabrik();
            }
        }
    }

    private void updateBossTarget() {
        BlockPos center = this.arenaCenter != null ? this.arenaCenter : this.blockPosition();
        AABB searchBox = new AABB(center).inflate(85.0, 90.0, 85.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
            LivingEntity.class, 
            searchBox, 
            LivingEntity::isAlive
        );

        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;
        boolean hasSurvivalPlayerNearby = false;

        // Clean up lastAttacker if dead or removed
        if (this.lastAttacker != null && (!this.lastAttacker.isAlive() || this.lastAttacker.isRemoved())) {
            this.lastAttacker = null;
        }

        // 1. Check for Survival / Adventure players descending into the arena basin
        for (LivingEntity e : entities) {
            if (e instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                hasSurvivalPlayerNearby = true;
                double dx = player.getX() - (center.getX() + 0.5);
                double dz = player.getZ() - (center.getZ() + 0.5);
                double distFromCenter = Math.hypot(dx, dz);
                // Player has descended into the arena basin (within 52 blocks of center and not high on surrounding cliffs)
                // or player directly damaged the worm
                boolean inBasin = distFromCenter <= 52.0 && player.getY() <= center.getY() + 14.0;
                boolean engagedWorm = (this.lastAttacker == player);

                if (inBasin || engagedWorm) {
                    double dist = this.distanceToSqr(player);
                    if (dist < closestDist) {
                        closestDist = dist;
                        bestTarget = player;
                    }
                }
            }
        }

        // 2. Only target other mobs if no survival player is observing from the mountains/arena
        // and the mob is inside the arena basin
        if (bestTarget == null && !hasSurvivalPlayerNearby) {
            for (LivingEntity e : entities) {
                if (e instanceof Player) {
                    continue;
                }
                double dx = e.getX() - (center.getX() + 0.5);
                double dz = e.getZ() - (center.getZ() + 0.5);
                if (Math.hypot(dx, dz) <= 45.0) {
                    double dist = this.distanceToSqr(e);
                    if (dist < closestDist) {
                        closestDist = dist;
                        bestTarget = e;
                    }
                }
            }
        }

        this.aggroTargetEntity = bestTarget;
    }

    private void retarget(float yRange) {
        this.updateBossTarget();
    }

    private boolean predictBreach(Level lvl, WormSegment headSegment) {
        Vec3 targetPos = this.getTargetedObjectPos();
        if (targetPos != null && targetPos.y - headSegment.getY() > 20.0) {
            return false;
        }
        Vec3 futurePos = headSegment.position().add(headSegment.getDirectionVector().scale(10.0));
        BlockPos futureBPos = BlockPos.containing(futurePos.x, futurePos.y, futurePos.z);
        return !lvl.getBlockState(futureBPos).isSolid() && !lvl.getBlockState(futureBPos).is(Blocks.SAND);
    }

    private void addWormSegment(float length, Vec3 dirVec, Vec3 scale) {
        if (!this.level().isClientSide()) {
            WormSegment segment = new WormSegment(ModEntities.WORM_SEGMENT.get(), this.level());
            segment.setLength(length);
            segment.setDirectionVector(dirVec);
            segment.setVisualScale(scale);
            segment.setOwnerUUID(this.getUUID());
            segment.parent = this;

            if (!this.segments.isEmpty()) {
                ChainSegment prev = this.segments.get(this.segments.size() - 1);
                if (prev != null) {
                    prev.leadingSegment = segment;
                }
            }

            Vec3 pos = this.segments.isEmpty() ? this.position() : this.segments.get(this.segments.size() - 1).position();
            segment.setPos(pos);
            this.segments.add(segment);
            this.segmentsUUIDs.add(segment.getUUID());
            this.level().addFreshEntity(segment);
            ++this.segmentCount;
        }
    }

    private void addHeadSegment(float length, Vec3 dirVec, Vec3 scale) {
        if (!this.level().isClientSide()) {
            if (this.segments.isEmpty()) return;

            WormHeadSegment headSegment = new WormHeadSegment(ModEntities.WORM_HEAD_SEGMENT.get(), this.level());
            headSegment.setLength(length);
            headSegment.setDirectionVector(dirVec);
            headSegment.setVisualScale(scale);
            headSegment.setOwnerUUID(this.getUUID());
            headSegment.parent = this;

            ChainSegment prev = this.segments.get(this.segments.size() - 1);
            if (prev != null) {
                prev.leadingSegment = headSegment;
            }

            Vec3 pos = this.segments.get(this.segments.size() - 1).position().add(dirVec.normalize().scale(length));
            headSegment.setPos(pos);
            this.segments.add(headSegment);
            this.segmentsUUIDs.add(headSegment.getUUID());
            this.level().addFreshEntity(headSegment);
            ++this.segmentCount;
            this.head = headSegment;
        }
    }

    @Override
    public void fabrik() {
        if (this.segments.isEmpty() || this.segmentCount <= 0) return;
        int lastIdx = Math.min(this.segmentCount - 1, this.segments.size() - 1);
        if (lastIdx < 0) return;
        ChainSegment endSegment = this.segments.get(lastIdx);
        if (endSegment == null || endSegment.isRemoved()) return;

        if (this.target == null || this.target.equals(Vec3.ZERO)) {
            this.target = endSegment.position();
        }

        for (int i = 0; Math.abs(this.target.subtract(endSegment.position()).length()) > this.tolerance && i <= 10; ++i) {
            this.fabrikForward();
        }
    }

    @Override
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
                    float dist = nextSegment.getLength();
                    if (nextSegment instanceof WormHeadSegment || i == this.segmentCount - 2) {
                        dist = Math.max(dist, 1.75F * 1.5F);
                    }
                    Vec3 nextTail = nextSegment.position().subtract(nextSegment.getDirectionVector().scale(dist));
                    currentSegment.setPos(nextTail);
                }
            }

            Vec3 moveDir = currentSegment.position().subtract(lastPosition);
            if (moveDir.lengthSqr() > 0.0001) {
                currentSegment.setDirectionVector(moveDir);
            }
        }
    }

    @Override
    public void applyAcceleration(Vec3 accel) {
        super.applyAcceleration(accel);
        float maxSpeed = (this.activeAttack != 0) ? 2.8F : SPEED_SCALE;
        if (this.targetV.length() > maxSpeed) {
            this.targetV = this.targetV.normalize().scale(maxSpeed);
        }
    }

    public void blastHit() {
        this.targetV = new Vec3(this.targetV.x * 0.075, this.targetV.y + 1.2, this.targetV.z * 0.075);
        ++this.explodedTimes;
        this.hurt(this.level().damageSources().generic(), this.getMaxHealth() / (float) MAX_HEALTH_HITS);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        if (!this.level().isClientSide()) {
            if (source.getEntity() instanceof LivingEntity living) {
                this.lastAttacker = living;
            }
            this.setHealth(this.getHealth() - amount);
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            if (this.getHealth() <= 0.0F) {
                this.die();
            }
        }
        return true;
    }

    public void die() {
        if (this.isRemoved()) return;
        if (this.level() instanceof ServerLevel serverLvl) {
            Vec3 pos = this.head != null ? this.head.position() : this.position();
            serverLvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 5, 1.0, 1.0, 1.0, 0.0);
            serverLvl.playSound(null, BlockPos.containing(pos.x, pos.y, pos.z), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5.0F, 0.7F);
            ItemEntity drop = new ItemEntity(this.level(), pos.x, pos.y, pos.z, new ItemStack(Items.NETHERITE_SCRAP, 2));
            this.level().addFreshEntity(drop);
            ItemEntity toothDrop = new ItemEntity(this.level(), pos.x, pos.y, pos.z, new ItemStack(ExampleMod.SAND_WORM_TOOTH.get(), 1));
            this.level().addFreshEntity(toothDrop);
        }
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);
        this.discard();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide()) {
            this.bossEvent.removeAllPlayers();
            this.bossEvent.setVisible(false);
            for (ChainSegment seg : this.segments) {
                if (seg != null) {
                    seg.discard();
                }
            }
        }
        super.remove(reason);
    }

    private void spawnSandEmergeParticles(Vec3 pos) {
        if (this.level() instanceof ServerLevel serverLvl) {
            serverLvl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                pos.x, pos.y, pos.z, 60, 2.0, 1.0, 2.0, 0.2);
            serverLvl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x, pos.y, pos.z, 20, 1.5, 0.5, 1.5, 0.05);
        }
    }

    private Vec3 getTargetedObjectPos() {
        return this.aggroTargetEntity != null ? this.aggroTargetEntity.position() : null;
    }

    private static boolean isDesertBiome(Entity entity) {
        return entity.level().getBiome(entity.blockPosition()).is(net.minecraft.world.level.biome.Biomes.DESERT) 
            || entity.level().getBiome(entity.blockPosition()).is(BiomeTags.IS_BADLANDS);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.removed = tag.getBoolean("removed");
        if (tag.contains("Health")) {
            this.setHealth(tag.getFloat("Health"));
        }
        if (tag.contains("ArenaCenterX")) {
            this.arenaCenter = new BlockPos(tag.getInt("ArenaCenterX"), tag.getInt("ArenaCenterY"), tag.getInt("ArenaCenterZ"));
        }
        if (tag.contains("InitialRoamTicks")) {
            this.initialRoamTicks = tag.getInt("InitialRoamTicks");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("removed", this.removed);
        tag.putFloat("Health", this.getHealth());
        tag.putInt("InitialRoamTicks", this.initialRoamTicks);
        if (this.arenaCenter != null) {
            tag.putInt("ArenaCenterX", this.arenaCenter.getX());
            tag.putInt("ArenaCenterY", this.arenaCenter.getY());
            tag.putInt("ArenaCenterZ", this.arenaCenter.getZ());
        }
    }

    public BlockPos getArenaCenter() {
        return this.arenaCenter;
    }

    public void setArenaCenter(BlockPos arenaCenter) {
        this.arenaCenter = arenaCenter;
    }

    public double getArenaRadius() {
        return this.arenaRadius;
    }

    public WormHeadSegment getHead() {
        return this.head;
    }

    public void setHead(WormHeadSegment head) {
        this.head = head;
    }
}
