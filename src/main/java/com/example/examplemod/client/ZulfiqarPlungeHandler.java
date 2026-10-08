package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.ZulfiqarPlungeEvents;
import com.example.examplemod.network.PlungeAttackPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ZulfiqarPlungeHandler {

    private static boolean localPlunging = false;
    private static boolean clientStuck = false;
    private static int clientStuckEntityId = -1;
    private static Vec3 clientRelativeOffset = null;
    private static Vec3 clientFallbackPos = null;
    private static float clientStuckYaw = 0.0F;
    private static float clientStuckPitch = 0.0F;
    private static float clientStuckHeadYaw = 0.0F;
    private static float clientStuckBodyYaw = 0.0F;
    private static float plungeAnimProgress = 0.0F;
    private static float prevPlungeAnimProgress = 0.0F;

    public static boolean isClientStuck() {
        return clientStuck;
    }

    public static float getPlungeAnimProgress(float partialTick) {
        return net.minecraft.util.Mth.lerp(partialTick, prevPlungeAnimProgress, plungeAnimProgress);
    }

    public static boolean isPlunging(Player player) {
        if (player == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            return localPlunging || clientStuck;
        }
        return ZulfiqarPlungeEvents.isPlunging(player);
    }

    public static void setLocalPlunging(boolean plunging) {
        localPlunging = plunging;
    }

    public static void handleStickSync(int entityId, boolean stuck, double offsetX, double offsetY, double offsetZ) {
        if (stuck) {
            localPlunging = false;
            clientStuck = true;
            clientStuckEntityId = entityId;
            clientRelativeOffset = new Vec3(offsetX, offsetY, offsetZ);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                mc.player.noPhysics = true;
                clientStuckYaw = mc.player.getYRot();
                clientStuckPitch = mc.player.getXRot();
                clientStuckHeadYaw = mc.player.yHeadRot;
                clientStuckBodyYaw = mc.player.yBodyRot;

                Entity victim = mc.level.getEntity(entityId);
                double px, py, pz;
                if (victim != null) {
                    px = victim.getX() + offsetX;
                    py = victim.getY() + offsetY;
                    pz = victim.getZ() + offsetZ;
                } else {
                    px = mc.player.getX();
                    py = mc.player.getY();
                    pz = mc.player.getZ();
                }
                clientFallbackPos = new Vec3(px, py, pz);
                mc.player.setPos(px, py, pz);
                mc.player.setDeltaMovement(0, 0, 0);
                mc.player.resetFallDistance();
                mc.player.fallDistance = 0.0F;
                mc.player.hurtMarked = true;
            }
        } else {
            releaseClientStuck();
        }
    }

    private static void releaseClientStuck() {
        if (!clientStuck) return;
        clientStuck = false;
        localPlunging = false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.noPhysics = false;
            Entity victim = (clientStuckEntityId != -1 && mc.level != null) ? mc.level.getEntity(clientStuckEntityId) : null;
            Vec3 pullDir;
            if (victim != null) {
                Vec3 away = player.position().subtract(victim.position());
                Vec3 horizontalAway = new Vec3(away.x, 0, away.z);
                if (horizontalAway.lengthSqr() > 0.0001D) {
                    pullDir = horizontalAway.normalize().scale(0.35D).add(0, 0.35D, 0);
                } else {
                    Vec3 look = player.getLookAngle();
                    pullDir = new Vec3(-look.x, 0, -look.z).normalize().scale(0.35D).add(0, 0.35D, 0);
                }
            } else {
                Vec3 look = player.getLookAngle();
                pullDir = new Vec3(-look.x, 0, -look.z).normalize().scale(0.35D).add(0, 0.35D, 0);
            }

            player.setDeltaMovement(pullDir);
            player.hurtMarked = true;
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.2F);
            player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.5F);
        }

        clientStuckEntityId = -1;
        clientRelativeOffset = null;
        clientFallbackPos = null;
        clientStuckYaw = 0.0F;
        clientStuckPitch = 0.0F;
        clientStuckHeadYaw = 0.0F;
        clientStuckBodyYaw = 0.0F;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (clientStuck) {
            event.getInput().forwardImpulse = 0.0F;
            event.getInput().leftImpulse = 0.0F;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
            event.getInput().up = false;
            event.getInput().down = false;
            event.getInput().left = false;
            event.getInput().right = false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) {
            prevPlungeAnimProgress = plungeAnimProgress;
            plungeAnimProgress = 0.0F;
            if (clientStuck) {
                releaseClientStuck();
                PacketDistributor.sendToServer(new PlungeAttackPayload(false));
            } else if (localPlunging) {
                localPlunging = false;
                PacketDistributor.sendToServer(new PlungeAttackPayload(false));
            }
            return;
        }

        prevPlungeAnimProgress = plungeAnimProgress;
        if (isPlunging(player)) {
            plungeAnimProgress = Math.min(1.0F, plungeAnimProgress + 0.35F);
        } else {
            plungeAnimProgress = Math.max(0.0F, plungeAnimProgress - 0.25F);
        }

        boolean holdingZulfiqar = player.getItemInHand(InteractionHand.MAIN_HAND).is(ExampleMod.ZULFIQAR.get());
        boolean attackKeyDown = mc.options.keyAttack.isDown();

        // Active stick freeze handling on client
        if (clientStuck) {
            if (!holdingZulfiqar || !attackKeyDown || !player.isAlive()) {
                releaseClientStuck();
                PacketDistributor.sendToServer(new PlungeAttackPayload(false));
                return;
            }

            Entity victim = (clientStuckEntityId != -1 && mc.level != null) ? mc.level.getEntity(clientStuckEntityId) : null;
            if (victim != null && (!victim.isAlive() || victim.isRemoved())) {
                releaseClientStuck();
                PacketDistributor.sendToServer(new PlungeAttackPayload(false));
                return;
            }

            double posX, posY, posZ;
            if (victim != null && victim.isAlive() && clientRelativeOffset != null) {
                posX = victim.getX() + clientRelativeOffset.x;
                posY = victim.getY() + clientRelativeOffset.y;
                posZ = victim.getZ() + clientRelativeOffset.z;
                clientFallbackPos = new Vec3(posX, posY, posZ);
            } else if (clientFallbackPos != null) {
                posX = clientFallbackPos.x;
                posY = clientFallbackPos.y;
                posZ = clientFallbackPos.z;
            } else {
                posX = player.getX();
                posY = player.getY();
                posZ = player.getZ();
            }

            player.noPhysics = true;
            player.setPos(posX, posY, posZ);
            player.setDeltaMovement(0, 0, 0);
            player.resetFallDistance();
            player.fallDistance = 0.0F;
            player.hurtMarked = true;

            // Lock face and camera orientation
            player.setYRot(clientStuckYaw);
            player.setXRot(clientStuckPitch);
            player.yRotO = clientStuckYaw;
            player.xRotO = clientStuckPitch;
            player.yHeadRot = clientStuckHeadYaw;
            player.yHeadRotO = clientStuckHeadYaw;
            player.yBodyRot = clientStuckBodyYaw;
            player.yBodyRotO = clientStuckBodyYaw;

            // Prevent breaking or hitting blocks while locked in strike
            if (mc.gameMode != null) {
                mc.gameMode.stopDestroyBlock();
            }
            return;
        }

        boolean inAir = !player.onGround() && !player.isInWater() && !player.isPassenger() && !player.getAbilities().flying;

        if (holdingZulfiqar && inAir && attackKeyDown) {
            if (!localPlunging) {
                if (!ZulfiqarPlungeEvents.canTriggerPlunge(player)) {
                    return;
                }
                localPlunging = true;
                PacketDistributor.sendToServer(new PlungeAttackPayload(true));
                player.playSound(ExampleMod.PLUNGE_SOUND.get(), 1.2F, 1.0F);
            }

            // If player was thrown upward or falling slowly, drive downward into plunge!
            if (player.getDeltaMovement().y > -0.8D) {
                player.setDeltaMovement(player.getDeltaMovement().x * 0.95D, -1.0D, player.getDeltaMovement().z * 0.95D);
                player.hurtMarked = true;
            }

            // Spawn wind / sweep effect on client
            if (player.level().random.nextFloat() < 0.6F) {
                player.level().addParticle(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 0.2D, player.getZ(), 0, -0.5D, 0);
            }
        } else {
            if (localPlunging) {
                localPlunging = false;
                PacketDistributor.sendToServer(new PlungeAttackPayload(false));
            }
        }
    }

    @SubscribeEvent
    public static void onCalculatePlayerTurn(CalculatePlayerTurnEvent event) {
        if (clientStuck) {
            event.setMouseSensitivity(0.0D);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (clientStuck) {
            event.setYaw(clientStuckYaw);
            event.setPitch(clientStuckPitch);
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (clientStuck) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        if (clientStuck && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(net.neoforged.neoforge.client.event.RenderHandEvent event) {
        if (event.getHand() == InteractionHand.OFF_HAND) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && isPlunging(mc.player)) {
                event.setCanceled(true);
            }
        }
    }
}
