package com.moblets.mixin;

import com.moblets.BabyWitches;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Witch.class)
public abstract class WitchMixin {

    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            ),
            index = 8
    )
    private float babyMobs$reduceBabyWitchAccuracy(float vanillaInaccuracy) {
        Witch witch = (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)) {
            return 40.0F;
        }

        return vanillaInaccuracy;
    }

    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            ),
            index = 2
    )
    private ItemStack babyMobs$weakenBabyWitchTimedPotions(ItemStack potionStack) {
        Witch witch = (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)) {
            potionStack.set(DataComponents.POTION_DURATION_SCALE, 0.5F);
        }

        return potionStack;
    }
}
