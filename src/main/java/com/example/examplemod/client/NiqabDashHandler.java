package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.NiqabArmorEvents;
import com.example.examplemod.network.NiqabDashPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class NiqabDashHandler {
    private static boolean wasXDown;
    private static int controlledFlightTicks;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null) {
            wasXDown = false;
            controlledFlightTicks = 0;
            return;
        }

        boolean xDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_X);
        if (xDown && !wasXDown && NiqabArmorEvents.hasDashArmor(player)) {
            sendDash(player, minecraft, true);
        }

        if (controlledFlightTicks > 0) {
            if (!NiqabArmorEvents.hasDashArmor(player)) {
                controlledFlightTicks = 0;
            } else {
                sendDash(player, minecraft, false);
                controlledFlightTicks--;
            }
        }
        wasXDown = xDown;
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (hasActiveDash(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()
                || !hasActiveDash(player)) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        boolean shemagh = player.getItemBySlot(EquipmentSlot.HEAD).is(ExampleMod.SHEMAGH.get());
        renderDashFilter(event.getGuiGraphics(), width, height, player.tickCount, shemagh);
    }

    private static void renderDashFilter(GuiGraphics graphics, int width, int height, int tick, boolean shemagh) {
        int hazeColor = shemagh ? 0x141C1008 : 0x1417082B;
        int shadowColor = shemagh ? 0x302F1A0C : 0x302E0B52;
        int mistColor = shemagh ? 0x483D2510 : 0x484B167A;
        int dustColor = shemagh ? 0x704F3215 : 0x704A1590;
        int phase = tick * 7;

        // A light ghostly haze leaves the scene visible instead of drawing a solid border.
        graphics.fill(0, 0, width, height, hazeColor);

        // Irregular translucent blotches create a soft spectral/dusty lens effect.
        for (int i = 0; i < 30; i++) {
            int x = Math.floorMod(i * 83 + phase * 2, Math.max(1, width));
            int y = Math.floorMod(i * 47 + phase, Math.max(1, height));
            int blobWidth = 8 + Math.floorMod(i * 13 + tick, 30);
            int blobHeight = 5 + Math.floorMod(i * 11 + tick * 2, 22);
            int left = Math.max(0, x - blobWidth / 2);
            int top = Math.max(0, y - blobHeight / 2);
            int right = Math.min(width, left + blobWidth);
            int bottom = Math.min(height, top + blobHeight);
            graphics.fill(left, top, right, bottom, (i % 3 == 0) ? shadowColor : mistColor);
            if (i % 2 == 0) {
                int speckSize = 2 + Math.floorMod(i + tick, 5);
                graphics.fill(Math.min(width, left + blobWidth / 3), Math.min(height, top + blobHeight / 3),
                        Math.min(width, left + blobWidth / 3 + speckSize),
                        Math.min(height, top + blobHeight / 3 + speckSize), dustColor);
            }
        }

        // Fine particles drift across the whole view without forming scanlines.
        for (int i = 0; i < 42; i++) {
            int x = Math.floorMod(i * 37 + phase * 3, Math.max(1, width));
            int y = Math.floorMod(i * 61 + phase * 2, Math.max(1, height));
            int size = 1 + Math.floorMod(i + tick, 4);
            graphics.fill(x, y, Math.min(width, x + size), Math.min(height, y + size),
                    (i % 4 == 0) ? mistColor : dustColor);
        }
    }

    private static boolean hasActiveDash(Player player) {
        return player != null
                && (player.getItemBySlot(EquipmentSlot.HEAD).is(ExampleMod.SHEMAGH.get())
                || player.getItemBySlot(EquipmentSlot.HEAD).is(ExampleMod.NIQAB_HELMET.get()))
                && player.hasEffect(MobEffects.INVISIBILITY);
    }

    private static void sendDash(LocalPlayer player, Minecraft minecraft, boolean starting) {
        if (player.isSpectator() || player.isPassenger()) {
            return;
        }

        float forward = 0.0F;
        float strafe = 0.0F;
        if (minecraft.options.keyUp.isDown()) forward++;
        if (minecraft.options.keyDown.isDown()) forward--;
        if (minecraft.options.keyLeft.isDown()) strafe++;
        if (minecraft.options.keyRight.isDown()) strafe--;

        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            horizontalLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontalLook = horizontalLook.normalize();
        }
        Vec3 right = new Vec3(-horizontalLook.z, 0.0D, horizontalLook.x);
        double vertical = 0.0D;
        if (minecraft.options.keyJump.isDown()) vertical++;
        if (minecraft.options.keyShift.isDown()) vertical--;
        Vec3 direction = horizontalLook.scale(forward).add(right.scale(strafe)).add(0.0D, vertical, 0.0D);
        if (starting && direction.lengthSqr() < 1.0E-4D) {
            direction = horizontalLook;
        }
        if (direction.lengthSqr() > 1.0E-4D) {
            direction = direction.normalize();
        }
        PacketDistributor.sendToServer(new NiqabDashPayload(direction.x, direction.y, direction.z));
        if (starting) {
            controlledFlightTicks = 40;
        }
    }
}