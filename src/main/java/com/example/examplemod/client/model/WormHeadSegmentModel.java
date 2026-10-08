package com.example.examplemod.client.model;

import com.example.examplemod.entity.ik.worm.WormHeadSegment;
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

public class WormHeadSegmentModel extends EntityModel<WormHeadSegment> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("examplemod", "worm_head_segment"), "main"
    );

    private final ModelPart basesegment;
    private final ModelPart mouth;
    private final ModelPart teeth;
    private final ModelPart teeth2;

    public WormHeadSegmentModel(ModelPart root) {
        this.basesegment = root.getChild("basesegment");
        this.mouth = root.getChild("mouth");
        this.teeth = root.getChild("teeth");
        this.teeth2 = root.getChild("teeth2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition basesegment = partdefinition.addOrReplaceChild("basesegment", CubeListBuilder.create()
            .texOffs(11, 11).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 16.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(48, 27).addBox(4.0F, -7.0F, -4.0F, 3.0F, 14.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(11, 48).addBox(-7.0F, -7.0F, -4.0F, 3.0F, 14.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(61, 74).addBox(-8.0F, -6.0F, -4.0F, 1.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(38, 62).addBox(7.0F, -6.0F, -4.0F, 1.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth = partdefinition.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        mouth.addOrReplaceChild("mouth_r1", CubeListBuilder.create().texOffs(66, 79).mirror().addBox(-0.85F, -5.125F, -2.9625F, 1.35F, 10.125F, 5.925F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.1341F, 0.0F, -6.897F, 0.0F, 0.1309F, 0.0F));
        mouth.addOrReplaceChild("mouth_r2", CubeListBuilder.create().texOffs(66, 79).addBox(-0.5F, -5.125F, -2.9625F, 1.35F, 10.125F, 5.925F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.1341F, 0.0F, -6.897F, 0.0F, -0.1309F, 0.0F));
        mouth.addOrReplaceChild("mouth_r3", CubeListBuilder.create().texOffs(15, 15).addBox(-4.0F, 0.1F, -6.0F, 8.0F, 1.0F, 6.475F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.925F, -4.125F, -0.1309F, 0.0F, 0.0F));
        mouth.addOrReplaceChild("mouth_r4", CubeListBuilder.create().texOffs(15, 15).addBox(-4.0F, -0.9F, -6.0F, 8.0F, 1.0F, 6.475F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.075F, -4.125F, 0.1309F, 0.0F, 0.0F));
        mouth.addOrReplaceChild("mouth_r5", CubeListBuilder.create().texOffs(15, 15).mirror().addBox(0.0F, -0.1F, -6.0F, 4.0F, 1.0F, 6.475F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.55F, 7.15F, -4.125F, -0.1163F, -0.0603F, -0.4765F));
        mouth.addOrReplaceChild("mouth_r6", CubeListBuilder.create().texOffs(15, 15).mirror().addBox(0.0F, -0.9F, -6.0F, 3.875F, 1.0F, 6.475F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.55F, -7.15F, -4.125F, 0.1163F, -0.0603F, 0.4765F));
        mouth.addOrReplaceChild("mouth_r7", CubeListBuilder.create().texOffs(15, 15).addBox(-4.0F, -0.1F, -6.0F, 4.0F, 1.0F, 6.475F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.55F, 7.15F, -4.125F, -0.1163F, 0.0603F, 0.4765F));
        mouth.addOrReplaceChild("mouth_r8", CubeListBuilder.create().texOffs(15, 15).addBox(-3.9F, -0.9F, -6.0F, 3.9F, 1.0F, 6.475F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.55F, -7.15F, -4.125F, 0.1163F, 0.0603F, -0.4765F));

        PartDefinition teeth = partdefinition.addOrReplaceChild("teeth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone6 = teeth.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone4 = bone6.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone = bone4.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.3F, -4.222F, -9.3772F));
        PartDefinition tooth = bone.addOrReplaceChild("tooth", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.1745F));
        tooth.addOrReplaceChild("tooth_r1", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth.addOrReplaceChild("tooth_r2", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth2 = bone.addOrReplaceChild("tooth2", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.1745F));
        tooth2.addOrReplaceChild("tooth2_r1", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth2.addOrReplaceChild("tooth2_r2", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth3 = bone.addOrReplaceChild("tooth3", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        tooth3.addOrReplaceChild("tooth3_r1", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth3.addOrReplaceChild("tooth3_r2", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth5 = bone.addOrReplaceChild("tooth5", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        tooth5.addOrReplaceChild("tooth5_r1", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth5.addOrReplaceChild("tooth5_r2", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth6 = bone.addOrReplaceChild("tooth6", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        tooth6.addOrReplaceChild("tooth6_r1", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth6.addOrReplaceChild("tooth6_r2", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth7 = bone.addOrReplaceChild("tooth7", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.3927F));
        tooth7.addOrReplaceChild("tooth7_r1", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth7.addOrReplaceChild("tooth7_r2", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition tooth8 = bone.addOrReplaceChild("tooth8", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        tooth8.addOrReplaceChild("tooth8_r1", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -0.564F, -0.1796F, 0.6F, 1.275F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4882F, 0.65F, 0.7418F, 0.0F, 0.0F));
        tooth8.addOrReplaceChild("tooth8_r2", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -1.1F, -0.075F, 0.6F, 2.8F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.597F, -0.3978F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone2 = bone4.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.7F, -3.872F, -9.0522F, 0.0F, 0.0F, -0.5236F));
        bone2.addOrReplaceChild("tooth4", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone2.addOrReplaceChild("tooth9", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.3927F));
        bone2.addOrReplaceChild("tooth10", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone2.addOrReplaceChild("tooth11", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.1309F));
        bone2.addOrReplaceChild("tooth12", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.1309F));
        bone2.addOrReplaceChild("tooth13", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone2.addOrReplaceChild("tooth14", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone3 = bone4.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        bone3.addOrReplaceChild("tooth15", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, 0.1745F));
        bone3.addOrReplaceChild("tooth16", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, -0.2182F));
        bone3.addOrReplaceChild("tooth17", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, 0.2618F));
        bone3.addOrReplaceChild("tooth18", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, -0.0873F));
        bone3.addOrReplaceChild("tooth19", CubeListBuilder.create().texOffs(1, 2).addBox(-3.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone3.addOrReplaceChild("tooth20", CubeListBuilder.create().texOffs(1, 2).addBox(-4.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -8.9272F, -0.3054F, 0.0F, 0.0F));
        bone3.addOrReplaceChild("tooth21", CubeListBuilder.create().texOffs(1, 2).addBox(-4.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone5 = bone6.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone7 = bone5.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(3.7F, -3.872F, -9.0522F, 0.0F, 0.0F, 0.5236F));
        bone7.addOrReplaceChild("tooth29", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.1309F));
        bone7.addOrReplaceChild("tooth30", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.2618F));
        bone7.addOrReplaceChild("tooth31", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone7.addOrReplaceChild("tooth32", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone7.addOrReplaceChild("tooth33", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-1.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone8 = bone5.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 1.5708F));
        bone8.addOrReplaceChild("tooth36", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.7F, -3.7046F, -8.296F, -0.3054F, 0.0F, -0.1309F));
        bone8.addOrReplaceChild("tooth37", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(5.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone8.addOrReplaceChild("tooth38", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, -0.2182F));
        bone8.addOrReplaceChild("tooth39", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(4.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone8.addOrReplaceChild("tooth40", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(2.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -8.3522F, -0.3054F, 0.0F, -0.1309F));
        bone8.addOrReplaceChild("tooth41", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, 0.1876F));
        bone8.addOrReplaceChild("tooth42", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(3.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone9 = teeth.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone10 = bone9.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone11 = bone10.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(0.3F, 4.222F, -9.3772F));
        bone11.addOrReplaceChild("tooth22", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone11.addOrReplaceChild("tooth23", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone11.addOrReplaceChild("tooth24", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone11.addOrReplaceChild("tooth25", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, -0.0873F));
        bone11.addOrReplaceChild("tooth26", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone11.addOrReplaceChild("tooth27", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone11.addOrReplaceChild("tooth28", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone12 = bone10.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.7F, 3.872F, -9.0522F, 0.0F, 0.0F, 0.5236F));
        bone12.addOrReplaceChild("tooth43", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, -0.1745F));
        bone12.addOrReplaceChild("tooth44", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.3491F));
        bone12.addOrReplaceChild("tooth45", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone12.addOrReplaceChild("tooth46", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone12.addOrReplaceChild("tooth47", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone12.addOrReplaceChild("tooth48", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone12.addOrReplaceChild("tooth48", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone14 = bone9.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone15 = bone14.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(3.7F, 3.872F, -9.0522F, 0.0F, 0.0F, -0.5236F));
        bone15.addOrReplaceChild("tooth57", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.1745F));
        bone15.addOrReplaceChild("tooth58", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(1.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, -0.5236F));
        bone15.addOrReplaceChild("tooth59", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-2.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone15.addOrReplaceChild("tooth60", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone15.addOrReplaceChild("tooth61", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone15.addOrReplaceChild("tooth62", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone15.addOrReplaceChild("tooth63", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition teeth2 = partdefinition.addOrReplaceChild("teeth2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.45F));
        PartDefinition bone13 = teeth2.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone16 = bone13.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone17 = bone16.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offset(0.3F, -4.222F, -9.3772F));
        bone17.addOrReplaceChild("tooth50", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.6118F, -0.22F, -0.3054F, 0.0F, -0.2182F));
        bone17.addOrReplaceChild("tooth51", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone17.addOrReplaceChild("tooth52", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.4363F));
        bone17.addOrReplaceChild("tooth53", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.6118F, -0.22F, -0.3054F, 0.0F, -0.2618F));
        bone17.addOrReplaceChild("tooth54", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone17.addOrReplaceChild("tooth55", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone17.addOrReplaceChild("tooth56", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone18 = bone16.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.7F, -3.872F, -9.0522F, 0.0F, 0.0F, -0.5236F));
        bone18.addOrReplaceChild("tooth64", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.3054F));
        bone18.addOrReplaceChild("tooth65", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone18.addOrReplaceChild("tooth66", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone18.addOrReplaceChild("tooth67", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone18.addOrReplaceChild("tooth68", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.3927F));
        bone18.addOrReplaceChild("tooth69", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone18.addOrReplaceChild("tooth70", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone19 = bone16.addOrReplaceChild("bone19", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        bone19.addOrReplaceChild("tooth71", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, -0.4363F));
        bone19.addOrReplaceChild("tooth72", CubeListBuilder.create().texOffs(1, 2).addBox(-6.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone19.addOrReplaceChild("tooth73", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone19.addOrReplaceChild("tooth74", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, -0.3491F));
        bone19.addOrReplaceChild("tooth75", CubeListBuilder.create().texOffs(1, 2).addBox(-3.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone19.addOrReplaceChild("tooth76", CubeListBuilder.create().texOffs(1, 2).addBox(-4.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone19.addOrReplaceChild("tooth77", CubeListBuilder.create().texOffs(1, 2).addBox(-4.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone20 = bone13.addOrReplaceChild("bone20", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone21 = bone20.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offsetAndRotation(3.7F, -3.872F, -9.0522F, 0.0F, 0.0F, 0.5236F));
        bone21.addOrReplaceChild("tooth78", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, -0.3054F));
        bone21.addOrReplaceChild("tooth79", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone21.addOrReplaceChild("tooth80", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-2.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone21.addOrReplaceChild("tooth81", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(0.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone21.addOrReplaceChild("tooth82", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-1.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone21.addOrReplaceChild("tooth83", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
        bone21.addOrReplaceChild("tooth84", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone22 = bone20.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 1.5708F));
        bone22.addOrReplaceChild("tooth85", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, 0.48F));
        bone22.addOrReplaceChild("tooth86", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(5.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone22.addOrReplaceChild("tooth87", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(1.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone22.addOrReplaceChild("tooth88", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(4.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone22.addOrReplaceChild("tooth89", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, -3.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.7F, -3.6101F, -9.5972F, -0.3054F, 0.0F, 0.2618F));
        bone22.addOrReplaceChild("tooth90", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(3.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));
        bone22.addOrReplaceChild("tooth91", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(3.7F, -2.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -4.222F, -9.3772F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone23 = teeth2.addOrReplaceChild("bone23", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone24 = bone23.addOrReplaceChild("bone24", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone25 = bone24.addOrReplaceChild("bone25", CubeListBuilder.create(), PartPose.offset(0.3F, 4.222F, -9.3772F));
        bone25.addOrReplaceChild("tooth92", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, -0.2182F));
        bone25.addOrReplaceChild("tooth93", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 1.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.6118F, -0.22F, 0.3054F, 0.0F, 0.2182F));
        bone25.addOrReplaceChild("tooth94", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 1.0527F, -0.822F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -0.6118F, -0.22F, 0.3054F, 0.0F, -0.3927F));
        bone25.addOrReplaceChild("tooth95", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.3054F));
        bone25.addOrReplaceChild("tooth96", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone25.addOrReplaceChild("tooth97", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone25.addOrReplaceChild("tooth98", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone26 = bone24.addOrReplaceChild("bone26", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.7F, 3.872F, -9.0522F, 0.0F, 0.0F, 0.5236F));
        bone26.addOrReplaceChild("tooth99", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.4363F));
        bone26.addOrReplaceChild("tooth100", CubeListBuilder.create().texOffs(1, 2).addBox(-2.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.5672F));
        bone26.addOrReplaceChild("tooth101", CubeListBuilder.create().texOffs(1, 2).addBox(1.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone26.addOrReplaceChild("tooth102", CubeListBuilder.create().texOffs(1, 2).addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.6109F));
        bone26.addOrReplaceChild("tooth103", CubeListBuilder.create().texOffs(1, 2).addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone26.addOrReplaceChild("tooth104", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, -0.3054F));
        bone26.addOrReplaceChild("tooth105", CubeListBuilder.create().texOffs(1, 2).addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition bone27 = bone23.addOrReplaceChild("bone27", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition bone28 = bone27.addOrReplaceChild("bone28", CubeListBuilder.create(), PartPose.offsetAndRotation(3.7F, 3.872F, -9.0522F, 0.0F, 0.0F, -0.5236F));
        bone28.addOrReplaceChild("tooth106", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.5236F));
        bone28.addOrReplaceChild("tooth107", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(1.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.15F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone28.addOrReplaceChild("tooth108", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-2.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone28.addOrReplaceChild("tooth109", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(0.7F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone28.addOrReplaceChild("tooth110", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-1.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone28.addOrReplaceChild("tooth111", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        bone28.addOrReplaceChild("tooth112", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-0.3F, 0.403F, -0.8478F, 0.6F, 2.0F, 0.425F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(WormHeadSegment entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        basesegment.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        teeth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        teeth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
