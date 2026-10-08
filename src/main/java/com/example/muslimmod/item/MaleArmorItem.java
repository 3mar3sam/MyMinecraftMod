package com.example.muslimmod.item;

import com.example.muslimmod.ExampleMod;
import com.example.muslimmod.NiqabArmorEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MaleArmorItem extends ArmorItem {
    private static final ResourceLocation MALE_ARMOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExampleMod.MODID, "textures/models/armor/muslim_man_armor.png");
    private final String descriptionKey;

    public MaleArmorItem(Type type, Properties properties, String descriptionKey) {
        super(net.minecraft.world.item.ArmorMaterials.NETHERITE, type, properties);
        this.descriptionKey = descriptionKey;
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return NiqabArmorEvents.hasFullMuslimSet(player);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                             ArmorMaterial.Layer layer, boolean innerModel) {
        return MALE_ARMOR_TEXTURE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable(this.descriptionKey));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}