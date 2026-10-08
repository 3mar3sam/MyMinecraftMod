package com.example.examplemod.client;

import com.example.examplemod.client.model.WormHeadSegmentModel;
import com.example.examplemod.client.model.WormSegmentModel;
import com.example.examplemod.client.renderer.WormHeadSegmentRenderer;
import com.example.examplemod.client.renderer.WormSegmentRenderer;
import com.example.examplemod.entity.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WormSegmentModel.LAYER_LOCATION, WormSegmentModel::createBodyLayer);
        event.registerLayerDefinition(WormHeadSegmentModel.LAYER_LOCATION, WormHeadSegmentModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // حل مشكلة الكراش: رندرر خفي لكيان التحكم
        event.registerEntityRenderer(ModEntities.WORM_CHAIN.get(), NoopRenderer::new);
        
        event.registerEntityRenderer(ModEntities.WORM_SEGMENT.get(), WormSegmentRenderer::new);
        event.registerEntityRenderer(ModEntities.WORM_HEAD_SEGMENT.get(), WormHeadSegmentRenderer::new);
    }
}