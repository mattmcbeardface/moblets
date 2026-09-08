package com.moblets.mixin.client;

import com.moblets.client.CreeperHelmetRenderState;

import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreeperRenderState.class)
public abstract class CreeperRenderStateMixin
        implements CreeperHelmetRenderState {

    @Unique
    private ItemStack moblets$helmet =
            ItemStack.EMPTY;

    @Override
    public ItemStack moblets$getHelmet() {
        return this.moblets$helmet;
    }

    @Override
    public void moblets$setHelmet(
            ItemStack helmet
    ) {
        this.moblets$helmet =
                helmet.isEmpty()
                        ? ItemStack.EMPTY
                        : helmet.copyWithCount(1);
    }
}
