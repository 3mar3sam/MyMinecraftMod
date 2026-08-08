package com.example.examplemod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

public class NeoForgeEvents {
    @SubscribeEvent
    public void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player
                && (event.getItem().is(Items.PORKCHOP) || event.getItem().is(Items.COOKED_PORKCHOP))) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 250, 0));
        }
    }
}
