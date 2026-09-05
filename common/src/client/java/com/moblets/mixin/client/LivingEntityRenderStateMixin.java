package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements BabyVariantRenderState {

    @Unique
    private boolean babyMobs$babyVariant;

    @Override
    public boolean babyMobs$isBabyVariant() {
        return this.babyMobs$babyVariant;
    }

    @Override
    public void babyMobs$setBabyVariant(boolean baby) {
        this.babyMobs$babyVariant = baby;
    }
}
