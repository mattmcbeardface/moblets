package com.moblets.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderMan.class)
public interface EnderManDataAccessor {

    @Accessor("DATA_CREEPY")
    static EntityDataAccessor<Boolean> babyMobs$getDataCreepy() {
        throw new AssertionError();
    }
}
