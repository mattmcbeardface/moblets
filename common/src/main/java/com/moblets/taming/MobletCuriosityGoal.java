package com.moblets.taming;

import java.util.EnumSet;
import java.util.UUID;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public final class MobletCuriosityGoal extends Goal {
    private static final double NOTICE_RANGE = 10.0D;
    private static final double CONTINUE_RANGE = 12.0D;

    private static final double APPROACH_DISTANCE = 2.75D;
    private static final double APPROACH_DISTANCE_SQR =
            APPROACH_DISTANCE * APPROACH_DISTANCE;

    /*
     * Once stopped, don't restart navigation for every tiny
     * player movement. Resume following only after the player
     * creates a little separation.
     */
    private static final double FOLLOW_RESUME_DISTANCE = 3.5D;
    private static final double FOLLOW_RESUME_DISTANCE_SQR =
            FOLLOW_RESUME_DISTANCE * FOLLOW_RESUME_DISTANCE;

    private static final double APPROACH_SPEED = 0.85D;

    /*
     * One-second hesitation after the player puts the desired
     * item away before normal hostility returns.
     */
    private static final int LOST_ITEM_GRACE_TICKS = 20;

    /*
     * Prevent immediately becoming curious again after the
     * player has just attacked it.
     */
    private static final int RECENT_ATTACK_TICKS = 40;

    private final Mob mob;

    private Player curiousPlayer;
    private UUID blockedPlayerUuid;

    private int missingItemTicks;
    private int hurtTimestampAtStart;
    private boolean reachedPlayer;

    public MobletCuriosityGoal(Mob mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        MobletTamingRule rule = rule();

        if (rule == null || !rule.appliesTo(this.mob)) {
            return false;
        }

        MobletTameState tameState =
                (MobletTameState) this.mob;

        if (tameState.moblets$isTamed()) {
            return false;
        }

        MobletDefinition definition =
                MobletRegistry.byEntityType(
                        this.mob.getType()
                );

        if (definition == null
                || !MobletsConfig.tamingEnabled(definition)) {
            return false;
        }

        clearExpiredBlock(rule);

        Player player =
                this.mob.level().getNearestPlayer(
                        this.mob.getX(),
                        this.mob.getY(),
                        this.mob.getZ(),
                        NOTICE_RANGE,
                        entity -> {
                            if (!(entity instanceof Player candidate)) {
                                return false;
                            }

                            if (!candidate.isAlive()
                                    || candidate.isSpectator()) {
                                return false;
                            }

                            if (this.blockedPlayerUuid != null
                                    && this.blockedPlayerUuid.equals(
                                            candidate.getUUID()
                                    )) {
                                return false;
                            }

                            return isHoldingAcceptedItem(
                                    candidate,
                                    rule
                            );
                        }
                );

        if (player == null) {
            return false;
        }

        /*
         * If this exact player has just attacked the Moblet,
         * don't let the held item instantly erase that hostility.
         */
        LivingEntity attacker =
                this.mob.getLastHurtByMob();

        if (attacker == player) {
            int hurtAge =
                    this.mob.tickCount
                            - this.mob.getLastHurtByMobTimestamp();

            if (hurtAge >= 0
                    && hurtAge < RECENT_ATTACK_TICKS) {
                this.blockedPlayerUuid =
                        player.getUUID();

                return false;
            }
        }

        this.curiousPlayer = player;
        this.missingItemTicks = 0;
        this.reachedPlayer = false;
        this.hurtTimestampAtStart =
                this.mob.getLastHurtByMobTimestamp();

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.curiousPlayer == null
                || !this.curiousPlayer.isAlive()
                || this.curiousPlayer.isSpectator()) {
            return false;
        }

        MobletTameState tameState =
                (MobletTameState) this.mob;

        if (tameState.moblets$isTamed()) {
            return false;
        }

        /*
         * Once an attempt has started, hold this goal through
         * the entire consideration animation regardless of
         * whether the consumed item disappeared from the hand.
         */
        if (tameState.moblets$isConsideringTame()) {
            return true;
        }

        MobletTamingRule rule = rule();

        if (rule == null || !rule.appliesTo(this.mob)) {
            return false;
        }

        if (this.mob.distanceToSqr(this.curiousPlayer)
                > CONTINUE_RANGE * CONTINUE_RANGE) {
            return false;
        }

        /*
         * Any new attack interrupts curiosity immediately.
         */
        int currentHurtTimestamp =
                this.mob.getLastHurtByMobTimestamp();

        if (currentHurtTimestamp
                != this.hurtTimestampAtStart) {
            LivingEntity attacker =
                    this.mob.getLastHurtByMob();

            if (attacker == this.curiousPlayer) {
                this.blockedPlayerUuid =
                        this.curiousPlayer.getUUID();
            }

            return false;
        }

        if (isHoldingAcceptedItem(
                this.curiousPlayer,
                rule
        )) {
            this.missingItemTicks = 0;
            return true;
        }

        this.missingItemTicks++;

        return this.missingItemTicks
                <= LOST_ITEM_GRACE_TICKS;
    }

    @Override
    public void start() {
        ((MobletTameState) this.mob)
                .moblets$setCuriousPlayerUuid(
                        this.curiousPlayer.getUUID()
                );

        suppressHostility();

        /*
         * RangedBowAttackGoal leaves its last strafe inputs
         * behind in xxa/zza when combat ends. Clear both the
         * MoveControl operation and those movement inputs so
         * curiosity starts from a true standstill.
         */
        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWait();

        this.mob.xxa = 0.0F;
        this.mob.zza = 0.0F;

        var movement = this.mob.getDeltaMovement();

        this.mob.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.mob.stopUsingItem();

        ((MobletTameState) this.mob)
                .moblets$setCuriousPlayerUuid(null);

        this.curiousPlayer = null;
        this.missingItemTicks = 0;
        this.reachedPlayer = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        MobletTameState tameState =
                (MobletTameState) this.mob;

        if (tameState.moblets$isConsideringTame()) {
            MobletTaming.tickConsideration(
                    this.mob
            );
            return;
        }

        if (this.curiousPlayer == null) {
            return;
        }

        suppressHostility();

        MobletTamingRule rule = rule();

        if (rule == null) {
            return;
        }

        /*
         * Once the item disappears, hesitate and watch rather
         * than instantly snapping back into combat.
         */
        if (!isHoldingAcceptedItem(
                this.curiousPlayer,
                rule
        )) {
            this.mob.getNavigation().stop();

            this.mob.getLookControl().setLookAt(
                    this.curiousPlayer,
                    30.0F,
                    30.0F
            );

            return;
        }

        double distanceSqr =
                this.mob.distanceToSqr(
                        this.curiousPlayer
                );

        /*
         * Once the Moblet reaches conversational distance,
         * stop trying to path all the way into the player.
         *
         * Without this latch, pathfinding can keep trying to
         * solve the final few blocks and make the Moblet orbit
         * or strafe around the player.
         */
        /*
         * If the player walks away after we've already reached
         * them, resume following once enough separation opens up.
         *
         * The different stop/resume distances prevent constant
         * start-stop jitter around one exact boundary.
         */
        if (this.reachedPlayer
                && distanceSqr
                        > FOLLOW_RESUME_DISTANCE_SQR) {
            this.reachedPlayer = false;
        }

        if (!this.reachedPlayer
                && distanceSqr
                        <= APPROACH_DISTANCE_SQR) {
            this.reachedPlayer = true;
            this.mob.getNavigation().stop();
        }

        if (!this.reachedPlayer) {
            this.mob.getNavigation().moveTo(
                    this.curiousPlayer,
                    APPROACH_SPEED
            );
        } else {
            this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(
                this.curiousPlayer,
                30.0F,
                30.0F
        );
    }

    private void suppressHostility() {
        this.mob.setTarget(null);
        this.mob.setAggressive(false);
        this.mob.stopUsingItem();
    }

    private MobletTamingRule rule() {
        return MobletTamingRegistry.byEntityType(
                this.mob.getType()
        );
    }

    private void clearExpiredBlock(
            MobletTamingRule rule
    ) {
        if (this.blockedPlayerUuid == null) {
            return;
        }

        Player blocked =
                this.mob.level().getPlayerByUUID(
                        this.blockedPlayerUuid
                );

        if (blocked == null
                || !isHoldingAcceptedItem(
                        blocked,
                        rule
                )) {
            this.blockedPlayerUuid = null;
        }
    }

    private static boolean isHoldingAcceptedItem(
            Player player,
            MobletTamingRule rule
    ) {
        return rule.accepts(
                player.getItemInHand(
                        InteractionHand.MAIN_HAND
                )
        ) || rule.accepts(
                player.getItemInHand(
                        InteractionHand.OFF_HAND
                )
        );
    }
}
