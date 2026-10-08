package com.example.muslimmod.client;

import com.example.muslimmod.ExampleMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * امتداد العميل لسيف ذي الفقار — يتحكم في شكل السيف في منظور الشخص الأول (First Person)
 * عند هجوم الغوص (Plunge). لا يغيّر منظور الشخص الثالث؛ ذلك في ZulfiqarRenderEvents.
 */
public class ZulfiqarClientExtensions implements IClientItemExtensions {

    public static final ZulfiqarClientExtensions INSTANCE = new ZulfiqarClientExtensions();

    /**
     * وضعية الذراع في نموذج اللاعب (للآخرين / بعض الرenders).
     * ITEM = يمسك غرضاً كالسيف العادي.
     */
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return HumanoidModel.ArmPose.ITEM;
    }

    /**
     * الدالة الرئيسية لتحريك السيف في يدك على الشاشة.
     * ترجع true = نحن نتحكم بالكامل في الموقع والدوران (ماينكرافت لا يطبّق الأنيميشن الافتراضي).
     * ترجع false = السلوك العادي للعنصر.
     */
    @Override
    public boolean applyForgeHandTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            ItemStack itemStack,
            float partialTick,
            float equipProgress,
            float swingProgress
    ) {
        // إذا لم يكن الذراع يحمل ذا الفقار، لا نتدخل
        if (!itemStack.is(ExampleMod.ZULFIQAR.get())) {
            return false;
        }

        // هل اللاعب في حالة غوص هجومي؟ (في الهواء + زر الهجوم، أو عالق في عدو)
        boolean isPlunging = ZulfiqarPlungeHandler.isPlunging(player);
        // تقدم الأنيميشن من 0 إلى 1 — يزيد أثناء الغوص وينخفض عند التوقف (يُحدَّث في ZulfiqarPlungeHandler)
        float animProgress = ZulfiqarPlungeHandler.getPlungeAnimProgress(partialTick);

        // لا غوص ولا بقايا أنيميشن → اترك يد ماينكرافت الافتراضية
        if (!isPlunging && animProgress <= 0.001F) {
            return false;
        }

        boolean isRightHand = arm == HumanoidArm.RIGHT;
        float handSign = isRightHand ? 1.0F : -1.0F;
        boolean isStuck = ZulfiqarPlungeHandler.isClientStuck();

        float progressClamped = Mth.clamp(animProgress, 0.0F, 1.0F);
        float easeFlip = 0.5F * (1.0F - (float) Math.cos(progressClamped * Math.PI));

        float defaultX = 0.56F * handSign;
        float defaultY = -0.52F;
        float defaultZ = -0.72F;
        float defaultPitch = 0.0F;
        float defaultYaw = 0.0F;

        float targetX = 0.07F * handSign;
        float targetStartY = 0.95F;
        float targetStuckY = -0.22F;
        float targetZ = -1.10F;
        float targetPitch = 200.0F;
        float targetYaw = 165.0F * handSign;

        float currentX = Mth.lerp(easeFlip, defaultX, targetX);
        float currentZ = Mth.lerp(easeFlip, defaultZ, targetZ);
        float currentY;
        if (isStuck) {
            float easeCurve = (float) Math.pow(animProgress, 3);
            currentY = Mth.lerp(easeCurve, targetStartY, targetStuckY);
        } else {
            currentY = Mth.lerp(easeFlip, defaultY, targetStartY);
        }

        poseStack.translate(currentX, currentY, currentZ);

        float currentPitch = Mth.lerp(easeFlip, defaultPitch, targetPitch);
        float currentYaw = Mth.lerp(easeFlip, defaultYaw, targetYaw);
        float currentRoll = 0.0F;

        poseStack.mulPose(Axis.XP.rotationDegrees(currentPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(currentYaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(currentRoll));

        /*
         * ─── 3) الحجم ───
         */
        float scale = 1.10F;
        poseStack.scale(scale, scale, scale);

        // true = تم تطبيق تحويل مخصص؛ لا تستخدم أنيميشن السيف الافتراضي
        return true;
    }
}
