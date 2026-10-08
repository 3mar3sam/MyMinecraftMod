package com.example.examplemod.client.renderer;

import com.example.examplemod.client.model.WormSegmentModel;
import com.example.examplemod.entity.ik.worm.WormSegment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WormSegmentRenderer extends EntityRenderer<WormSegment> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("examplemod", "textures/entity/worm_segment_texture.png");
    protected final WormSegmentModel model;

    public WormSegmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WormSegmentModel(context.bakeLayer(WormSegmentModel.LAYER_LOCATION));
    }

    @Override
    public boolean shouldRender(WormSegment entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(WormSegment entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot));
        Vec3 scale = entity.getVisualScale();
        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, entityYaw, entity.getXRot());
        int overlay = entity.hurtTime > 0 ? OverlayTexture.pack(0, 10) : OverlayTexture.NO_OVERLAY;
        int light = Math.max(packedLight, 0x00F000B0);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity))), light, overlay, -1);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WormSegment entity) {
        return TEXTURE;
    }
}
