package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.ModItems;
import com.example.examplemod.client.model.MuslimArmorModel;
import com.example.examplemod.network.ParryPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    private static MuslimArmorModel muslimArmorModel;
        private static final ResourceLocation NIQAB_ARMOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExampleMod.MODID, "textures/models/armor/muslim_women_armor.png");

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get())) {
            while (KeyInit.PARRY_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new ParryPayload(true));
                player.swing(InteractionHand.MAIN_HAND);
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, 1.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Only render armor sleeve if wearing the Abaya Chestplate
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ABAYA_CHESTPLATE.get())) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();
            HumanoidArm arm = event.getArm();

            if (muslimArmorModel == null) {
                ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(MuslimArmorModel.LAYER_LOCATION);
                muslimArmorModel = new MuslimArmorModel(root);
            }

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(NIQAB_ARMOR_TEXTURE));

            poseStack.pushPose();
            if (player instanceof AbstractClientPlayer clientPlayer) {
                EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(clientPlayer);
                if (renderer instanceof PlayerRenderer playerRenderer) {
                    PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();
                    if (arm == HumanoidArm.RIGHT) {
                        muslimArmorModel.rightArm.copyFrom(playerModel.rightArm);
                        muslimArmorModel.rightArm.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    } else {
                        muslimArmorModel.leftArm.copyFrom(playerModel.leftArm);
                        muslimArmorModel.leftArm.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    }
                }
            }
            poseStack.popPose();
        }
    }
}
