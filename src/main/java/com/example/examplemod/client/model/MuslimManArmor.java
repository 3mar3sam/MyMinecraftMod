package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
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

public class MuslimManArmor extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "muslim_man_armor"), "main");

    private final ModelPart headwear;
    private final ModelPart jacket;
    private final ModelPart leftSleeve;
    private final ModelPart rightSleeve;
    private final ModelPart leftPants;
    private final ModelPart rightPants;
    private EquipmentSlot renderSlot;

    public MuslimManArmor(ModelPart root) {
        super(root);
        this.headwear = child(root, "headwear");
        this.jacket = child(root, "jacket");
        this.leftSleeve = child(root, "left_sleeve");
        this.rightSleeve = child(root, "right_sleeve");
        this.leftPants = child(root, "left_pants");
        this.rightPants = child(root, "right_pants");
    }

    public void setupForSlot(EquipmentSlot slot) {
        this.renderSlot = slot;
        if (slot == null) {
            setVisible(this.head, false);
            setVisible(this.body, false);
            setVisible(this.leftArm, false);
            setVisible(this.rightArm, false);
            setVisible(this.leftLeg, false);
            setVisible(this.rightLeg, false);
            return;
        }

        copyPartPose(this.headwear, this.head);
        copyPartPose(this.jacket, this.body);
        copyPartPose(this.leftSleeve, this.leftArm);
        copyPartPose(this.rightSleeve, this.rightArm);
        copyPartPose(this.leftPants, this.leftLeg);
        copyPartPose(this.rightPants, this.rightLeg);

        setVisible(this.head, slot == EquipmentSlot.HEAD);
        setVisible(this.body, slot == EquipmentSlot.CHEST);
        setVisible(this.leftArm, slot == EquipmentSlot.CHEST);
        setVisible(this.rightArm, slot == EquipmentSlot.CHEST);
        setVisible(this.leftLeg, slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET);
        setVisible(this.rightLeg, slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET);
        setVisible(this.headwear, true);
        setVisible(this.jacket, true);
        setVisible(this.leftSleeve, true);
        setVisible(this.rightSleeve, true);
        setVisible(this.leftPants, true);
        setVisible(this.rightPants, true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void copyPropertiesFrom(HumanoidModel<?> source) {
        if (source != null) {
            ((HumanoidModel) source).copyPropertiesTo(this);
        }
    }

    private static ModelPart child(ModelPart parent, String name) {
        return parent != null && parent.hasChild(name) ? parent.getChild(name) : null;
    }

    private static void copyPartPose(ModelPart target, ModelPart source) {
        if (target != null && source != null) {
            target.copyFrom(source);
        }
    }

    private static void setVisible(ModelPart part, boolean visible) {
        if (part != null) {
            part.visible = visible;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F)), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.ZERO);
        root.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.ZERO);
        addArm(root, "left_arm", "left_sleeve", 5.0F, 32, 48, 48, 48);
        addArm(root, "right_arm", "right_sleeve", -5.0F, 40, 16, 40, 32);
        addLeg(root, "left_leg", "left_pants", 2.0F, 16, 48, 0, 48);
        addLeg(root, "right_leg", "right_pants", -2.0F, 0, 16, 0, 32);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addArm(PartDefinition root, String armName, String sleeveName, float x,
                               int armU, int armV, int sleeveU, int sleeveV) {
        float offsetX = x > 0 ? -1.0F : -3.0F;
        root.addOrReplaceChild(armName, CubeListBuilder.create().texOffs(armU, armV)
                .addBox(offsetX, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(x, 2.0F, 0.0F));
        root.addOrReplaceChild(sleeveName, CubeListBuilder.create().texOffs(sleeveU, sleeveV)
                .addBox(offsetX, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                PartPose.offset(x, 2.0F, 0.0F));
    }

    private static void addLeg(PartDefinition root, String legName, String pantsName, float x,
                               int legU, int legV, int pantsU, int pantsV) {
        root.addOrReplaceChild(legName, CubeListBuilder.create().texOffs(legU, legV)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(x, 12.0F, 0.0F));
        root.addOrReplaceChild(pantsName, CubeListBuilder.create().texOffs(pantsU, pantsV)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                PartPose.offset(x, 12.0F, 0.0F));
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        if (entity != null) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               int color) {
        if (poseStack == null || buffer == null) {
            return;
        }
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        if (this.renderSlot == EquipmentSlot.HEAD) {
            renderPart(this.headwear, poseStack, buffer, packedLight, packedOverlay, color);
        } else if (this.renderSlot == EquipmentSlot.CHEST) {
            renderPart(this.jacket, poseStack, buffer, packedLight, packedOverlay, color);
            renderPart(this.leftSleeve, poseStack, buffer, packedLight, packedOverlay, color);
            renderPart(this.rightSleeve, poseStack, buffer, packedLight, packedOverlay, color);
        } else if (this.renderSlot == EquipmentSlot.LEGS) {
            renderPart(this.leftPants, poseStack, buffer, packedLight, packedOverlay, color);
            renderPart(this.rightPants, poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer,
                                   int packedLight, int packedOverlay, int color) {
        if (part != null && part.visible) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}