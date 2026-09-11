package com.moblets.mixin;

import com.moblets.BabyWitches;
import com.moblets.balance.MobletBalance;
import com.moblets.registry.BalanceStat;
import com.moblets.taming.MobletTameState;

import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownSplashPotion.class)
public abstract class ThrownSplashPotionMixin {

    /*
     * Wild baby Witches remain intentionally weaker than adults.
     *
     * Tamed baby Witches are exempt so their support Healing
     * potion has normal vanilla strength.
     */
    @ModifyArg(
            method = "onHitAsPotion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;applyInstantenousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V"
            ),
            index = 5
    )
    private double moblets$weakenWildBabyWitchInstantPotions(
            double vanillaScale
    ) {
        ThrownSplashPotion potion =
                (ThrownSplashPotion) (Object) this;

        if (potion.getOwner()
                instanceof Witch witch
                && BabyWitches.isBaby(
                        witch)
                && !((MobletTameState) witch)
                        .moblets$isTamed()) {

            return vanillaScale
                    * 0.5D
                    * MobletBalance.multiplier(
                            witch,
                            BalanceStat.DAMAGE
                    );
        }

        return vanillaScale;
    }
}
