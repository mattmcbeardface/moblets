package com.moblets.mixin;

import com.moblets.BabyTraderSalesState;
import com.moblets.BabyWanderingTraders;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    /*
     * ItemEntity.playerTouch only calls Player.onItemPickup(...)
     * after the inventory insertion succeeded.
     *
     * Inject immediately before that call so simply touching an
     * item with a full inventory does NOT create a debt.
     */
    @Inject(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;onItemPickup(Lnet/minecraft/world/entity/item/ItemEntity;)V"
            )
    )
    private void babyMobs$recordBabyTraderDebt(
            Player player,
            CallbackInfo ci
    ) {
        ItemEntity item =
                (ItemEntity) (Object) this;

        Entity owner =
                item.getOwner();

        if (!(owner instanceof
                WanderingTrader trader)) {
            return;
        }

        if (!BabyWanderingTraders
                .isBaby(trader)) {
            return;
        }

        /*
         * Normal items thrown by a trader should never trigger
         * this. It must be the specifically tagged merchandise
         * from this exact baby trader.
         */
        if (!BabyTraderSalesState
                .isOfferFrom(
                        item,
                        trader
                )) {
            return;
        }

        BabyTraderSalesState.setDebtor(
                trader,
                player
        );
    }
}
