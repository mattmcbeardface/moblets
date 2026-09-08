package com.moblets.mixin;

import com.moblets.BabyPillagers;
import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletCuriosityGoal;
import com.moblets.taming.MobletFollowOwnerGoal;
import com.moblets.taming.MobletOwnerHurtByTargetGoal;
import com.moblets.taming.MobletOwnerHurtTargetGoal;
import com.moblets.taming.MobletSkeletonSentryGoal;
import com.moblets.taming.MobletStayGoal;
import com.moblets.taming.MobletTamedTargetGuardGoal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pillager.class)
public abstract class PillagerMixin
        extends AbstractIllager {

    protected PillagerMixin(
            EntityType<? extends AbstractIllager> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void moblets$addPillagerCompanionGoals(
            CallbackInfo ci
    ) {
        Pillager pillager =
                (Pillager) (Object) this;

        /*
         * Owner-directed combat.
         */
        this.targetSelector.addGoal(
                0,
                new MobletOwnerHurtByTargetGoal(
                        pillager
                )
        );

        this.targetSelector.addGoal(
                1,
                new MobletOwnerHurtTargetGoal(
                        pillager
                )
        );

        this.targetSelector.addGoal(
                2,
                new MobletTamedTargetGuardGoal(
                        pillager
                )
        );

        /*
         * Stay owns idle positioning.
         */
        this.goalSelector.addGoal(
                0,
                new MobletStayGoal(
                        pillager
                )
        );

        /*
         * Stay-mode proactive ranged sentry.
         */
        this.goalSelector.addGoal(
                1,
                new MobletSkeletonSentryGoal(
                        pillager
                )
        );

        /*
         * Wild baby Pillagers become curious about a player
         * presenting the configured taming item.
         */
        this.goalSelector.addGoal(
                1,
                new MobletCuriosityGoal(
                        pillager
                )
        );

        /*
         * Follow behavior for tamed Pillagers.
         *
         * Vanilla RangedCrossbowAttackGoal remains responsible
         * for charging, aiming and firing.
         */
        this.goalSelector.addGoal(
                2,
                new MobletFollowOwnerGoal(
                        pillager
                )
        );
    }

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
        Pillager pillager =
                (Pillager) (Object) this;

        if (!BabyPillagers.isBaby(pillager)) {
            return;
        }

        InteractionHand hand =
                ProjectileUtil.getWeaponHoldingHand(
                        pillager,
                        Items.CROSSBOW
                );

        ItemStack weapon =
                pillager.getItemInHand(hand);

        if (weapon.getItem()
                instanceof CrossbowItem crossbow) {

            float inaccuracy = 20.0F;

            if (pillager instanceof MobletTameState state
                    && state.moblets$isTamed()) {

                float adultInaccuracy =
                        14.0F
                                - pillager.level()
                                .getDifficulty()
                                .getId()
                                * 4.0F;

                /*
                 * Tamed Pillager:
                 * half the shot spread of an adult Pillager.
                 */
                inaccuracy =
                        Math.max(
                                0.0F,
                                adultInaccuracy * 0.50F
                        );
            }

            crossbow.performShooting(
                    pillager.level(),
                    pillager,
                    hand,
                    weapon,
                    1.6F,
                    inaccuracy,
                    target
            );
        }

        pillager.onCrossbowAttackPerformed();

        ci.cancel();
    }
}
