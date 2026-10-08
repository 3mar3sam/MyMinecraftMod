package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;

import net.minecraft.client.Minecraft;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HeadbandModel extends HumanoidModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "headband"), "main");

    public final ModelPart tailLeft;
    public final ModelPart tailRight;

    public HeadbandModel(ModelPart root) {
        super(root);
        this.tailLeft = this.head.getChild("tail_left");
        this.tailRight = this.head.getChild("tail_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F);
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);

        // Headband strip around forehead
        head.addOrReplaceChild("band", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.5F, -7.5F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.05F)), PartPose.ZERO);

        // Left and right ribbon tails hanging from back of the head
        head.addOrReplaceChild("tail_left", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, 0.0F, 1.5F, 9.0F, 0.2F), PartPose.offset(0.0F, -6.0F, 4.1F));

        head.addOrReplaceChild("tail_right", CubeListBuilder.create()
                .texOffs(8, 16)
                .addBox(0.5F, 0.0F, 0.0F, 1.5F, 9.0F, 0.2F), PartPose.offset(0.0F, -6.0F, 4.1F));

        // Clear hat layer so only the headband renders
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        applyPhysics(entity);
    }

    public void applyPhysics(LivingEntity entity) {
        if (this.tailLeft == null || this.tailRight == null) return;

        // Calculate partial tick animation time
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float age = entity.tickCount + partialTick;

        // Calculate actual movement speed
        Vec3 motion = entity.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        boolean isMoving = horizontalSpeed > 0.05 || entity.isSprinting();

        // Base breathing sway
        float idleWave = Mth.sin(age * 0.15F) * 0.08F;

        // Running / Dashing lift angle (lifts backward on X axis)
        float runLift = (float) Math.min(horizontalSpeed * 3.5F, 1.4F);
        if (entity.isSprinting() && runLift < 0.8F) {
            runLift = 0.8F;
        }

        // Flutter wave effect
        float flutterSpeed = isMoving ? 0.8F : 0.2F;
        float flutterIntensity = isMoving ? (0.2F + (float) horizontalSpeed * 0.5F) : 0.05F;
        float flutterLeft = Mth.sin(age * flutterSpeed) * flutterIntensity;
        float flutterRight = Mth.cos(age * flutterSpeed + 0.5F) * flutterIntensity;

        // Apply rotations directly to model parts
        this.tailLeft.xRot = idleWave + runLift + flutterLeft;
        this.tailRight.xRot = idleWave + runLift + flutterRight;
        this.tailLeft.zRot = Mth.sin(age * 0.3F) * 0.1F;
        this.tailRight.zRot = -Mth.sin(age * 0.3F) * 0.1F;
    }
}
