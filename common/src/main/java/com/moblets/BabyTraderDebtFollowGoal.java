package com.moblets;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public final class BabyTraderDebtFollowGoal
        extends Goal {

    /*
     * Taking the merchandise does NOT make him immediately
     * charge into the player's face.
     *
     * He only starts pestering once the debtor actually tries
     * to leave.
     */
    private static final double START_FOLLOW_DISTANCE =
            8.0D;

    /*
     * Once he catches up, he hangs several blocks behind the
     * player instead of physically crowding them.
     */
    /*
     * Once he has decided the player owes him, he stays nearby
     * like an annoyingly persistent little salesman.
     */
    private static final double STOP_FOLLOW_DISTANCE =
            2.0D;

    private static final double START_DISTANCE_SQR =
            START_FOLLOW_DISTANCE
                    * START_FOLLOW_DISTANCE;

    private static final double STOP_DISTANCE_SQR =
            STOP_FOLLOW_DISTANCE
                    * STOP_FOLLOW_DISTANCE;

    /*
     * Faster than an ordinary wandering trader, but not a
     * full-speed missile.
     *
     * The Moblet already has its +20% movement modifier.
     */
    private static final double FOLLOW_SPEED =
            0.75D;

    private static final int REPATH_INTERVAL =
            10;

    private final WanderingTrader trader;

    private ServerPlayer debtor;
    private int repathTicks;

    public BabyTraderDebtFollowGoal(
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
        if (!BabyWanderingTraders
                .isBaby(this.trader)) {
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

        /*
         * This is the important behavior:
         *
         * Picking up the item alone does nothing.
         * The player has to WALK AWAY before the little salesman
         * decides they are trying to skip out on the bill.
         */
        if (this.trader.distanceToSqr(
                player
        ) <= START_DISTANCE_SQR) {
            return false;
        }

        this.debtor = player;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.debtor == null) {
            return false;
        }

        if (!this.debtor.isAlive()
                || this.debtor.isSpectator()) {
            return false;
        }

        if (this.debtor.level()
                != this.trader.level()) {
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
         * Once the stalking behavior starts, KEEP the goal
         * active until the debt disappears or the player is no
         * longer available.
         *
         * The tick method handles the two-block stopping
         * distance. Keeping this goal active prevents vanilla
         * wandering from taking over and making the trader
         * repeatedly walk away and come back.
         */
        return true;
    }

    @Override
    public void start() {
        /*
         * Cancel the caravan's original wander destination
         * while he is chasing down his customer.
         */
        this.trader.setWanderTarget(null);

        this.repathTicks = 0;
    }

    @Override
    public void stop() {
        this.trader.getNavigation()
                .stop();

        this.debtor = null;
        this.repathTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.debtor == null) {
            return;
        }

        this.trader.getLookControl()
                .setLookAt(
                        this.debtor,
                        30.0F,
                        30.0F
                );

        if (this.trader.distanceToSqr(
                this.debtor
        ) <= STOP_DISTANCE_SQR) {

            this.trader.getNavigation()
                    .stop();

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
                            this.debtor,
                            FOLLOW_SPEED
                    );
        }
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

        /*
         * No magical cross-dimensional stalking.
         * If the player returns to this dimension later, the
         * persistent debt is still waiting for them.
         */
        if (player.level() != level) {
            return null;
        }

        return player;
    }
}
