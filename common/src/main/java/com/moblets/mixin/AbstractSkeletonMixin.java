package com.moblets.mixin;

import com.moblets.BabySkeletons;
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
    private float babyMobs$makeBabySkeletonAimTerrible(float vanillaInaccuracy) {
        AbstractSkeleton skeleton = (AbstractSkeleton) (Object) this;

        if (BabySkeletons.isBaby(skeleton)) {
            return 26.0F;
        }

        return vanillaInaccuracy;
    }

    @Redirect(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;FLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;"
            )
    )
    private AbstractArrow babyMobs$weakenBabySkeletonArrow(
            AbstractSkeleton instance,
            ItemStack projectile,
            float power,
            @Nullable ItemStack firingWeapon
    ) {
        AbstractArrow arrow = this.getArrow(projectile, power, firingWeapon);

        if (BabySkeletons.isBaby(instance)) {
            arrow.setBaseDamageFromMob(power * 0.5F);
        }

        return arrow;
    }
}
