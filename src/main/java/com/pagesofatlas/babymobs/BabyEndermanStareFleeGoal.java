package com.pagesofatlas.babymobs;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class BabyEndermanStareFleeGoal extends Goal {
    private static final double DETECTION_DISTANCE = 24.0D;
    private static final double DETECTION_DISTANCE_SQR =
            DETECTION_DISTANCE * DETECTION_DISTANCE;

    private static final double RESET_DISTANCE = 28.0D;
    private static final double RESET_DISTANCE_SQR =
            RESET_DISTANCE * RESET_DISTANCE;

    /*
     * Give the player a real opportunity to catch the thief.
     *
     * 30 ticks = 1.5 seconds of "OH GOD EYE CONTACT"
     * before the Moblet actually bolts.
     */
    private static final int STARTLE_TICKS = 30;

    /*
     * The Moblet already has +25% base movement speed.
     * This should still feel quick without becoming impossible
     * to chase.
     */
    private static final double FLEE_SPEED = 1.10D;

    /*
     * Matches vanilla Enderman stare sensitivity.
     */
    private static final double LOOK_THRESHOLD = 0.025D;

    private final EnderMan enderman;
    private final PathNavigation navigation;

    private Player player;
    private Path path;
    private int startleTicks;

    public BabyEndermanStareFleeGoal(
            EnderMan enderman
    ) {
        this.enderman = enderman;
        this.navigation = enderman.getNavigation();

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (!BabyEndermen.isBaby(this.enderman)) {
            return false;
        }

        this.player = this.findStaringPlayer();

        if (this.player == null) {
            return false;
        }

        /*
         * Don't require the escape path yet.
         *
         * First we want the Moblet to stop and react.
         */
        this.path = null;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return BabyEndermen.isBaby(this.enderman)
                && this.player != null
                && this.player.isAlive()
                && this.enderman.distanceToSqr(this.player)
                        < RESET_DISTANCE_SQR;
    }

    @Override
    public void start() {
        /*
         * Clear any stale target FIRST.
         *
         * Vanilla setTarget(null) clears the Enderman's creepy
         * and stared-at flags, so this must happen before we
         * turn the juvenile stare reaction on.
         */
        this.enderman.setTarget(null);

        this.navigation.stop();

        /*
         * Preserve the classic Enderman reaction:
         *
         * stare -> creepy/shaky -> scream
         *
         * But instead of becoming hostile, the Moblet freezes
         * in panic before running away.
         */
        this.enderman.setBeingStaredAt();

        BabyEndermen.setCreepy(
                this.enderman,
                true
        );

        this.startleTicks = STARTLE_TICKS;
    }

    @Override
    public void stop() {
        this.navigation.stop();

        /*
         * Vanilla setTarget(null) conveniently resets both
         * DATA_CREEPY and DATA_STARED_AT.
         */
        this.enderman.setTarget(null);

        this.path = null;
        this.player = null;
        this.startleTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.player == null) {
            return;
        }

        /*
         * Phase 1:
         *
         * Freeze, stare back, shake and scream.
         */
        if (this.startleTicks > 0) {
            this.startleTicks--;

            this.navigation.stop();

            this.enderman.getLookControl().setLookAt(
                    this.player,
                    30.0F,
                    30.0F
            );

            /*
             * Generate the escape route near the end of the
             * reaction so it can bolt immediately afterward.
             */
            if (this.startleTicks == 1) {
                this.makeEscapePath();
            }

            return;
        }

        /*
         * Phase 2:
         *
         * Panic and run.
         */
        if (this.path != null
                && this.navigation.isDone()) {

            this.navigation.moveTo(
                    this.path,
                    FLEE_SPEED
            );

            this.path = null;
        }

        if (this.navigation.isDone()
                && this.enderman.distanceToSqr(this.player)
                        < RESET_DISTANCE_SQR) {

            if (this.makeEscapePath()) {
                this.navigation.moveTo(
                        this.path,
                        FLEE_SPEED
                );

                this.path = null;
            }
        }

        this.navigation.setSpeedModifier(
                FLEE_SPEED
        );
    }

    private Player findStaringPlayer() {
        List<Player> players =
                this.enderman.level().getEntitiesOfClass(
                        Player.class,
                        this.enderman
                                .getBoundingBox()
                                .inflate(
                                        DETECTION_DISTANCE,
                                        12.0D,
                                        DETECTION_DISTANCE
                                ),
                        player ->
                                player.isAlive()
                                && !player.isSpectator()
                                && LivingEntity
                                        .PLAYER_NOT_WEARING_DISGUISE_ITEM
                                        .test(player)
                                && isLookingAtEnderman(
                                        this.enderman,
                                        player
                                )
                );

        Player nearest = null;
        double nearestDistance =
                DETECTION_DISTANCE_SQR;

        for (Player candidate : players) {
            double distance =
                    this.enderman.distanceToSqr(
                            candidate
                    );

            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private boolean makeEscapePath() {
        if (this.player == null) {
            return false;
        }

        Vec3 escape = DefaultRandomPos.getPosAway(
                this.enderman,
                16,
                7,
                this.player.position()
        );

        if (escape == null) {
            return false;
        }

        if (this.player.distanceToSqr(
                escape.x,
                escape.y,
                escape.z
        ) <= this.player.distanceToSqr(
                this.enderman
        )) {
            return false;
        }

        this.path = this.navigation.createPath(
                escape.x,
                escape.y,
                escape.z,
                0
        );

        return this.path != null;
    }

    private static boolean isLookingAtEnderman(
            EnderMan enderman,
            Player player
    ) {
        Vec3 look =
                player.getViewVector(1.0F).normalize();

        Vec3 towardEnderman = new Vec3(
                enderman.getX() - player.getX(),
                enderman.getEyeY() - player.getEyeY(),
                enderman.getZ() - player.getZ()
        );

        double distance =
                towardEnderman.length();

        if (distance < 0.0001D) {
            return true;
        }

        towardEnderman =
                towardEnderman.normalize();

        double alignment =
                look.dot(towardEnderman);

        return alignment
                > 1.0D
                - LOOK_THRESHOLD / distance
                && player.hasLineOfSight(enderman);
    }
}
