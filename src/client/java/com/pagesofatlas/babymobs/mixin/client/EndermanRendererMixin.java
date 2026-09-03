package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.BabyEndermen;
import com.pagesofatlas.babymobs.client.BabyEndermanItemInHandLayer;
import com.pagesofatlas.babymobs.client.BabyVariantRenderState;

import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.world.entity.monster.EnderMan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermanRenderer.class)
public abstract class EndermanRendererMixin {

    /*
     * Vanilla EndermanRenderer extracts HumanoidRenderState,
     * including both held-item render states, but never installs
     * ItemInHandLayer.
     *
     * Add the missing layer so a Moblet's stolen MAINHAND item
     * is actually visible.
     */
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void babyMobs$addItemInHandLayer(
            EntityRendererProvider.Context context,
            CallbackInfo ci
    ) {
        EndermanRenderer renderer =
                (EndermanRenderer) (Object) this;

        BabyEndermanItemInHandLayer layer =
                new BabyEndermanItemInHandLayer(
                        renderer
                );

        ((LivingEntityRendererInvoker)
                (Object) renderer)
                .babyMobs$addLayer(layer);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/EnderMan;Lnet/minecraft/client/renderer/entity/state/EndermanRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabyEnderman(
            EnderMan entity,
            EndermanRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        ((BabyVariantRenderState) state)
                .babyMobs$setBabyVariant(
                        BabyEndermen.isBaby(entity)
                );
    }
}
