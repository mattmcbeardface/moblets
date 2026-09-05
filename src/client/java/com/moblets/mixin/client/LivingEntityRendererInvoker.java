package com.moblets.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    /*
     * LivingEntityRenderer.addLayer(...) is protected.
     *
     * Expose it so the Enderman renderer mixin can install
     * Minecraft's normal held-item rendering layer.
     */
    @Invoker("addLayer")
    boolean babyMobs$addLayer(RenderLayer<?, ?> layer);
}
