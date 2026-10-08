package com.example.muslimmod.item;

import com.example.muslimmod.ExampleMod;
import com.example.muslimmod.entity.ik.ChainSegment;
import com.example.muslimmod.entity.ik.KinematicChainEntity;
import com.example.muslimmod.entity.ik.worm.WormChainEntity;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ZulfiqarItem extends SwordItem {

    public ZulfiqarItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, 4, 16.0F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (isBorrowed(stack)) {
            tooltipComponents.add(Component.literal("§c⚠ سيف معار - مخصص لقتال دودة الرمال فقط"));
        }
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("§c▶ 8 Attack Damage"));
        tooltipComponents.add(Component.literal("§6⚔ Ability: Charged Dash Strike"));
        tooltipComponents.add(Component.literal("§7Hold & Release Right-Click to dash forward and deal up to 22.0 Area Damage (10s Cooldown)."));
        tooltipComponents.add(Component.literal("§6⚔ Ability: Fall Strike"));
        tooltipComponents.add(Component.literal("§7Jump/Fall from 2+ blocks and hold Attack in mid-air to plunge downward, locking onto targets on impact. Release to dislodge with double damage."));
        tooltipComponents.add(Component.literal("§6🛡 Ability: Parry"));
        tooltipComponents.add(Component.literal("§7Press Parry key to deflect incoming arrows and projectiles."));
        tooltipComponents.add(Component.literal("§a✔ Instant Attack Speed (No Cooldown)"));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int charge = this.getUseDuration(stack, entity) - timeLeft;
        float power = Math.min((float) charge / 20.0F, 1.0F);

        if (power < 0.2F) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
        double velocity = 1.4 + (power * 1.4); // Instant high-velocity burst (up to 2.8)
        player.setDeltaMovement(horizontalLook.x * velocity, player.getDeltaMovement().y, horizontalLook.z * velocity);
        player.hurtMarked = true;

        player.swing(entity.getUsedItemHand(), true);
        level.playSound(null, player.blockPosition(), ExampleMod.DASH_SOUND.get(), SoundSource.PLAYERS, 1.2F, 1.0F);

        if (!level.isClientSide) {
            float damage = 6.0F + (power * 16.0F); // Deals up to 22.0F at full charge

            if (level instanceof ServerLevel serverLevel) {
                double dashDist = 2.5D + (power * 3.5D);
                for (int i = 0; i <= 10; i++) {
                    double progress = (dashDist * i) / 10.0D;
                    double px = player.getX() + horizontalLook.x * progress;
                    double py = player.getY() + 0.5D;
                    double pz = player.getZ() + horizontalLook.z * progress;
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0.1D, 0.1D, 0.1D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, (int) (1 + power * 3), 0.2D, 0.2D, 0.2D, 0.05D);
                }
            }

            AABB targetBox = player.getBoundingBox()
                    .expandTowards(horizontalLook.scale(3.0D + (power * 3.0D)))
                    .inflate(1.5D, 1.0D, 1.5D);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, targetBox,
                    target -> target != player && target.isAlive() && !player.isAlliedTo(target));

            for (LivingEntity target : targets) {
                if (isBorrowed(stack) && !isSandWorm(target)) {
                    breakBorrowedSword(stack, player);
                    break;
                }
                target.hurt(level.damageSources().playerAttack(player), damage);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ(),
                            10, 0.3D, 0.3D, 0.3D, 0.1D);
                }
            }

            if (stack.isEmpty()) {
                player.getCooldowns().addCooldown(this, 200);
                return;
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }

        player.getCooldowns().addCooldown(this, 200);
    }

    public static boolean isBorrowed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("is_borrowed")) {
            return customData.copyTag().getBoolean("is_borrowed");
        }
        return false;
    }

    public static void setBorrowed(ItemStack stack, boolean borrowed) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("is_borrowed", borrowed));
    }

    public static boolean isSandWorm(Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity instanceof WormChainEntity || entity instanceof ChainSegment || entity instanceof KinematicChainEntity;
    }

    public static void breakBorrowedSword(ItemStack stack, LivingEntity attacker) {
        stack.shrink(1);
        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (attacker instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("§cنقضت العهد! انكسر السيف لأنك استخدمته في غير موضعه."), true);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isBorrowed(stack)) {
            if (!isSandWorm(target)) {
                breakBorrowedSword(stack, attacker);
                return true;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!stack.isEmpty()) {
            super.postHurtEnemy(stack, target, attacker);
        }
    }
}
