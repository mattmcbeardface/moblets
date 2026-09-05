package com.moblets.mixin.client;

import com.moblets.BabyPillagers;
import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Pillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IllagerRenderer.class)
public abstract class IllagerRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/illager/AbstractIllager;Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabyPillager(
            AbstractIllager entity,
            IllagerRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        boolean baby = entity instanceof Pillager pillager
                && BabyPillagers.isBaby(pillager);

        ((BabyVariantRenderState) state).babyMobs$setBabyVariant(baby);
    }
}
