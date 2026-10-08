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
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AbayaArmorItem extends ArmorItem {
    private static final ResourceLocation NIQAB_ARMOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExampleMod.MODID, "textures/models/armor/muslim_women_armor.png");

    public AbayaArmorItem(Type type, Properties properties) {
        super(ArmorMaterials.DIAMOND, type, properties);
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return NiqabArmorEvents.hasFullMuslimSet(player);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, net.minecraft.world.item.ArmorMaterial.Layer layer, boolean innerModel) {
        return NIQAB_ARMOR_TEXTURE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6[ميزة الطقم الكامل - Full Set Bonus]"));
        tooltip.add(Component.literal("§b✦ رؤية ليلية دائمة وزيادة سرعة الحركة"));
        tooltip.add(Component.literal("§a✦ وقار وسكينة: الوحوش لا تبادر بالهجوم إلا إذا اعتديت عليها"));
        tooltip.add(Component.literal("§c✦ نجدة وحماية: الوحوش القريبة تهب للدفاع عنك فور تعرضك لأي اعتداء"));
        tooltip.add(Component.literal("§d✦ حياء: الإندرمان لا يغضب عند التحديق في عينيه"));
        tooltip.add(Component.literal("§7(يتطلب ارتداء الطقم كاملاً: خوذة، ثوب، سروال، حذاء)"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
