package com.moblets.mixin;

import com.moblets.BabySkeletons;
import com.moblets.balance.MobletBalance;
import com.moblets.registry.BalanceStat;
import com.moblets.taming.MobletTameState;

import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {

    private static final float WILD_BABY_INACCURACY =
            26.0F;

    /*
     * Smaller inaccuracy = tighter shot grouping.
     *
     * A tamed Skeleton gets 25% less spread than the
     * equivalent adult Skeleton difficulty value.
     */
    private static final float TAMED_INACCURACY_MULTIPLIER =
            0.75F;

    /*
     * Wild Moblets remain deliberately weaker.
     *
     * Tamed Moblets instead shoot at 125% of the same
     * adult Skeleton ranged-damage baseline.
     */
    private static final float WILD_DAMAGE_MULTIPLIER =
            0.50F;

    private static final float TAMED_DAMAGE_MULTIPLIER =
            1.25F;

    @Shadow
    protected abstract AbstractArrow getArrow(
            ItemStack projectile,
            float power,
            @Nullable ItemStack firingWeapon
    );

    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            ),
            index = 7
    )
    private float babyMobs$modifyBabySkeletonAccuracy(
            float vanillaInaccuracy
    ) {
        AbstractSkeleton skeleton =
                (AbstractSkeleton) (Object) this;

        if (!BabySkeletons.isBaby(skeleton)
                || !BabySkeletons.isRangedFamily(skeleton)) {
            return vanillaInaccuracy;
        }

        if (babyMobs$isTamed(skeleton)) {
            return MobletBalance.adjustedInaccuracy(
                    skeleton,
                    vanillaInaccuracy
                            * TAMED_INACCURACY_MULTIPLIER
            );
        }

        return MobletBalance.adjustedInaccuracy(
                skeleton,
                WILD_BABY_INACCURACY
        );
    }

    @Redirect(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;FLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;"
            )
    )
    private AbstractArrow babyMobs$modifyBabySkeletonArrow(
            AbstractSkeleton instance,
            ItemStack projectile,
            float power,
            @Nullable ItemStack firingWeapon
    ) {
        AbstractArrow arrow =
                this.getArrow(
                        projectile,
                        power,
                        firingWeapon
                );

        if (!BabySkeletons.isBaby(instance)
                || !BabySkeletons.isRangedFamily(instance)) {
            return arrow;
        }

        float multiplier =
                babyMobs$isTamed(instance)
                        ? TAMED_DAMAGE_MULTIPLIER
                        : WILD_DAMAGE_MULTIPLIER;

        multiplier *= (float) MobletBalance.multiplier(
                instance,
                BalanceStat.DAMAGE
        );

        arrow.setBaseDamageFromMob(
                power * multiplier
        );

        return arrow;
    }

    private static boolean babyMobs$isTamed(
            AbstractSkeleton skeleton
    ) {
        return skeleton instanceof MobletTameState state
                && state.moblets$isTamed();
    }
}
