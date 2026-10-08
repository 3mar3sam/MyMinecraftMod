package com.example.muslimmod.client;

import com.example.muslimmod.ExampleMod;
import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class HeadbandDashHandler {

    private enum DirectionKey {
        NONE(0.0F),
        FORWARD(0.0F),
        BACKWARD(180.0F),
        LEFT(-90.0F),
        RIGHT(90.0F),
        JUMP(0.0F);

        final float angleOffset;

        DirectionKey(float angleOffset) {
            this.angleOffset = angleOffset;
        }
    }

    private static DirectionKey lastPressedKey = DirectionKey.NONE;
    private static long lastPressTime = 0L;
    private static final long DOUBLE_TAP_WINDOW_MS = 250L;

    private static long lastDashTime = 0L;
    private static final long DASH_COOLDOWN_MS = 2000L;
    private static int dashIntangibleTicks = 0;

    private static boolean wasUp = false;
    private static boolean wasDown = false;
    private static boolean wasLeft = false;
    private static boolean wasRight = false;
    private static boolean wasJump = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            wasUp = false;
            wasDown = false;
            wasLeft = false;
            wasRight = false;
            wasJump = false;
            lastPressedKey = DirectionKey.NONE;
            lastPressTime = 0L;
            return;
        }

        LocalPlayer player = mc.player;
        long now = System.currentTimeMillis();

        if (dashIntangibleTicks > 0) {
            dashIntangibleTicks--;
            player.noPhysics = false; // Retain block/terrain collision while passing through entities
        }

        boolean isUp = mc.options.keyUp.isDown();
        boolean isDown = mc.options.keyDown.isDown();
        boolean isLeft = mc.options.keyLeft.isDown();
        boolean isRight = mc.options.keyRight.isDown();
        boolean isJump = mc.options.keyJump.isDown();

        DirectionKey currentKey = DirectionKey.NONE;
        if (isUp && !wasUp) {
            currentKey = DirectionKey.FORWARD;
        } else if (isDown && !wasDown) {
            currentKey = DirectionKey.BACKWARD;
        } else if (isLeft && !wasLeft) {
            currentKey = DirectionKey.LEFT;
        } else if (isRight && !wasRight) {
            currentKey = DirectionKey.RIGHT;
        } else if (isJump && !wasJump) {
            currentKey = DirectionKey.JUMP;
        }

        if (currentKey != DirectionKey.NONE) {
            long windowHandle = mc.getWindow().getWindow();
            boolean isCapsLockHeld = InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_CAPS_LOCK);

            if (isCapsLockHeld) {
                // Instant single-tap dash with Caps Lock modifier
                if (currentKey == DirectionKey.JUMP) {
                    tryVerticalDash(player);
                } else {
                    tryDash(player, currentKey);
                }
                lastPressedKey = DirectionKey.NONE;
                lastPressTime = 0L;
            } else {
                // Strict double-tap logic
                if (currentKey == lastPressedKey && (now - lastPressTime) <= DOUBLE_TAP_WINDOW_MS && (now - lastPressTime) > 30L) {
                    if (currentKey == DirectionKey.JUMP) {
                        tryVerticalDash(player);
                    } else {
                        tryDash(player, currentKey);
                    }
                    lastPressedKey = DirectionKey.NONE;
                    lastPressTime = 0L;
                } else {
                    lastPressedKey = currentKey;
                    lastPressTime = now;
                }
            }
        }

        wasUp = isUp;
        wasDown = isDown;
        wasLeft = isLeft;
        wasRight = isRight;
        wasJump = isJump;
    }

    private static void tryDash(LocalPlayer player, DirectionKey dir) {
        if (player.isSpectator() || player.isPassenger()) {
            return;
        }

        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ExampleMod.RED_HEADBAND.get())) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(ExampleMod.RED_HEADBAND.get())) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastDashTime < DASH_COOLDOWN_MS) {
            return;
        }

        float targetAngle = player.getYRot() + dir.angleOffset;
        double rad = Math.toRadians(targetAngle);
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        Vec3 direction = new Vec3(dx, 0, dz).normalize();

        // Apply velocity impulse
        player.setDeltaMovement(direction.scale(0.95D).add(0, 0.2D, 0));
        player.hurtMarked = true;
        player.noPhysics = false;
        dashIntangibleTicks = 8;
        lastDashTime = now;

        // Play subtle swoosh sound
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.2F);

        // Spawn cloud and sweep particles
        Level level = player.level();
        for (int i = 0; i < 8; i++) {
            double px = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
            double py = player.getY() + 0.2D + (player.getRandom().nextDouble() * 0.5D);
            double pz = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
            level.addParticle(ParticleTypes.CLOUD, px, py, pz, -direction.x * 0.1D, 0.05D, -direction.z * 0.1D);
        }
        level.addParticle(ParticleTypes.SWEEP_ATTACK, player.getX() + direction.x * 0.5D, player.getY() + 0.5D, player.getZ() + direction.z * 0.5D, 0, 0, 0);

        player.getCooldowns().addCooldown(ExampleMod.RED_HEADBAND.get(), 40);
    }

    private static void tryVerticalDash(LocalPlayer player) {
        if (player.isSpectator() || player.isPassenger()) {
            return;
        }

        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ExampleMod.RED_HEADBAND.get())) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(ExampleMod.RED_HEADBAND.get())) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastDashTime < DASH_COOLDOWN_MS) {
            return;
        }

        // Apply vertical impulse
        player.setDeltaMovement(player.getDeltaMovement().x, 0.95D, player.getDeltaMovement().z);
        player.hurtMarked = true;
        player.noPhysics = false;
        dashIntangibleTicks = 8;
        lastDashTime = now;

        // Play subtle swoosh sound
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.2F);

        // Spawn cloud and sweep particles
        Level level = player.level();
        for (int i = 0; i < 8; i++) {
            double px = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
            double py = player.getY() + 0.2D + (player.getRandom().nextDouble() * 0.5D);
            double pz = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
            level.addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, -0.1D, 0.0D);
        }
        level.addParticle(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 0.5D, player.getZ(), 0, 0, 0);

        player.getCooldowns().addCooldown(ExampleMod.RED_HEADBAND.get(), 40);
    }
}
