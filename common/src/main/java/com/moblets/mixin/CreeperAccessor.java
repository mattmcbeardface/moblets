package com.moblets.mixin;

import net.minecraft.world.entity.monster.Creeper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperAccessor {

    @Accessor("explosionRadius")
    void babyMobs$setExplosionRadius(
            int radius
    );

    @Accessor("swell")
    void babyMobs$setSwell(
            int swell
    );

    @Accessor("oldSwell")
    void babyMobs$setOldSwell(
            int swell
    );
}
