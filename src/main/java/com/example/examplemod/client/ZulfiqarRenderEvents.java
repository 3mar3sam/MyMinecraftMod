package com.example.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.lang.reflect.Field;
import java.util.List;

@EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ZulfiqarRenderEvents {

    private static final Field LAYERS_FIELD;

    static {
        Field field = null;
        try {
            field = LivingEntityRenderer.class.getDeclaredField("layers");
            field.setAccessible(true);
        } catch (Exception e) {
            for (Field f : LivingEntityRenderer.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    field = f;
                    break;
                }
            }
        }
        LAYERS_FIELD = field;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        // Only apply when actively airborne holding the plunge attack or stuck onto an enemy
        if (!ZulfiqarPlungeHandler.isPlunging(player)) return;

        event.setCanceled(true); // Prevent vanilla setupAnim from overwriting custom angles

        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();

        poseStack.pushPose();

        // 1. Vanilla Entity Matrix Setup
        float bodyYaw = Mth.rotLerp(partialTick, clientPlayer.yBodyRotO, clientPlayer.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, clientPlayer.yHeadRotO, clientPlayer.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        float headPitch = Mth.rotLerp(partialTick, clientPlayer.xRotO, clientPlayer.getXRot());
        float ageInTicks = clientPlayer.tickCount + partialTick;

        // Yaw orientation
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - bodyYaw));

        // Vanilla model scaling & base vertical positioning
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0.0D, -1.501D, 0.0D);

        // Center-of-mass forward dive pitch tilt (15 degrees forward tilt for dynamic falling)
        poseStack.translate(0.0D, 0.75D, 0.0D);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(15.0F));
        poseStack.translate(0.0D, -0.75D, 0.0D);

        // 2. Base model setup (disable limb swinging while airborne)
        model.attackTime = 0.0F;
        model.riding = false;
        model.young = clientPlayer.isBaby();
        model.setupAnim(clientPlayer, 0.0F, 0.0F, ageInTicks, netHeadYaw, headPitch);

        // 3. Custom Dynamic Airborne Downward Plunge Impale Posture

        // --- HEAD ---
        // Focus gaze down onto the enemy beneath
        model.head.xRot = (float) Math.toRadians(25.0D);
        model.head.yRot = 0.0F;
        model.head.zRot = 0.0F;

        // --- BODY (TORSO) ---
        // Naturally aligned with root skeleton so neck, shoulders, and hips stay perfectly intact
        model.body.xRot = (float) Math.toRadians(5.0D);
        model.body.yRot = 0.0F;
        model.body.zRot = 0.0F;

        // --- RIGHT ARM (PRIMARY SWORD HAND) ---
        // Mathematically calculated 3D angle ensuring the sword blade points vertically downward (0.999 Y)
        model.rightArm.xRot = (float) Math.toRadians(96.0D);
        model.rightArm.yRot = (float) Math.toRadians(150.0D);
        model.rightArm.zRot = (float) Math.toRadians(6.0D);

        // --- LEFT ARM (OFFHAND TWO-HANDED PLUNGE GRIP) ---
        // Reaches forward across to clasp the sword hilt together with the right hand
        model.leftArm.xRot = (float) Math.toRadians(-120.0D);
        model.leftArm.yRot = (float) Math.toRadians(5.0D);
        model.leftArm.zRot = (float) Math.toRadians(-75.0D);

        // --- LEGS (DYNAMIC AIRBORNE DIVE SILHOUETTE) ---
        // Right leg trailing backward dynamically in the wind
        model.rightLeg.xRot = (float) Math.toRadians(-25.0D);
        model.rightLeg.yRot = (float) Math.toRadians(8.0D);
        model.rightLeg.zRot = (float) Math.toRadians(10.0D);

        // Left leg bent forward/upward in an athletic dive scissor stance
        model.leftLeg.xRot = (float) Math.toRadians(18.0D);
        model.leftLeg.yRot = (float) Math.toRadians(-8.0D);
        model.leftLeg.zRot = (float) Math.toRadians(-10.0D);

        // --- 4. CRITICAL: SYNCHRONIZE 3D SKIN LAYERS ---
        // Synchronizes outer skin layers (jacket, sleeves, pants, hat) so they never detach
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);

        // 5. Render main player model
        RenderType renderType = model.renderType(clientPlayer.getSkin().texture());
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 6. Render layers (Equipped Zulfiqar sword, Armor, etc.)
        if (LAYERS_FIELD != null) {
            try {
                @SuppressWarnings("unchecked")
                List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layers =
                        (List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) LAYERS_FIELD.get(renderer);
                if (layers != null) {
                    for (var layer : layers) {
                        layer.render(poseStack, buffer, packedLight, clientPlayer, 0.0F, 0.0F, partialTick, ageInTicks, netHeadYaw, headPitch);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        poseStack.popPose();
    }
}
