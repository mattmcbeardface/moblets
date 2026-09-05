package com.moblets.mixin;

import com.moblets.BabyPillagers;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @ModifyArg(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 1
    )
    private float babyMobs$reduceBabyPillagerArrowDamage(float vanillaDamage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;

        if (arrow.getOwner() instanceof Pillager pillager
                && BabyPillagers.isBaby(pillager)) {
            return vanillaDamage * 0.5F;
        }

        return vanillaDamage;
    }
}
