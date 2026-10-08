package com.example.muslimmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.example.muslimmod.client.model.HeadbandModel;
import com.example.muslimmod.client.model.MuslimArmorModel;
import com.example.muslimmod.client.model.MuslimManArmor;
import com.example.muslimmod.client.model.SheikhModel;
import com.example.muslimmod.client.renderer.SheikhRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.example.muslimmod.client.KeyInit;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() != null && event.getSound().getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SheikhModel.LAYER_LOCATION, SheikhModel::createBodyLayer);
        event.registerLayerDefinition(HeadbandModel.LAYER_LOCATION, HeadbandModel::createBodyLayer);
        event.registerLayerDefinition(MuslimArmorModel.LAYER_LOCATION, MuslimArmorModel::createBodyLayer);
        event.registerLayerDefinition(MuslimManArmor.LAYER_LOCATION, MuslimManArmor::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ExampleMod.SHEIKH.get(), SheikhRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.PARRY_KEY);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private HeadbandModel headbandModel;

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.headbandModel == null) {
                    var entityModels = Minecraft.getInstance().getEntityModels();
                    var root = entityModels.bakeLayer(HeadbandModel.LAYER_LOCATION);
                    this.headbandModel = new HeadbandModel(root);
                }
                ((HumanoidModel) original).copyPropertiesTo(this.headbandModel);
                this.headbandModel.applyPhysics(livingEntity);
                return this.headbandModel;
            }
        }, ExampleMod.RED_HEADBAND.get());

        IClientItemExtensions muslimArmorExtension = new IClientItemExtensions() {
            private MuslimArmorModel muslimArmorModel;

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.muslimArmorModel == null) {
                    var entityModels = Minecraft.getInstance().getEntityModels();
                    var root = entityModels.bakeLayer(MuslimArmorModel.LAYER_LOCATION);
                    this.muslimArmorModel = new MuslimArmorModel(root);
                }
                ((HumanoidModel) original).copyPropertiesTo(this.muslimArmorModel);
                this.muslimArmorModel.setupForSlot(equipmentSlot);
                return this.muslimArmorModel;
            }
        };

        event.registerItem(muslimArmorExtension, ExampleMod.NIQAB_HELMET.get());
        event.registerItem(muslimArmorExtension, ExampleMod.ABAYA_CHESTPLATE.get());
        event.registerItem(muslimArmorExtension, ExampleMod.ABAYA_LEGGINGS.get());
        event.registerItem(muslimArmorExtension, ExampleMod.ABAYA_BOOTS.get());

        IClientItemExtensions muslimManArmorExtension = new IClientItemExtensions() {
            private MuslimManArmor model;

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.model == null) {
                    var root = Minecraft.getInstance().getEntityModels().bakeLayer(MuslimManArmor.LAYER_LOCATION);
                    this.model = new MuslimManArmor(root);
                }
                this.model.copyPropertiesFrom(original);
                this.model.setupForSlot(equipmentSlot);
                return this.model;
            }
        };
        event.registerItem(muslimManArmorExtension, ExampleMod.SHEMAGH.get());
        event.registerItem(muslimManArmorExtension, ExampleMod.THOBE_CHESTPLATE.get());
        event.registerItem(muslimManArmorExtension, ExampleMod.THOBE_LEGGINGS.get());
        event.registerItem(muslimManArmorExtension, ExampleMod.ISLAMIC_SANDALS.get());
        event.registerItem(com.example.muslimmod.client.ZulfiqarClientExtensions.INSTANCE, ExampleMod.ZULFIQAR.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ExampleMod.ARAK_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ExampleMod.ARAK_LEAVES.get(), RenderType.cutoutMipped());
        });
    }
}
