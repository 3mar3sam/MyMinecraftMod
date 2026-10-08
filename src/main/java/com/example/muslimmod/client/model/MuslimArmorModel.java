package com.example.muslimmod.client.model;

import com.example.muslimmod.ExampleMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class MuslimArmorModel extends HumanoidArmorModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "muslim_armor"), "main");

    public final ModelPart clothTail;
    public final ModelPart clothTailBack;
    public final ModelPart clothTailFront;
    public final ModelPart sideClothTail;
    public final ModelPart sideClothTailRight;
    public final ModelPart sideClothTailLeft;

    public MuslimArmorModel(ModelPart root) {
        super(root);
        this.clothTail = this.body.hasChild("cloth_tail") ? this.body.getChild("cloth_tail") : null;
        this.clothTailBack = this.clothTail != null && this.clothTail.hasChild("cloth_tail_back") ? this.clothTail.getChild("cloth_tail_back") : null;
        this.clothTailFront = this.clothTail != null && this.clothTail.hasChild("cloth_tail_front") ? this.clothTail.getChild("cloth_tail_front") : null;
        this.sideClothTail = this.body.hasChild("side_cloth_tail") ? this.body.getChild("side_cloth_tail") : null;
        this.sideClothTailRight = this.sideClothTail != null && this.sideClothTail.hasChild("side_cloth_tail_right") ? this.sideClothTail.getChild("side_cloth_tail_right") : null;
        this.sideClothTailLeft = this.sideClothTail != null && this.sideClothTail.hasChild("side_cloth_tail_left") ? this.sideClothTail.getChild("side_cloth_tail_left") : null;
    }

    public void setupForSlot(EquipmentSlot slot) {
        this.setAllVisible(false);
        this.head.visible = (slot == EquipmentSlot.HEAD);
        this.hat.visible = (slot == EquipmentSlot.HEAD);
        this.body.visible = (slot == EquipmentSlot.CHEST);
        this.rightArm.visible = (slot == EquipmentSlot.CHEST);
        this.leftArm.visible = (slot == EquipmentSlot.CHEST);
        if (this.clothTail != null) this.clothTail.visible = (slot == EquipmentSlot.CHEST);
        if (this.sideClothTail != null) this.sideClothTail.visible = (slot == EquipmentSlot.CHEST);
        this.rightLeg.visible = (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET);
        this.leftLeg.visible = (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 16).mirror().addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cloth_tail = body.addOrReplaceChild("cloth_tail", CubeListBuilder.create(), PartPose.offset(0.125F, 3.125F, 0.5625F));

        PartDefinition cloth_tail_back = cloth_tail.addOrReplaceChild("cloth_tail_back", CubeListBuilder.create().texOffs(20, 37).addBox(-4.1667F, -1.1667F, 0.0F, 8.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0417F, -1.9583F, 1.9375F));

        PartDefinition cloth_tail_front = cloth_tail.addOrReplaceChild("cloth_tail_front", CubeListBuilder.create().texOffs(20, 37).addBox(-4.1667F, -1.1667F, 0.0F, 8.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0417F, -1.9583F, -3.0625F));

        PartDefinition side_cloth_tail = body.addOrReplaceChild("side_cloth_tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition side_cloth_tail_right = side_cloth_tail.addOrReplaceChild("side_cloth_tail_right", CubeListBuilder.create().texOffs(48, 32).addBox(-2.0F, -1.0F, 0.1F, 4.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 4.0F));

        PartDefinition side_cloth_tail_left = side_cloth_tail.addOrReplaceChild("side_cloth_tail_left", CubeListBuilder.create().texOffs(48, 32).addBox(-2.0F, -1.0F, -0.1F, 4.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        applyClothPhysics();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        applyClothPhysics();
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    private void applyClothPhysics() {
        // Enforce cloth flaps inherit body visibility
        boolean bodyVis = this.body.visible;
        if (this.clothTail != null) this.clothTail.visible = bodyVis;
        if (this.sideClothTail != null) this.sideClothTail.visible = bodyVis;

        if (!bodyVis) return;

        // Calculate real-time continuous wave & dynamic movement physics
        long time = System.currentTimeMillis();
        float idleWave = (float) Math.sin(time / 450.0D) * 0.08F; // Gentle wind breathing

        // Back flap swings backward when running forward (momentum from leg movement)
        if (this.clothTail != null) {
            // Fixed at top, swinging outward at bottom
            this.clothTail.xRot = (float) Math.toRadians(6.0D) + idleWave;
            // Additional reaction if body is leaning / running (sprinting/walking)
            this.clothTail.xRot += Math.abs(this.rightLeg.xRot) * 0.25F;
        }

        // Side flap swings slightly to the side with natural idle flutter
        if (this.sideClothTail != null) {
            float sideFlutter = (float) Math.sin(time / 380.0D + 1.2D) * 0.06F;
            this.sideClothTail.zRot = sideFlutter + (this.rightLeg.xRot * 0.1F);
        }
    }
}