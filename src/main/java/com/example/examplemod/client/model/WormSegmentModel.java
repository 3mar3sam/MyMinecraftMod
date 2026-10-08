package com.example.examplemod.client.model;

import com.example.examplemod.entity.ik.worm.WormSegment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class WormSegmentModel extends EntityModel<WormSegment> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("examplemod", "worm_segment"), "main"
    );

    private final ModelPart bb_main;

    public WormSegmentModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
            .texOffs(9, 9).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(46, 25).addBox(4.0F, -7.0F, -6.0F, 3.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(9, 46).addBox(-7.0F, -7.0F, -6.0F, 3.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(59, 72).addBox(-8.0F, -6.0F, -6.0F, 1.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(36, 60).addBox(7.0F, -6.0F, -6.0F, 1.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), 
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(WormSegment entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
