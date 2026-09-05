package com.moblets.mixin;

import com.moblets.BabyTraderDebtFollowGoal;
import com.moblets.BabyTraderPaymentGoal;
import com.moblets.BabyTraderSalesGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin
        extends AbstractVillager {

    protected WanderingTraderMixin(
            EntityType<? extends AbstractVillager> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void babyMobs$addSalesBehavior(
            CallbackInfo ci
    ) {
        WanderingTrader trader =
                (WanderingTrader) (Object) this;

        /*
         * Payment outranks every other Moblet trader behavior so
         * a deliberately thrown payment is accepted immediately.
         */
        this.goalSelector.addGoal(
                0,
                new BabyTraderPaymentGoal(
                        trader
                )
        );

        /*
         * Active sales behavior outranks ordinary Wandering
         * Trader movement while an offer is being presented,
         * retrieved, or re-presented.
         */
        this.goalSelector.addGoal(
                1,
                new BabyTraderSalesGoal(
                        trader
                )
        );

        /*
         * Once the merchandise has been accepted and the player
         * tries to leave, the baby trader follows the debtor.
         */
        this.goalSelector.addGoal(
                1,
                new BabyTraderDebtFollowGoal(
                        trader
                )
        );
    }
}
