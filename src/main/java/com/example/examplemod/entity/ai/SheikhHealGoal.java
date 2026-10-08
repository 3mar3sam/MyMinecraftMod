package com.example.examplemod.entity.ai;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.SheikhEntity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SheikhHealGoal extends Goal {
    private final SheikhEntity sheikh;
    private int noCombatTicks = 0;
    private int eatingTicks = 0;
    private ItemStack previousMainHand = ItemStack.EMPTY;
    private boolean isPostCombatHeal = false;

    public SheikhHealGoal(SheikhEntity sheikh) {
        this.sheikh = sheikh;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (sheikh.getTarget() != null && sheikh.getTarget().isAlive()) {
            noCombatTicks = 0;
            return false;
        }

        noCombatTicks++;
        if (noCombatTicks < 60) {
            return false;
        }

        if (sheikh.getHealth() < sheikh.getMaxHealth()) {
            isPostCombatHeal = true;
            return true;
        }

        // Random idle miswak usage during peacetime (roughly every 15-20 seconds of idling)
        if (sheikh.getRandom().nextInt(300) == 0) {
            isPostCombatHeal = false;
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        eatingTicks = 40;
        previousMainHand = sheikh.getItemBySlot(EquipmentSlot.MAINHAND);
        sheikh.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ExampleMod.MISWAK.get()));
    }

    @Override
    public boolean canContinueToUse() {
        return eatingTicks > 0 && (sheikh.getTarget() == null || !sheikh.getTarget().isAlive());
    }

    @Override
    public void tick() {
        eatingTicks--;

        if (eatingTicks % 4 == 0) {
            float pitch = 1.0F + sheikh.getRandom().nextFloat() * 0.2F;
            sheikh.playSound(SoundEvents.BRUSH_GENERIC, 0.7F, pitch);

            if (sheikh.level() instanceof ServerLevel serverLevel) {
                Vec3 look = sheikh.getViewVector(1.0F);
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ExampleMod.MISWAK.get())),
                        sheikh.getX() + look.x * 0.5, sheikh.getY() + sheikh.getEyeHeight() - 0.2, sheikh.getZ() + look.z * 0.5,
                        3, 0.1D, 0.1D, 0.1D, 0.05D);
            }
        }
    }

    @Override
    public void stop() {
        if (eatingTicks <= 0) {
            if (isPostCombatHeal) {
                sheikh.heal(8.0F);
            } else {
                sheikh.heal(2.0F);
            }
        }
        sheikh.setItemSlot(EquipmentSlot.MAINHAND, previousMainHand);
        noCombatTicks = 0;
    }
}
