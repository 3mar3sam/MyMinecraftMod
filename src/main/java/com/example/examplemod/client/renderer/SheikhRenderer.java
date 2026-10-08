package com.example.examplemod.client.renderer;

import com.example.examplemod.client.model.SheikhModel;
import com.example.examplemod.entity.SheikhEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class SheikhRenderer extends MobRenderer<SheikhEntity, SheikhModel<SheikhEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("examplemod", "textures/entity/sheikh.png");

    public SheikhRenderer(EntityRendererProvider.Context context) {
        super(context, new SheikhModel<>(context.bakeLayer(SheikhModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SheikhEntity entity) {
        return TEXTURE;
    }
}
