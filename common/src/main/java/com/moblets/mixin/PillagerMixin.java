package com.moblets.mixin;

import com.moblets.BabyPillagers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pillager.class)
public abstract class PillagerMixin {

    @Inject(
            method = "performRangedAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void babyMobs$babyPillagerRangedAttack(
            LivingEntity target,
            float power,
            CallbackInfo ci
    ) {
        Pillager pillager = (Pillager) (Object) this;

        if (!BabyPillagers.isBaby(pillager)) {
            return;
        }

        InteractionHand hand =
                ProjectileUtil.getWeaponHoldingHand(
                        pillager,
                        Items.CROSSBOW
                );

        ItemStack weapon = pillager.getItemInHand(hand);

        if (weapon.getItem() instanceof CrossbowItem crossbow) {
            crossbow.performShooting(
                    pillager.level(),
                    pillager,
                    hand,
                    weapon,
                    1.6F,
                    20.0F,
                    target
            );
        }

        pillager.onCrossbowAttackPerformed();

        ci.cancel();
    }
}
