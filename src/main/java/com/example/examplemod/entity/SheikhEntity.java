package com.example.examplemod.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ai.SheikhDashAttackGoal;
import com.example.examplemod.entity.ai.SheikhHealGoal;
import com.example.examplemod.entity.ai.SheikhReturnToMosqueGoal;
import com.example.examplemod.entity.ai.SheikhWalkOutsideGoal;
import com.example.examplemod.item.ZulfiqarItem;
import com.example.examplemod.network.SheikhDialoguePayload;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.neoforge.network.PacketDistributor;

public class SheikhEntity extends AbstractVillager implements VillagerDataHolder {
    private int tradeLevel = 1;
    private int tradeXp = 0;
    private BlockPos mosquePos = null;

    private SheikhInteractionState interactionState = SheikhInteractionState.INSIDE_MOSQUE;
    private BlockPos targetOutsidePos = null;
    private BlockPos exitDoorPos = null;
    private int idleOutsideTicks = 0;
    private UUID tradingPartnerUUID = null;
    private boolean wasTrading = false;

    public SheikhEntity(EntityType<? extends SheikhEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
            groundNav.setCanPassDoors(true);
        }
    }

    public BlockPos getMosquePos() {
        return this.mosquePos;
    }

    public void setMosquePos(BlockPos pos) {
        this.mosquePos = pos;
    }

    public SheikhInteractionState getInteractionState() {
        return this.interactionState;
    }

    public void setInteractionState(SheikhInteractionState state) {
        this.interactionState = state;
    }

    public BlockPos getTargetOutsidePos() {
        return this.targetOutsidePos;
    }

    public void setTargetOutsidePos(BlockPos pos) {
        this.targetOutsidePos = pos;
    }

    public BlockPos getExitDoorPos() {
        return this.exitDoorPos;
    }

    public void setExitDoorPos(BlockPos pos) {
        this.exitDoorPos = pos;
    }

    @Override
    public boolean canRide(Entity vehicle) {
        return false; // Strictly prevent riding boats, minecarts, or any vehicle
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        return false;
    }

    private boolean combatMode = false;

    public boolean isCombatMode() {
        return this.combatMode;
    }

    public void setCombatMode(boolean combatMode) {
        this.combatMode = combatMode;
    }

    public boolean isAggressionTriggered() {
        return this.combatMode || (this.getHealth() <= (this.getMaxHealth() * 0.5F));
    }

    public void triggerDefenseCombat(LivingEntity aggressor) {
        this.setCombatMode(true); // Switch weapon/stance immediately regardless of current health
        this.setTarget(aggressor);
        this.setAggressive(true);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && !this.isAggressionTriggered()) {
            return;
        }
        super.setTarget(target);
        if (target == null) {
            this.combatMode = false;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Prevent despawning during worldgen or when player moves away
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(0, new SheikhDashAttackGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35D, false) {
            @Override
            public boolean canUse() {
                return (SheikhEntity.this.isCombatMode() || SheikhEntity.this.isAggressionTriggered()) && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return (SheikhEntity.this.isCombatMode() || SheikhEntity.this.isAggressionTriggered()) && super.canContinueToUse();
            }
            @Override
            protected int getAttackInterval() {
                return 8; // Rapid attacks: strikes every 8 ticks (0.4s) instead of standard 20 ticks
            }
        });
        this.goalSelector.addGoal(2, new SheikhWalkOutsideGoal(this, 1.1D));
        this.goalSelector.addGoal(2, new SheikhReturnToMosqueGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new FloatGoal(this));
        this.goalSelector.addGoal(4, new SheikhHealGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return SheikhEntity.this.isAggressionTriggered() && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return SheikhEntity.this.isAggressionTriggered() && super.canContinueToUse();
            }
        });
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // ZERO ambient villager noises
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PLAYER_HURT; // Neutral hurt sound, NO villager grunt
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null; // Silent on death
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return null; // Silent trade notify, NO villager grunt
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean yes) {
        return null; // Silent trade updated
    }

    @Override
    public VillagerData getVillagerData() {
        return new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, this.tradeLevel);
    }

    @Override
    public void setVillagerData(VillagerData data) {
        this.tradeLevel = data.getLevel();
    }

    public boolean isCurrentlyInsideMosque() {
        if (this.interactionState == SheikhInteractionState.OUTSIDE_MOSQUE) {
            return false;
        }

        if (this.targetOutsidePos != null && this.blockPosition().closerThan(this.targetOutsidePos, 4.0)) {
            return false;
        }

        if (this.level().canSeeSky(this.blockPosition()) || this.level().canSeeSky(this.blockPosition().above())) {
            return false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(
                    this.blockPosition(),
                    holder -> holder.is(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "masjid"))
            );
            if (start != null && start.isValid()) {
                return true;
            }
        }

        if (this.mosquePos != null && this.interactionState == SheikhInteractionState.INSIDE_MOSQUE) {
            double distSq = this.distanceToSqr(this.mosquePos.getX() + 0.5, this.mosquePos.getY(), this.mosquePos.getZ() + 0.5);
            if (distSq <= 16.0 * 16.0) {
                return true;
            }
        }

        return false;
    }

    public BlockPos findSuitableOutdoorLocation() {
        Level lvl = this.level();
        BlockPos origin = this.mosquePos != null ? this.mosquePos : this.blockPosition();

        // 1. Search for a door leading outside to the open sky/courtyard
        BlockPos bestDoor = null;
        BlockPos bestOutsideSpot = null;
        double minDistance = Double.MAX_VALUE;

        int searchRadius = 24;
        int verticalRadius = 6;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockPos candidateDoor = origin.offset(dx, dy, dz);
                    BlockState state = lvl.getBlockState(candidateDoor);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            for (int step = 1; step <= 10; step++) {
                                BlockPos checkPos = candidateDoor.relative(dir, step);
                                BlockState checkState = lvl.getBlockState(checkPos);
                                if (checkState.isSolid() || lvl.getBlockState(checkPos.above()).isSolid()) {
                                    break;
                                }

                                if (lvl.canSeeSky(checkPos) || lvl.canSeeSky(checkPos.above())) {
                                    BlockPos groundPos = checkPos.below();
                                    BlockState groundState = lvl.getBlockState(groundPos);
                                    if (groundState.isSolid() && !checkState.isSolid() && !lvl.getBlockState(checkPos.above()).isSolid()) {
                                        double dist = candidateDoor.distSqr(this.blockPosition());
                                        if (dist < minDistance) {
                                            minDistance = dist;
                                            bestDoor = candidateDoor.immutable();
                                            bestOutsideSpot = checkPos.immutable();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestDoor != null && bestOutsideSpot != null) {
            this.exitDoorPos = bestDoor;
            return bestOutsideSpot;
        }

        // 2. Fallback: Search in circle for nearest walkable position under the sky
        for (int r = 8; r <= 24; r += 4) {
            for (int angleDeg = 0; angleDeg < 360; angleDeg += 30) {
                double rad = Math.toRadians(angleDeg);
                int x = origin.getX() + (int) Math.round(r * Math.sin(rad));
                int z = origin.getZ() + (int) Math.round(r * Math.cos(rad));
                int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos candidate = new BlockPos(x, y, z);

                if (lvl.canSeeSky(candidate)
                        && lvl.getBlockState(candidate.below()).isSolid()
                        && !lvl.getBlockState(candidate).isSolid()
                        && !lvl.getBlockState(candidate.above()).isSolid()) {
                    return candidate;
                }
            }
        }

        return origin.offset(0, 0, 10);
    }

    public void handleDialogueResponse(Player player, boolean accept) {
        if (this.level().isClientSide()) return;

        if (accept) {
            this.tradingPartnerUUID = player.getUUID();
            this.targetOutsidePos = findSuitableOutdoorLocation();
            this.interactionState = SheikhInteractionState.WALKING_OUTSIDE;
            player.sendSystemMessage(Component.translatable("message.examplemod.sheikh.walking_outside"));
            this.getNavigation().moveTo(
                    this.targetOutsidePos.getX() + 0.5,
                    this.targetOutsidePos.getY(),
                    this.targetOutsidePos.getZ() + 0.5,
                    1.1D
            );
        } else {
            player.sendSystemMessage(Component.translatable("message.examplemod.sheikh.decline_outside"));
            this.interactionState = SheikhInteractionState.INSIDE_MOSQUE;
        }
    }

    public static CompoundTag getPlayerQuestData(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, 10)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static boolean isQuestAccepted(Player player) {
        if (player.getPersistentData().getBoolean("sand_worm_quest_accepted")) {
            return true;
        }
        CompoundTag tag = getPlayerQuestData(player);
        return tag.getBoolean("worm_quest_accepted") || tag.getBoolean("sand_worm_quest_accepted");
    }

    public static void setQuestAccepted(Player player, boolean accepted) {
        player.getPersistentData().putBoolean("sand_worm_quest_accepted", accepted);
        getPlayerQuestData(player).putBoolean("worm_quest_accepted", accepted);
        getPlayerQuestData(player).putBoolean("sand_worm_quest_accepted", accepted);
    }

    public static boolean isQuestCompleted(Player player) {
        if (player.getPersistentData().getBoolean("sand_worm_quest_completed")) {
            return true;
        }
        CompoundTag tag = getPlayerQuestData(player);
        return tag.getBoolean("worm_quest_completed") || tag.getBoolean("sand_worm_quest_completed");
    }

    public static void setQuestCompleted(Player player, boolean completed) {
        player.getPersistentData().putBoolean("sand_worm_quest_completed", completed);
        getPlayerQuestData(player).putBoolean("worm_quest_completed", completed);
        getPlayerQuestData(player).putBoolean("sand_worm_quest_completed", completed);
    }

    public static boolean hasPermanentZulfiqar(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ExampleMod.ZULFIQAR.get()) && !ZulfiqarItem.isBorrowed(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBorrowedZulfiqar(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ExampleMod.ZULFIQAR.get()) && ZulfiqarItem.isBorrowed(stack)) {
                return true;
            }
        }
        return false;
    }

    public static int findItemSlot(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    public static BlockPos findSandWormArena(ServerLevel serverLevel, BlockPos originPos) {
        ResourceLocation structId = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sand_worm_arena");
        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, structId);
        BlockPos found = serverLevel.findNearestMapStructure(tag, originPos, 100, false);
        if (found != null) {
            return found;
        }

        var registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var holderOpt = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, structId));
        if (holderOpt.isPresent()) {
            var pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                    serverLevel, HolderSet.direct(holderOpt.get()), originPos, 100, false
            );
            if (pair != null) {
                return pair.getFirst();
            }
        }
        return null;
    }

    public static ItemStack createSandWormArenaMap(ServerLevel serverLevel, BlockPos originPos) {
        BlockPos targetPos = findSandWormArena(serverLevel, originPos);
        if (targetPos == null) {
            targetPos = originPos.offset(350, 0, 350);
        }

        ItemStack mapStack = MapItem.create(serverLevel, targetPos.getX(), targetPos.getZ(), (byte) 3, true, true);
        MapItem.renderBiomePreviewMap(serverLevel, mapStack);
        MapItemSavedData.addTargetDecoration(mapStack, targetPos, "+", MapDecorationTypes.RED_X);
        mapStack.set(DataComponents.ITEM_NAME, Component.literal("§6خريطة مكمن دودة الرمال"));

        int dx = targetPos.getX() - originPos.getX();
        int dz = targetPos.getZ() - originPos.getZ();
        int approxDist = (int) Math.round(Math.sqrt(dx * dx + dz * dz));

        List<Component> loreLines = List.of(
            Component.literal("§7الموقع: §fX: " + targetPos.getX() + ", Z: " + targetPos.getZ()),
            Component.literal("§7المسافة التقريبية: §e" + approxDist + " بلوك"),
            Component.literal("§8استرشد بها للعثور على عرين الوحش المدفون.")
        );
        mapStack.set(DataComponents.LORE, new ItemLore(loreLines));

        return mapStack;
    }

    public void handleQuestResponse(Player player, boolean accept) {
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel serverLevel)) return;

        CompoundTag questData = getPlayerQuestData(player);
        if (accept) {
            setQuestAccepted(player, true);
            getPlayerQuestData(player).putBoolean("worm_quest_offered", false);

            ItemStack borrowedSword = new ItemStack(ExampleMod.ZULFIQAR.get());
            ZulfiqarItem.setBorrowed(borrowedSword, true);

            ItemStack explorerMap = createSandWormArenaMap(serverLevel, this.blockPosition());

            if (!player.getInventory().add(borrowedSword)) {
                player.drop(borrowedSword, false);
            }
            if (!player.getInventory().add(explorerMap)) {
                player.drop(explorerMap, false);
            }

            BlockPos targetPos = findSandWormArena(serverLevel, this.blockPosition());
            if (targetPos == null) {
                targetPos = this.blockPosition().offset(350, 0, 350);
            }
            int dx = targetPos.getX() - player.getBlockX();
            int dz = targetPos.getZ() - player.getBlockZ();
            int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz));

            player.sendSystemMessage(Component.literal("§6[الشيخ] §eقبلت العهد! خذ السيف والخريطة واسترشد بها إلى أعماق الصحراء."));
            player.sendSystemMessage(Component.literal("§6[الشيخ] §7إحداثيات المكمن: §fX: " + targetPos.getX() + " | Z: " + targetPos.getZ() + " §8(يبعد حوالي §e" + dist + "§8 بلوك)"));
            this.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            questData.putBoolean("worm_quest_offered", false);
            player.sendSystemMessage(Component.literal("§7[الشيخ] كما تشاء. أعاننا الله وإياك."));
        }
    }

    public void onArrivedOutside() {
        this.interactionState = SheikhInteractionState.OUTSIDE_MOSQUE;
        this.idleOutsideTicks = 0;
        this.getNavigation().stop();
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

        if (this.tradingPartnerUUID != null) {
            Player partner = this.level().getPlayerByUUID(this.tradingPartnerUUID);
            if (partner != null && partner.distanceToSqr(this) < 400.0D) {
                partner.sendSystemMessage(Component.translatable("message.examplemod.sheikh.arrived_outside"));
                this.lookAt(partner, 30.0F, 30.0F);
                return;
            }
        }

        for (Player p : this.level().players()) {
            if (p.distanceToSqr(this) < 256.0D) {
                p.sendSystemMessage(Component.translatable("message.examplemod.sheikh.arrived_outside"));
            }
        }
    }

    public void onWalkOutsideFailed() {
        this.interactionState = SheikhInteractionState.RETURNING_TO_MOSQUE;
        this.targetOutsidePos = null;
        if (this.tradingPartnerUUID != null) {
            Player partner = this.level().getPlayerByUUID(this.tradingPartnerUUID);
            if (partner != null) {
                partner.sendSystemMessage(Component.translatable("message.examplemod.sheikh.return_inside"));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mosquePos == null && !this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(
                        this.blockPosition(),
                        holder -> holder.is(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "masjid"))
                );
                if (start != null && start.isValid()) {
                    this.mosquePos = this.blockPosition();
                    this.interactionState = SheikhInteractionState.INSIDE_MOSQUE;
                } else {
                    this.interactionState = SheikhInteractionState.OUTSIDE_MOSQUE;
                }
            }
        }
        if (this.getTarget() != null && !this.isAggressionTriggered()) {
            this.setTarget(null);
        }
        if (this.getTarget() == null || !this.getTarget().isAlive()) {
            this.combatMode = false;
        }

        // Failsafe: if somehow trading while inside mosque, immediately terminate trade
        if (!this.level().isClientSide() && this.isTrading() && this.isCurrentlyInsideMosque()) {
            this.setTradingPlayer(null);
        }
    }

    private int dashIntangibleTicks = 0;
    private int parryCooldown = 0;
    public int dashCooldown = 0;

    public void setDashIntangible(int ticks) {
        this.dashIntangibleTicks = ticks;
    }

    public boolean isDashIntangible() {
        return this.dashIntangibleTicks > 0;
    }

    public boolean canParry() {
        return this.parryCooldown <= 0;
    }

    public void setParryCooldown(int ticks) {
        this.parryCooldown = ticks;
    }

    @Override
    public boolean isPushable() {
        return !isDashIntangible() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (!isDashIntangible()) {
            super.push(entity);
        }
    }

    @Override
    protected void doPush(Entity entity) {
        if (!isDashIntangible()) {
            super.doPush(entity);
        }
    }

    @Override
    protected void pushEntities() {
        if (!isDashIntangible()) {
            super.pushEntities();
        }
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (dashIntangibleTicks > 0) {
            dashIntangibleTicks--;
            this.noPhysics = false;
        }
        if (this.parryCooldown > 0) {
            this.parryCooldown--;
        }
        if (this.dashCooldown > 0) {
            this.dashCooldown--;
        }
        boolean shouldDrawSword = (this.getTarget() != null && this.isAlive() && this.isAggressionTriggered());
        if (shouldDrawSword) {
            if (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(ExampleMod.ZULFIQAR.get())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ExampleMod.ZULFIQAR.get()));
                this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            }
        } else if (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(ExampleMod.MISWAK.get())) {
            if (!this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }

        // Handle lifecycle of OUTSIDE_MOSQUE state
        if (this.interactionState == SheikhInteractionState.OUTSIDE_MOSQUE) {
            if (this.isTrading()) {
                this.wasTrading = true;
                this.idleOutsideTicks = 0;
            } else {
                this.idleOutsideTicks++;
                // If trade window closed and 5 seconds passed, or idle for 30 seconds
                if ((this.wasTrading && this.idleOutsideTicks > 100) || this.idleOutsideTicks > 600) {
                    this.wasTrading = false;
                    this.interactionState = SheikhInteractionState.RETURNING_TO_MOSQUE;
                    this.targetOutsidePos = null;
                    if (this.tradingPartnerUUID != null) {
                        Player partner = this.level().getPlayerByUUID(this.tradingPartnerUUID);
                        if (partner != null && partner.distanceToSqr(this) < 256.0D) {
                            partner.sendSystemMessage(Component.translatable("message.examplemod.sheikh.return_inside"));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.hurt(this.damageSources().mobAttack(this), damage);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.setTarget(attacker);
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TradeLevel", this.tradeLevel);
        tag.putInt("TradeXp", this.tradeXp);
        if (this.mosquePos != null) {
            tag.putInt("MosqueX", this.mosquePos.getX());
            tag.putInt("MosqueY", this.mosquePos.getY());
            tag.putInt("MosqueZ", this.mosquePos.getZ());
        }
        tag.putString("InteractionState", this.interactionState.name());
        if (this.targetOutsidePos != null) {
            tag.putInt("OutsideX", this.targetOutsidePos.getX());
            tag.putInt("OutsideY", this.targetOutsidePos.getY());
            tag.putInt("OutsideZ", this.targetOutsidePos.getZ());
        }
        if (this.tradingPartnerUUID != null) {
            tag.putUUID("TradingPartner", this.tradingPartnerUUID);
        }
        tag.putInt("IdleOutsideTicks", this.idleOutsideTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TradeLevel")) {
            this.tradeLevel = tag.getInt("TradeLevel");
        }
        this.getOffers().removeIf(offer -> offer.getResult().is(ExampleMod.ZULFIQAR.get()));
        if (tag.contains("TradeXp")) {
            this.tradeXp = tag.getInt("TradeXp");
        }
        if (tag.contains("MosqueX")) {
            this.mosquePos = new BlockPos(tag.getInt("MosqueX"), tag.getInt("MosqueY"), tag.getInt("MosqueZ"));
        }
        if (tag.contains("InteractionState")) {
            try {
                this.interactionState = SheikhInteractionState.valueOf(tag.getString("InteractionState"));
            } catch (Exception ignored) {
                this.interactionState = SheikhInteractionState.INSIDE_MOSQUE;
            }
        }
        if (tag.contains("OutsideX")) {
            this.targetOutsidePos = new BlockPos(tag.getInt("OutsideX"), tag.getInt("OutsideY"), tag.getInt("OutsideZ"));
        }
        if (tag.hasUUID("TradingPartner")) {
            this.tradingPartnerUUID = tag.getUUID("TradingPartner");
        }
        if (tag.contains("IdleOutsideTicks")) {
            this.idleOutsideTicks = tag.getInt("IdleOutsideTicks");
        }
    }

    @Override
    public int getVillagerXp() {
        return this.tradeXp;
    }

    @Override
    public void overrideXp(int xp) {
        this.tradeXp = xp;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        super.notifyTrade(offer);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public InteractionResult openTradingScreen(Player player, InteractionHand hand) {
        this.idleOutsideTicks = 0;
        InteractionResult superResult = super.mobInteract(player, hand);
        if (superResult.consumesAction()) {
            return superResult;
        }

        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.level().isClientSide()) {
                if (this.getOffers().isEmpty()) {
                    return InteractionResult.CONSUME;
                }

                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return superResult;
    }

    public void triggerQuestVow(Player player) {
        MutableComponent vowMsg = Component.literal("§6[الشيخ] يا هذا، إن في أعماق الصحراء دودة عظيمة قد أضرت بالناس وأهلكت الحرث. عندي سيف متوارث عن أجدادي أعطيك إياه عارية بشرط: ألا تستعمله إلا في مواجهة تلك الدودة، فإن هزمتها وأتيتني بسنها صار لك للأبد. فهل تقبل العهد؟\n")
                .append(Component.literal("[✔ قبلت العهد]")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GREEN)
                                .withBold(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sheikh accept_quest " + this.getId() + " true"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("قبول العهد واستلام السيف والخريطة")))))
                .append("   ")
                .append(Component.literal("[✖ أرفض]")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.RED)
                                .withBold(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sheikh accept_quest " + this.getId() + " false"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("رفض العهد")))));

        player.sendSystemMessage(vowMsg);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SheikhDialoguePayload(this.getId(), this.getDisplayName().getString(), SheikhDialoguePayload.TYPE_QUEST_VOW));
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.getTarget() != null) {
            return InteractionResult.PASS;
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 1. Quest Turn-in Check (Top Priority):
        int toothSlot = findItemSlot(player, ExampleMod.SAND_WORM_TOOTH.get());
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdingTooth = mainHand.is(ExampleMod.SAND_WORM_TOOTH.get()) || offHand.is(ExampleMod.SAND_WORM_TOOTH.get());
        boolean hasTooth = holdingTooth || toothSlot != -1;

        if (hasTooth) {
            if (!this.level().isClientSide()) {
                ItemStack toothStack = mainHand.is(ExampleMod.SAND_WORM_TOOTH.get()) ? mainHand
                        : (offHand.is(ExampleMod.SAND_WORM_TOOTH.get()) ? offHand : player.getInventory().getItem(toothSlot));
                toothStack.shrink(1);

                boolean convertedBorrowed = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(ExampleMod.ZULFIQAR.get()) && ZulfiqarItem.isBorrowed(stack)) {
                        ZulfiqarItem.setBorrowed(stack, false);
                        convertedBorrowed = true;
                        break;
                    }
                }

                if (!convertedBorrowed) {
                    ItemStack permanentSword = new ItemStack(ExampleMod.ZULFIQAR.get());
                    if (!player.getInventory().add(permanentSword)) {
                        player.drop(permanentSword, false);
                    }
                }

                setQuestCompleted(player, true);
                setQuestAccepted(player, true);

                player.sendSystemMessage(Component.literal("§a[الشيخ] وفيت بعهدك ودفعت الضرّ عن العباد وأهلكت الوحش، هذا السيف صار لك حقاً ولن ينكسر بعد اليوم."));
                this.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Quest Vow Check (Top Priority when Level >= 2):
        int currentLevel = this.getVillagerData().getLevel();
        boolean questAccepted = isQuestAccepted(player);
        boolean questCompleted = isQuestCompleted(player);

        if (currentLevel >= 2 && !questAccepted && !questCompleted) {
            if (!this.level().isClientSide()) {
                triggerQuestVow(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 3. Quest in Progress (Reissue sword if broken/lost):
        if (!this.level().isClientSide() && questAccepted && !questCompleted && !hasPermanentZulfiqar(player)) {
            boolean hasBorrowed = hasBorrowedZulfiqar(player);
            if (!hasBorrowed) {
                ItemStack borrowedSword = new ItemStack(ExampleMod.ZULFIQAR.get());
                ZulfiqarItem.setBorrowed(borrowedSword, true);
                if (!player.getInventory().add(borrowedSword)) {
                    player.drop(borrowedSword, false);
                }
                player.sendSystemMessage(Component.literal("§c[الشيخ] انكسر السيف لأنك لم تحفظ العهد؟ سأعطيك إياه مرة أخيرة، فلا تخن الأمانة."));
            }
        }

        // 4. Check if the Sheikh is currently inside the mosque:
        if (this.isCurrentlyInsideMosque()) {
            if (!this.level().isClientSide()) {
                // 1. Display the original mosque reminder message
                player.sendSystemMessage(Component.literal("§c[الشيخ] المسجد للعبادة وليس للتجارة."));

                // 2. Offer dialogue prompt with clickable Yes and No buttons in chat to trade outside
                MutableComponent prompt = Component.translatable("message.examplemod.sheikh.ask_trade_outside")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(" ")
                        .append(Component.literal("[✔ ")
                                .append(Component.translatable("gui.examplemod.dialogue.trade_outside_yes"))
                                .append("]")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withBold(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sheikh trade_outside " + this.getId() + " true"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("gui.examplemod.dialogue.trade_outside_yes")))))
                        .append("  ")
                        .append(Component.literal("[✖ ")
                                .append(Component.translatable("gui.examplemod.dialogue.trade_outside_no"))
                                .append("]")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.RED)
                                        .withBold(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sheikh trade_outside " + this.getId() + " false"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("gui.examplemod.dialogue.trade_outside_no")))));

                player.sendSystemMessage(prompt);

                // 3. Open GUI Dialogue Screen
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SheikhDialoguePayload(this.getId(), this.getDisplayName().getString(), SheikhDialoguePayload.TYPE_TRADE_OUTSIDE));
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // If walking outside
        if (this.interactionState == SheikhInteractionState.WALKING_OUTSIDE) {
            if (!this.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.examplemod.sheikh.please_wait_outside"));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // If returning to mosque
        if (this.interactionState == SheikhInteractionState.RETURNING_TO_MOSQUE) {
            if (!this.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.examplemod.sheikh.returning_now"));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Proceed to open the normal trading screen
        return this.openTradingScreen(player, hand);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        offers.clear();
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(ExampleMod.MISWAK.get(), 32), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ExampleMod.ARAK_SAPLING.get()), 16, 2, 0.05F));

        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_13), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_CAT), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_BLOCKS), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_CHIRP), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_FAR), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_MALL), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_MELLOHI), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_STAL), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_STRAD), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_WARD), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_11), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_WAIT), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_PIGSTEP), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_OTHERSIDE), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_5), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_RELIC), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_CREATOR), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_CREATOR_MUSIC_BOX), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_NUGGET, 3), new ItemStack(Items.MUSIC_DISC_PRECIPICE), 16, 2, 0.05F));

        if (this.tradeLevel >= 2) {
            offers.add(new MerchantOffer(
                    new ItemCost(ExampleMod.ARAK_LOG_ITEM.get(), 16),
                    new ItemStack(Items.EMERALD, 1),
                    16, 5, 0.05F
            ));
            offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 3),
                    new ItemStack(Items.BOOK, 1),
                    12, 5, 0.05F
            ));
        }

        if (this.tradeLevel >= 3) {
            offers.add(new MerchantOffer(
                    new ItemCost(Items.IRON_INGOT, 32),
                    Optional.of(new ItemCost(Items.RED_WOOL, 10)),
                    new ItemStack(ExampleMod.RED_HEADBAND.get()),
                    1, 0, 0.2F
            ));
        }

        // Safety: ensure Zulfiqar is never present in regular offers
        offers.removeIf(offer -> offer.getResult().is(ExampleMod.ZULFIQAR.get()));
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        int xp = offer.getXp();
        if (offer.shouldRewardExp()) {
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), xp));
        }
        this.tradeXp += xp;

        // If villagerXp >= 10 (the requirement for Level 2 in Minecraft villagers) and getVillagerData().getLevel() < 2:
        if (this.tradeXp >= 10 && this.getVillagerData().getLevel() < 2) {
            this.setVillagerData(this.getVillagerData().setLevel(2));
            this.updateTrades();

            // When reaching Level 2, interrupt trading and trigger the historical quest vow
            Player tradingPlayer = this.getTradingPlayer();
            if (tradingPlayer != null) {
                tradingPlayer.closeContainer();
                if (!isQuestAccepted(tradingPlayer) && !isQuestCompleted(tradingPlayer)) {
                    triggerQuestVow(tradingPlayer);
                }
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        } else if (this.tradeXp >= 70) {
            if (this.getVillagerData().getLevel() < 3) {
                this.setVillagerData(this.getVillagerData().setLevel(3));
                this.updateTrades();
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
