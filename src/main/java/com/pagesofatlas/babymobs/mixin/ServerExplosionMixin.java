package com.pagesofatlas.babymobs.mixin;

import com.pagesofatlas.babymobs.BabyCreepers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerExplosion;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

    @Accessor("source")
    protected abstract @Nullable Entity babyMobs$getSource();

    @ModifyArg(
            method = "hurtEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 2
    )
    private float babyMobs$reduceBabyCreeperDamage(float vanillaDamage) {
        Entity source = babyMobs$getSource();

        if (source instanceof Creeper creeper
                && BabyCreepers.isBaby(creeper)) {
            return vanillaDamage / 3.0F;
        }

        return vanillaDamage;
    }
}
