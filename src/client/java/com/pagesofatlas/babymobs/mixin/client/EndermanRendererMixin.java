package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.BabyEndermen;
import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermanRenderer.class)
public abstract class EndermanRendererMixin {

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
                .babyMobs$setBabyVariant(BabyEndermen.isBaby(entity));
    }
}
