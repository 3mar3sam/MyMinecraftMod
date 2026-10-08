package com.example.muslimmod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SaplingBlock;
import com.example.muslimmod.item.AbayaArmorItem;
import com.example.muslimmod.item.MiswakItem;
import com.example.muslimmod.item.MaleArmorItem;
import com.example.muslimmod.item.NiqabItem;
import com.example.muslimmod.item.ZulfiqarItem;
import com.example.muslimmod.block.SandWormAltarBlock;
import com.example.muslimmod.block.entity.SandWormAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.example.muslimmod.entity.SheikhEntity;
import com.example.muslimmod.entity.ModEntities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "muslimmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static final Map<UUID, Long> parryTimestamps = new ConcurrentHashMap<>();
    // Create a Deferred Register to hold Blocks which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold EntityTypes which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register to hold SoundEvents which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    // Create a Deferred Register to hold BlockEntityTypes which will all be registered under the "muslimmod" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<SoundEvent> PARRY_SOUND = SOUND_EVENTS.register("custom_parry",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "custom_parry")));

    public static final Supplier<SoundEvent> DASH_SOUND = SOUND_EVENTS.register("custom_dash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "custom_dash")));

    public static final Supplier<SoundEvent> SHEMAGH_DASH_SOUND = SOUND_EVENTS.register("shemagh_dash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "shemagh_dash")));

    public static final Supplier<SoundEvent> PLUNGE_SOUND = SOUND_EVENTS.register("custom_plunge",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "custom_plunge")));

    public static final Supplier<SoundEvent> PIERCE_SOUND = SOUND_EVENTS.register("custom_pierce",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "custom_pierce")));

    public static final DeferredItem<Item> MISWAK = ITEMS.register("miswak",
            () -> new MiswakItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> SAND_WORM_TOOTH = ITEMS.register("sand_worm_tooth",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ZULFIQAR = ITEMS.register("zulfiqar",
            () -> new ZulfiqarItem(Tiers.DIAMOND, new Item.Properties().durability(1561)));

    public static final DeferredItem<ArmorItem> RED_HEADBAND = ITEMS.register("red_headband",
            () -> new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(33))) {
                @Override
                public ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
                    return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/models/armor/red_headband_3d.png");
                }
            });

    public static final DeferredItem<NiqabItem> NIQAB_HELMET = ITEMS.register("niqab_helmet",
            () -> new NiqabItem(new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(33))));

    public static final DeferredItem<AbayaArmorItem> ABAYA_CHESTPLATE = ITEMS.register("abaya_chestplate",
            () -> new AbayaArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(33))));

    public static final DeferredItem<AbayaArmorItem> ABAYA_LEGGINGS = ITEMS.register("abaya_leggings",
            () -> new AbayaArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(33))));

    public static final DeferredItem<AbayaArmorItem> ABAYA_BOOTS = ITEMS.register("abaya_boots",
            () -> new AbayaArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(33))));

    public static final DeferredItem<MaleArmorItem> SHEMAGH = ITEMS.register("shemagh",
            () -> new MaleArmorItem(ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(37)),
                    "item.muslimmod.shemagh.description"));

    public static final DeferredItem<MaleArmorItem> THOBE_CHESTPLATE = ITEMS.register("thobe_chestplate",
            () -> new MaleArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(37)),
                    "item.muslimmod.thobe_chestplate.description"));

    public static final DeferredItem<MaleArmorItem> THOBE_LEGGINGS = ITEMS.register("thobe_leggings",
            () -> new MaleArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(37)),
                    "item.muslimmod.thobe_leggings.description"));

    public static final DeferredItem<MaleArmorItem> ISLAMIC_SANDALS = ITEMS.register("islamic_sandals",
            () -> new MaleArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(37)),
                    "item.muslimmod.islamic_sandals.description"));

    public static final DeferredHolder<EntityType<?>, EntityType<SheikhEntity>> SHEIKH = ENTITIES.register("sheikh",
            () -> EntityType.Builder.of(SheikhEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F).build("sheikh"));

    public static final DeferredItem<SpawnEggItem> SHEIKH_SPAWN_EGG = ITEMS.register("sheikh_spawn_egg",
            () -> new SpawnEggItem(SHEIKH.get(), 0x6D4C41, 0xD7CCC8, new Item.Properties()));

    public static final DeferredBlock<Block> ARAK_LOG = BLOCKS.register("arak_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredItem<BlockItem> ARAK_LOG_ITEM = ITEMS.registerSimpleBlockItem("arak_log", ARAK_LOG);

    public static final DeferredBlock<Block> ARAK_LEAVES = BLOCKS.register("arak_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()));
    public static final DeferredItem<BlockItem> ARAK_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("arak_leaves", ARAK_LEAVES);

    public static final DeferredBlock<Block> ARAK_SAPLING = BLOCKS.register("arak_sapling", () -> new SaplingBlock(ArakTreeFeatures.ARAK_TREE_GROWER, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().noCollission().noOcclusion().sound(SoundType.GRASS)));
    public static final DeferredItem<BlockItem> ARAK_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("arak_sapling", ARAK_SAPLING);

    public static final DeferredBlock<SandWormAltarBlock> SAND_WORM_ALTAR_BLOCK = BLOCKS.register("sand_worm_altar",
            () -> new SandWormAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> SAND_WORM_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("sand_worm_altar", SAND_WORM_ALTAR_BLOCK);

    public static final Supplier<BlockEntityType<SandWormAltarBlockEntity>> SAND_WORM_ALTAR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("sand_worm_altar",
            () -> BlockEntityType.Builder.of(SandWormAltarBlockEntity::new, SAND_WORM_ALTAR_BLOCK.get()).build(null));

    /** Vanilla music discs retextured/renamed as Quran recitations in lang + sounds overrides. */
    public static final Item[] QURAN_DISCS = new Item[] {
            Items.MUSIC_DISC_13,
            Items.MUSIC_DISC_CAT,
            Items.MUSIC_DISC_BLOCKS,
            Items.MUSIC_DISC_CHIRP,
            Items.MUSIC_DISC_FAR,
            Items.MUSIC_DISC_MALL,
            Items.MUSIC_DISC_MELLOHI,
            Items.MUSIC_DISC_STAL,
            Items.MUSIC_DISC_STRAD,
            Items.MUSIC_DISC_WARD,
            Items.MUSIC_DISC_11,
            Items.MUSIC_DISC_WAIT,
            Items.MUSIC_DISC_PIGSTEP,
            Items.MUSIC_DISC_OTHERSIDE,
            Items.MUSIC_DISC_5,
            Items.MUSIC_DISC_RELIC,
            Items.MUSIC_DISC_CREATOR,
            Items.MUSIC_DISC_CREATOR_MUSIC_BOX,
            Items.MUSIC_DISC_PRECIPICE
    };

    // (Removed default example item and tab)

    // Creates a creative tab that shows the vanilla Totem of Undying and example items
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOTEM_TAB = CREATIVE_MODE_TABS.register("totem_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.muslimmod.tab"))
            .icon(() -> Items.TOTEM_OF_UNDYING.getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(Items.TOTEM_OF_UNDYING); // Show only the vanilla Totem of Undying
                output.accept(MISWAK);
                output.accept(ZULFIQAR);
                output.accept(RED_HEADBAND);
                output.accept(NIQAB_HELMET);
                output.accept(ABAYA_CHESTPLATE);
                output.accept(ABAYA_LEGGINGS);
                output.accept(ABAYA_BOOTS);
                output.accept(SHEMAGH);
                output.accept(THOBE_CHESTPLATE);
                output.accept(THOBE_LEGGINGS);
                output.accept(ISLAMIC_SANDALS);
                output.accept(SHEIKH_SPAWN_EGG);
                output.accept(ARAK_LOG_ITEM);
                output.accept(ARAK_LEAVES_ITEM);
                output.accept(ARAK_SAPLING_ITEM);
                output.accept(SAND_WORM_ALTAR_ITEM);
                output.accept(SAND_WORM_TOOTH);
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entity types get registered
        ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so sound events get registered
        SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(com.example.muslimmod.network.ModPayloads::register);

        // Register our NeoForge event handlers on the global event bus.
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
        NeoForge.EVENT_BUS.register(new NiqabArmorEvents());


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        event.enqueueWork(() -> LOGGER.info("HELLO FROM COMMON SETUP WORK QUEUE"));

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SHEIKH.get(), SheikhEntity.createAttributes().build());
    }

}
