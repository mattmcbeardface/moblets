package com.moblets.mixin.client;

import com.moblets.BabyCreepers;
import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperRenderer.class)
public abstract class CreeperRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/Creeper;Lnet/minecraft/client/renderer/entity/state/CreeperRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabyCreeper(
            Creeper entity,
            CreeperRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        ((BabyVariantRenderState) state)
                .babyMobs$setBabyVariant(BabyCreepers.isBaby(entity));
    }
}
