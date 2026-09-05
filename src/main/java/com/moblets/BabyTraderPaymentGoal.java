package com.moblets;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;

public final class BabyTraderPaymentGoal
        extends Goal {

    /*
     * Payment must actually be thrown somewhere reasonably near
     * the little salesman.
     *
     * We're not vacuuming up every item the debtor has dropped
     * somewhere in the world.
     */
    private static final double PAYMENT_SEARCH_DISTANCE =
            12.0D;

    private static final double PAYMENT_VERTICAL_DISTANCE =
            6.0D;

    /*
     * Once he gets this close to the dropped item, he accepts
     * exactly ONE item as payment.
     */
    private static final double ACCEPT_DISTANCE =
            1.5D;

    private static final double ACCEPT_DISTANCE_SQR =
            ACCEPT_DISTANCE
                    * ACCEPT_DISTANCE;

    private static final double PAYMENT_RUN_SPEED =
            1.0D;

    private static final int REPATH_INTERVAL =
            5;

    private final WanderingTrader trader;

    private ServerPlayer debtor;
    private ItemEntity payment;
    private int repathTicks;

    public BabyTraderPaymentGoal(
            WanderingTrader trader
    ) {
        this.trader = trader;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!BabyWanderingTraders.isBaby(
                this.trader
        )) {
            return false;
        }

        UUID debtorUuid =
                BabyTraderSalesState
                        .getDebtorUuid(
                                this.trader
                        );

        if (debtorUuid == null) {
            return false;
        }

        ServerPlayer player =
                this.findDebtor(
                        debtorUuid
                );

        if (player == null) {
            return false;
        }

        ItemEntity candidate =
                this.findPayment(
                        player
                );

        if (candidate == null) {
            return false;
        }

        this.debtor = player;
        this.payment = candidate;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.debtor == null
                || this.payment == null) {
            return false;
        }

        if (!this.debtor.isAlive()
                || this.debtor.isSpectator()) {
            return false;
        }

        if (this.payment.isRemoved()
                || this.payment.getItem()
                        .isEmpty()) {
            return false;
        }

        UUID debtorUuid =
                BabyTraderSalesState
                        .getDebtorUuid(
                                this.trader
                        );

        if (debtorUuid == null
                || !debtorUuid.equals(
                        this.debtor.getUUID()
                )) {
            return false;
        }

        /*
         * If somebody picks it up again, or ownership no longer
         * identifies our debtor as the thrower, it is no longer
         * valid payment.
         */
        return this.wasThrownBy(
                this.payment,
                this.debtor
        );
    }

    @Override
    public void start() {
        this.repathTicks = 0;

        /*
         * Whatever he was doing previously is suddenly much less
         * important. Somebody is trying to PAY HIM.
         */
        this.trader.setWanderTarget(null);
    }

    @Override
    public void stop() {
        this.trader.getNavigation()
                .stop();

        this.debtor = null;
        this.payment = null;
        this.repathTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.debtor == null
                || this.payment == null) {
            return;
        }

        if (this.payment.isRemoved()) {
            return;
        }

        this.trader.getLookControl()
                .setLookAt(
                        this.payment,
                        30.0F,
                        30.0F
                );

        double distance =
                this.trader.distanceToSqr(
                        this.payment
                );

        if (distance <= ACCEPT_DISTANCE_SQR) {
            this.acceptPayment();
            return;
        }

        if (--this.repathTicks <= 0
                || this.trader
                        .getNavigation()
                        .isDone()) {

            this.repathTicks =
                    REPATH_INTERVAL;

            this.trader.getNavigation()
                    .moveTo(
                            this.payment,
                            PAYMENT_RUN_SPEED
                    );
        }
    }

    /*
     * Find the nearest dropped item that was actually thrown by
     * THIS debtor.
     *
     * Random world drops, mob drops and another player's items
     * do not count.
     */
    private ItemEntity findPayment(
            ServerPlayer player
    ) {
        if (!(this.trader.level()
                instanceof ServerLevel level)) {
            return null;
        }

        ItemEntity nearest = null;
        double nearestDistance =
                Double.MAX_VALUE;

        for (ItemEntity item :
                level.getEntitiesOfClass(
                        ItemEntity.class,
                        this.trader
                                .getBoundingBox()
                                .inflate(
                                        PAYMENT_SEARCH_DISTANCE,
                                        PAYMENT_VERTICAL_DISTANCE,
                                        PAYMENT_SEARCH_DISTANCE
                                )
                )) {

            if (item.isRemoved()
                    || item.getItem()
                            .isEmpty()) {
                continue;
            }

            if (!this.wasThrownBy(
                    item,
                    player
            )) {
                continue;
            }

            double distance =
                    this.trader.distanceToSqr(
                            item
                    );

            if (distance
                    < nearestDistance) {
                nearestDistance =
                        distance;

                nearest = item;
            }
        }

        return nearest;
    }

    private boolean wasThrownBy(
            ItemEntity item,
            ServerPlayer player
    ) {
        Entity owner =
                item.getOwner();

        return owner != null
                && owner.getUUID()
                        .equals(
                                player.getUUID()
                        );
    }

    private void acceptPayment() {
        if (this.payment == null
                || this.debtor == null) {
            return;
        }

        if (!this.wasThrownBy(
                this.payment,
                this.debtor
        )) {
            return;
        }

        ItemStack stack =
                this.payment.getItem();

        if (stack.isEmpty()) {
            return;
        }

        /*
         * His understanding of economics is terrible, but at
         * least he is polite enough to take only ONE item.
         *
         * 1 diamond?
         * Paid.
         *
         * 1 dirt?
         * Also paid.
         *
         * 1 bread he literally just gave you?
         * Excellent business.
         */
        if (stack.getCount() <= 1) {
            this.payment.discard();
        } else {
            ItemStack remainder =
                    stack.copy();

            remainder.shrink(1);

            this.payment.setItem(
                    remainder
            );
        }

        /*
         * The transaction is complete forever.
         *
         * sales_attempted remains on the trader, so clearing the
         * debtor cannot cause another sales pitch later.
         */
        BabyTraderSalesState.clearDebtor(
                this.trader
        );

        /*
         * The sale is genuinely finished now. Remove the
         * persistent customer marker so the sales goal cannot
         * ever resume this transaction.
         */
        BabyTraderSalesState.clearSalesCustomer(
                this.trader
        );

        this.trader.getNavigation()
                .stop();

        this.trader.getLookControl()
                .setLookAt(
                        this.debtor,
                        30.0F,
                        30.0F
                );

        this.trader.playSound(
                SoundEvents.WANDERING_TRADER_YES,
                1.0F,
                1.2F
        );

        /*
         * Vanilla green happy-villager particles make it
         * immediately obvious that this tiny businessman
         * considers the transaction complete.
         */
        if (this.trader.level()
                instanceof ServerLevel level) {

            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.trader.getX(),
                    this.trader.getEyeY(),
                    this.trader.getZ(),
                    12,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.05D
            );
        }

        this.payment = null;
    }

    private ServerPlayer findDebtor(
            UUID uuid
    ) {
        if (!(this.trader.level()
                instanceof ServerLevel level)) {
            return null;
        }

        ServerPlayer player =
                level.getServer()
                        .getPlayerList()
                        .getPlayer(uuid);

        if (player == null
                || !player.isAlive()
                || player.isSpectator()) {
            return null;
        }

        if (player.level() != level) {
            return null;
        }

        return player;
    }
}
