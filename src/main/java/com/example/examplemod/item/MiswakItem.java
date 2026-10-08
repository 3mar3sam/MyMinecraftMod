package com.example.examplemod.item;

import java.util.List;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MiswakItem extends Item {
    public MiswakItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.BRUSH_GENERIC;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            List<MobEffectInstance> harmfulEffects = player.getActiveEffects().stream()
                    .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                    .toList();

            for (MobEffectInstance effect : harmfulEffects) {
                player.removeEffect(effect.getEffect());
            }

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            level.playSound(null, player.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 0.35F, 1.15F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return stack;
    }
}
