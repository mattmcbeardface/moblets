package com.pagesofatlas.babymobs.mixin;

import com.pagesofatlas.babymobs.BabyWitches;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownSplashPotion.class)
public abstract class ThrownSplashPotionMixin {

    @ModifyArg(
            method = "onHitAsPotion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;applyInstantaneousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V"
            ),
            index = 5
    )
    private double babyMobs$weakenBabyWitchInstantPotions(double vanillaScale) {
        ThrownSplashPotion potion =
                (ThrownSplashPotion) (Object) this;

        if (potion.getOwner() instanceof Witch witch
                && BabyWitches.isBaby(witch)) {
            return vanillaScale * 0.5D;
        }

        return vanillaScale;
    }
}
