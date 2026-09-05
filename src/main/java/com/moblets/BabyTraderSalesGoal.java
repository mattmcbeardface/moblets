package com.moblets;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class BabyTraderSalesGoal extends Goal {

    private static final double DETECTION_DISTANCE =
            14.0D;

    /*
     * Stand far enough back that the merchandise lands outside
     * the player's automatic pickup radius.
     */
    private static final double STOP_DISTANCE =
            5.5D;

    private static final double STOP_DISTANCE_SQR =
            STOP_DISTANCE * STOP_DISTANCE;

    /*
     * Once the player has accepted the merchandise, let the
     * debt-follow goal take over when they actually leave.
     */
    private static final double DEBT_START_DISTANCE =
            8.0D;

    private static final double DEBT_START_DISTANCE_SQR =
            DEBT_START_DISTANCE
                    * DEBT_START_DISTANCE;

    private static final double APPROACH_SPEED =
            0.85D;

    /*
     * He waits one second after reaching the customer before
     * presenting the merchandise.
     */
    private static final int OFFER_PAUSE_TICKS =
            20;

    /*
     * Ignore his merchandise for five seconds and he decides
     * that clearly you just didn't understand the offer.
     */
    private static final int IGNORED_OFFER_DURATION_TICKS =
            100;

    /*
     * How close he gets before manually reclaiming his dropped
     * merchandise.
     */
    private static final double RETRIEVE_DISTANCE =
            1.5D;

    private static final double RETRIEVE_DISTANCE_SQR =
            RETRIEVE_DISTANCE
                    * RETRIEVE_DISTANCE;

    private static final double RETRIEVE_SPEED =
            0.90D;

    private enum Phase {
        APPROACHING,
        WAITING_FOR_PICKUP,
        RETRIEVING_OFFER
    }

    private final WanderingTrader trader;

    private Player customer;

    private Phase phase =
            Phase.APPROACHING;

    private ItemEntity outstandingOffer;

    /*
     * The merchandise is chosen ONCE for this sales interaction.
     *
     * If the customer ignores the offer and the trader retrieves
     * it, he presents the SAME item again instead of rerolling.
     */
    private ItemStack merchandise;

    private int offerPause;

    /*
     * World game time when the current offer was thrown.
     *
     * Using actual server game time instead of counting goal
     * ticks means the five-second retry timer keeps progressing
     * even if another AI goal briefly interrupts this one.
     */
    private long offerThrownGameTime =
            -1L;

    /*
     * Distinguishes the original lifetime offer from retries.
     *
     * The permanent sales-attempt tag is only claimed once.
     */
    private boolean initialOfferThrown;

    public BabyTraderSalesGoal(
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

        /*
         * Once the merchandise has been accepted, the debt
         * behavior owns the interaction.
         */
        if (BabyTraderSalesState.hasDebtor(
                this.trader
        )) {
            return false;
        }

        /*
         * SALES ATTEMPT ALREADY STARTED
         *
         * Vanilla Wandering Trader AI may temporarily preempt
         * this goal. Recover the existing interaction instead
         * of treating that interruption as the end of the sale.
         */
        if (BabyTraderSalesState
                .hasSalesAttempted(
                        this.trader
                )) {

            UUID customerUuid =
                    BabyTraderSalesState
                            .getSalesCustomerUuid(
                                    this.trader
                            );

            if (customerUuid == null) {
                return false;
            }

            ServerPlayer activeCustomer =
                    this.findSalesCustomer(
                            customerUuid
                    );

            if (activeCustomer == null) {
                return false;
            }

            this.customer =
                    activeCustomer;

            this.initialOfferThrown =
                    true;

            ItemEntity existingOffer =
                    this.findOutstandingOffer();

            if (existingOffer != null) {
                this.outstandingOffer =
                        existingOffer;

                        if (this.offerThrownGameTime < 0L) {
                    this.offerThrownGameTime =
                            this.trader.level()
                                    .getGameTime();
                }

                /*
                 * Recover the original merchandise too, so an
                 * interrupted retry does not reroll the item.
                 */
                if (this.merchandise == null) {
                    this.merchandise =
                            existingOffer
                                    .getItem()
                                    .copy();
                }

                /*
                 * Preserve RETRIEVING_OFFER if that is what we
                 * were already doing. Otherwise resume waiting.
                 */
                if (this.phase
                        != Phase.RETRIEVING_OFFER) {

                    this.phase =
                            Phase.WAITING_FOR_PICKUP;
                }
            } else {
                        /*
                 * We were interrupted after retrieving the item
                 * but before presenting it again.
                 */
                this.outstandingOffer =
                        null;

                this.offerThrownGameTime =
                        -1L;

                this.phase =
                        Phase.APPROACHING;

                this.offerPause =
                        OFFER_PAUSE_TICKS;
            }

            return true;
        }

        /*
         * BRAND-NEW sales attempt.
         */
        Player nearest =
                this.trader.level()
                        .getNearestPlayer(
                                this.trader,
                                DETECTION_DISTANCE
                        );

        if (nearest == null
                || !nearest.isAlive()
                || nearest.isSpectator()) {
            return false;
        }

        this.customer = nearest;

        this.phase =
                Phase.APPROACHING;

        this.offerPause =
                OFFER_PAUSE_TICKS;

        this.offerThrownGameTime = -1L;
        this.outstandingOffer = null;
        this.merchandise = null;
        this.initialOfferThrown = false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!BabyWanderingTraders.isBaby(
                this.trader
        )) {
            return false;
        }

        if (this.customer == null
                || !this.customer.isAlive()
                || this.customer.isSpectator()
                || this.customer.level()
                        != this.trader.level()) {
            return false;
        }

        /*
         * Before the FIRST offer is thrown, retain the ordinary
         * detection-distance rules.
         */
        if (!this.initialOfferThrown) {
            return !BabyTraderSalesState
                    .hasSalesAttempted(
                            this.trader
                    )
                    && this.trader.distanceToSqr(
                            this.customer
                    ) <= DETECTION_DISTANCE
                            * DETECTION_DISTANCE;
        }

        /*
         * No debt yet means the customer still has not accepted
         * the merchandise.
         *
         * Keep this salesman interaction alive indefinitely.
         * He may wait, retrieve, and re-present the same offer.
         */
        if (!BabyTraderSalesState.hasDebtor(
                this.trader
        )) {
            return true;
        }

        /*
         * Merchandise accepted.
         *
         * Stay planted while the customer is nearby. Once they
         * move about eight blocks away, this sales goal ends and
         * BabyTraderDebtFollowGoal takes control.
         */
        return this.trader.distanceToSqr(
                this.customer
        ) <= DEBT_START_DISTANCE_SQR;
    }

    @Override
    public void start() {
        /*
         * Temporarily abandon the caravan wander destination
         * while making this sales pitch.
         */
        this.trader.setWanderTarget(null);
    }

    @Override
    public void stop() {
        /*
         * Important:
         *
         * Goal.stop() may be called because a higher-priority
         * vanilla Wandering Trader behavior temporarily took
         * control.
         *
         * Do NOT erase the sales session here.
         */
        this.trader.getNavigation()
                .stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.customer == null) {
            return;
        }

        this.trader.getLookControl()
                .setLookAt(
                        this.customer,
                        30.0F,
                        30.0F
                );

        /*
         * The player finally accepted the merchandise.
         *
         * Stand here judging them until they actually walk away.
         */
        if (BabyTraderSalesState.hasDebtor(
                this.trader
        )) {
            this.trader.getNavigation()
                    .stop();

            return;
        }

        switch (this.phase) {
            case APPROACHING ->
                    this.tickApproaching();

            case WAITING_FOR_PICKUP ->
                    this.tickWaitingForPickup();

            case RETRIEVING_OFFER ->
                    this.tickRetrievingOffer();
        }
    }

    private void tickApproaching() {
        if (this.customer == null) {
            return;
        }

        double distance =
                this.trader.distanceToSqr(
                        this.customer
                );

        /*
         * Approach until he is within presentation distance.
         *
         * Do NOT try to maintain a narrow distance band here.
         * During retries the trader has just walked over to the
         * merchandise, so he may naturally be somewhat closer
         * to the customer than he was for the original pitch.
         *
         * Once he is within 5.5 blocks, simply plant his feet,
         * pause, and present the item again.
         */
        if (distance > STOP_DISTANCE_SQR) {
            this.offerPause =
                    OFFER_PAUSE_TICKS;

            this.trader.getNavigation()
                    .moveTo(
                            this.customer,
                            APPROACH_SPEED
                    );

            return;
        }

        this.trader.getNavigation()
                .stop();

        /*
         * Give him his little dramatic salesman's pause.
         */
        if (this.offerPause > 0) {
            this.offerPause--;
            return;
        }

        this.throwOffer();
    }

    private void tickWaitingForPickup() {
        this.trader.getNavigation()
                .stop();

        /*
         * If the item disappeared but no debt was created
         * (lava, cactus, command, etc.), simply try presenting
         * another copy instead of leaving the AI permanently
         * stuck.
         */
        if (this.outstandingOffer == null
                || this.outstandingOffer.isRemoved()) {

                this.outstandingOffer = null;

            this.phase =
                    Phase.APPROACHING;

            this.offerPause =
                    OFFER_PAUSE_TICKS;

            this.offerThrownGameTime =
                    -1L;

            return;
        }

        /*
         * Five REAL server seconds of silently staring at the
         * customer.
         *
         * This does not depend on how many ticks this particular
         * Goal happened to receive.
         */
        long elapsed =
                this.trader.level()
                        .getGameTime()
                        - this.offerThrownGameTime;

        if (elapsed < IGNORED_OFFER_DURATION_TICKS) {
            return;
        }

        /*
         * Fine. Apparently he has to SHOW you again.
         */
        this.phase =
                Phase.RETRIEVING_OFFER;

        this.trader.getNavigation()
                .moveTo(
                        this.outstandingOffer,
                        RETRIEVE_SPEED
                );
    }

    private void tickRetrievingOffer() {
        if (this.outstandingOffer == null
                || this.outstandingOffer.isRemoved()) {

                /*
             * The customer may have grabbed it while he was
             * walking over.
             *
             * If that happened, the pickup hook will have set
             * the debtor and the next tick will enter debt mode.
             *
             * Otherwise, prepare another presentation.
             */
            this.outstandingOffer = null;

            this.phase =
                    Phase.APPROACHING;

            this.offerPause =
                    OFFER_PAUSE_TICKS;

            this.offerThrownGameTime =
                    -1L;

            return;
        }

        double distance =
                this.trader.distanceToSqr(
                        this.outstandingOffer
                );

        if (distance > RETRIEVE_DISTANCE_SQR) {
            this.trader.getNavigation()
                    .moveTo(
                            this.outstandingOffer,
                            RETRIEVE_SPEED
                    );

            return;
        }

        /*
         * "Pick up" the merchandise.
         *
         * The trader isn't a Player, so ItemEntity.playerTouch
         * won't collect it naturally. Remove the original entity
         * once he physically reaches it.
         */
        this.outstandingOffer.discard();
        this.outstandingOffer = null;

        this.trader.getNavigation()
                .stop();

        /*
         * Now run back up to the customer and try the exact same
         * ridiculous sales pitch again.
         */
        this.phase =
                Phase.APPROACHING;

        this.offerPause =
                OFFER_PAUSE_TICKS;

        this.offerThrownGameTime =
                -1L;
    }

    /*
     * --------------------------------------------------------
     * MERCHANDISE TABLE
     * --------------------------------------------------------
     *
     * This little businessman has absolutely no understanding
     * of market value.
     *
     * 70% - common / mundane / useful
     * 22% - decent
     *  7% - valuable
     *  1% - ridiculous jackpot
     */
    private ItemStack chooseMerchandise() {
        int roll =
                this.trader.getRandom()
                        .nextInt(100);

        if (roll < 70) {
            return this.chooseCommonMerchandise();
        }

        if (roll < 92) {
            return this.chooseDecentMerchandise();
        }

        if (roll < 99) {
            return this.chooseValuableMerchandise();
        }

        return this.chooseJackpotMerchandise();
    }

    private ItemStack chooseCommonMerchandise() {
        return switch (
                this.trader.getRandom()
                        .nextInt(12)
        ) {
            case 0 ->
                    new ItemStack(
                            Items.BREAD,
                            1
                    );

            case 1 ->
                    new ItemStack(
                            Items.BAKED_POTATO,
                            2
                    );

            case 2 ->
                    new ItemStack(
                            Items.TORCH,
                            4
                    );

            case 3 ->
                    new ItemStack(
                            Items.STICK,
                            4
                    );

            case 4 ->
                    new ItemStack(
                            Items.STRING,
                            3
                    );

            case 5 ->
                    new ItemStack(
                            Items.COAL,
                            2
                    );

            case 6 ->
                    new ItemStack(
                            Items.LEATHER,
                            1
                    );

            case 7 ->
                    new ItemStack(
                            Items.PAPER,
                            3
                    );

            case 8 ->
                    new ItemStack(
                            Items.GLASS_BOTTLE,
                            2
                    );

            case 9 ->
                    new ItemStack(
                            Items.COOKED_CHICKEN,
                            1
                    );

            case 10 ->
                    new ItemStack(
                            Items.LANTERN,
                            1
                    );

            default ->
                    new ItemStack(
                            Items.FLINT,
                            2
                    );
        };
    }

    private ItemStack chooseDecentMerchandise() {
        return switch (
                this.trader.getRandom()
                        .nextInt(8)
        ) {
            case 0 ->
                    new ItemStack(
                            Items.IRON_INGOT,
                            2
                    );

            case 1 ->
                    new ItemStack(
                            Items.GOLD_INGOT,
                            2
                    );

            case 2 ->
                    new ItemStack(
                            Items.EMERALD,
                            2
                    );

            case 3 ->
                    new ItemStack(
                            Items.REDSTONE,
                            8
                    );

            case 4 ->
                    new ItemStack(
                            Items.LAPIS_LAZULI,
                            8
                    );

            case 5 ->
                    new ItemStack(
                            Items.ENDER_PEARL,
                            1
                    );

            case 6 ->
                    new ItemStack(
                            Items.EXPERIENCE_BOTTLE,
                            3
                    );

            default ->
                    new ItemStack(
                            Items.NAME_TAG,
                            1
                    );
        };
    }

    private ItemStack chooseValuableMerchandise() {
        return switch (
                this.trader.getRandom()
                        .nextInt(5)
        ) {
            case 0 ->
                    new ItemStack(
                            Items.DIAMOND,
                            1
                    );

            case 1 ->
                    new ItemStack(
                            Items.GOLDEN_APPLE,
                            1
                    );

            case 2 ->
                    new ItemStack(
                            Items.EMERALD_BLOCK,
                            1
                    );

            case 3 ->
                    new ItemStack(
                            Items.BLAZE_ROD,
                            2
                    );

            default ->
                    new ItemStack(
                            Items.NETHERITE_SCRAP,
                            1
                    );
        };
    }

    private ItemStack chooseJackpotMerchandise() {
        /*
         * One percent.
         *
         * He will happily exchange this for one dirt.
         * He is doing his best.
         */
        return new ItemStack(
                Items.NETHERITE_INGOT,
                1
        );
    }


    private ServerPlayer findSalesCustomer(
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

    private ItemEntity findOutstandingOffer() {
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
                                        24.0D,
                                        8.0D,
                                        24.0D
                                )
                )) {

            if (item.isRemoved()
                    || item.getItem().isEmpty()) {
                continue;
            }

            if (!BabyTraderSalesState
                    .isOfferFrom(
                            item,
                            this.trader
                    )) {
                continue;
            }

            double distance =
                    this.trader.distanceToSqr(
                            item
                    );

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = item;
            }
        }

        return nearest;
    }

    private void throwOffer() {
        if (!(this.trader.level()
                instanceof ServerLevel level)) {
            return;
        }

        if (this.customer == null) {
            return;
        }

        /*
         * Claim the ONE lifetime sales attempt only on the first
         * presentation.
         *
         * Subsequent presentations are retries of the same sale,
         * not additional sales attempts.
         */
        if (!this.initialOfferThrown) {
            if (!BabyTraderSalesState
                    .claimSalesAttempt(
                            this.trader
                    )) {
                return;
            }

            this.initialOfferThrown = true;

            BabyTraderSalesState
                    .setSalesCustomer(
                            this.trader,
                            this.customer
                    );
        }

        /*
         * Choose merchandise only once.
         *
         * Repeated presentations caused by the five-second
         * ignored-offer behavior use a COPY of this same stack.
         */
        if (this.merchandise == null) {
            this.merchandise =
                    this.chooseMerchandise();
        }

        ItemStack merchandise =
                this.merchandise.copy();

        Vec3 start =
                new Vec3(
                        this.trader.getX(),
                        this.trader.getEyeY()
                                - 0.25D,
                        this.trader.getZ()
                );

        Vec3 towardPlayer =
                this.customer.position()
                        .subtract(
                                this.trader.position()
                        );

        Vec3 horizontalDirection =
                new Vec3(
                        towardPlayer.x,
                        0.0D,
                        towardPlayer.z
                ).normalize();

        /*
         * Low, short toss onto the ground rather than directly
         * into the player's inventory.
         */
        /*
         * Very short little salesman's toss.
         *
         * The merchandise should land close to the trader rather
         * than halfway to the customer. This keeps it outside the
         * player's automatic pickup radius and also makes the
         * five-second retrieval much more reliable.
         */
        Vec3 velocity =
                new Vec3(
                        horizontalDirection.x
                                * 0.10D,
                        0.06D,
                        horizontalDirection.z
                                * 0.10D
                );

        ItemEntity offer =
                new ItemEntity(
                        level,
                        start.x,
                        start.y,
                        start.z,
                        merchandise,
                        velocity.x,
                        velocity.y,
                        velocity.z
                );

        /*
         * Only this prospective customer may pick it up.
         */
        offer.setTarget(
                this.customer.getUUID()
        );

        /*
         * Allows the pickup mixin to identify the baby trader
         * responsible for the offer.
         */
        offer.setThrower(
                this.trader
        );

        BabyTraderSalesState.tagOffer(
                offer,
                this.trader
        );

        level.addFreshEntity(offer);

        this.outstandingOffer =
                offer;

        this.phase =
                Phase.WAITING_FOR_PICKUP;

        this.offerThrownGameTime =
                level.getGameTime();

        this.trader.playSound(
                SoundEvents.WANDERING_TRADER_TRADE,
                1.0F,
                1.15F
        );

        this.trader.getNavigation()
                .stop();
    }
}
