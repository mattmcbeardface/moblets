package com.moblets.taming;

import com.moblets.BabyWitches;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletWitchFleeGoal
        extends Goal {

    private static final double SAFE_DISTANCE =
            12.0D;

    private static final double SAFE_DISTANCE_SQR =
            SAFE_DISTANCE * SAFE_DISTANCE;

    private static final double FLEE_STEP_DISTANCE =
            7.0D;

    private static final double FLEE_SPEED =
            1.15D;

    private static final int MAX_FLEE_TICKS =
            100;

    private static final int REPATH_INTERVAL =
            8;

    /*
     * Straight away first. Angles are only terrain/pathing
     * fallbacks if directly away is blocked.
     */
    private static final double[] FLEE_ANGLES = {
            0.0D,
            25.0D,
            -25.0D,
            45.0D,
            -45.0D,
            65.0D,
            -65.0D
    };

    private final Witch witch;

    private LivingEntity attacker;
    private int lastHandledHurtTimestamp;
    private int fleeTicks;
    private int repathCooldown;

    public MobletWitchFleeGoal(
            Witch witch
    ) {
        this.witch = witch;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!isTamedBaby()) {
            return false;
        }

        LivingEntity candidate =
                this.witch.getLastHurtByMob();

        int timestamp =
                this.witch.getLastHurtByMobTimestamp();

        /*
         * Only react to a NEW actual attack.
         *
         * Owner attacking something does not count.
         */
        if (timestamp == this.lastHandledHurtTimestamp
                || !isValidAttacker(candidate)) {

            return false;
        }

        this.attacker =
                candidate;

        this.lastHandledHurtTimestamp =
                timestamp;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return isTamedBaby()
                && this.attacker != null
                && this.attacker.isAlive()
                && this.fleeTicks > 0
                && this.witch.distanceToSqr(
                        this.attacker
                ) < SAFE_DISTANCE_SQR;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.fleeTicks =
                MAX_FLEE_TICKS;

        this.repathCooldown =
                0;

        /*
         * She is fleeing, not fighting.
         */
        this.witch.setTarget(null);

        chooseEscapePath();
    }

    @Override
    public void tick() {
        --this.fleeTicks;

        /*
         * Never allow fleeing to turn into combat.
         */
        this.witch.setTarget(null);

        if (this.attacker == null) {
            return;
        }

        if (this.repathCooldown > 0) {
            --this.repathCooldown;
        }

        if (this.repathCooldown <= 0
                || this.witch.getNavigation()
                        .isDone()) {

            chooseEscapePath();

            this.repathCooldown =
                    REPATH_INTERVAL;
        }
    }

    @Override
    public void stop() {
        this.witch.getNavigation()
                .stop();

        this.attacker =
                null;

        this.fleeTicks =
                0;
    }

    private void chooseEscapePath() {
        if (this.attacker == null) {
            return;
        }

        double awayX =
                this.witch.getX()
                        - this.attacker.getX();

        double awayZ =
                this.witch.getZ()
                        - this.attacker.getZ();

        double length =
                Math.sqrt(
                        awayX * awayX
                                + awayZ * awayZ
                );

        if (length < 0.001D) {
            return;
        }

        awayX /= length;
        awayZ /= length;

        for (double angleDegrees :
                FLEE_ANGLES) {

            double radians =
                    Math.toRadians(
                            angleDegrees
                    );

            double cos =
                    Math.cos(radians);

            double sin =
                    Math.sin(radians);

            double directionX =
                    awayX * cos
                            - awayZ * sin;

            double directionZ =
                    awayX * sin
                            + awayZ * cos;

            double x =
                    this.witch.getX()
                            + directionX
                            * FLEE_STEP_DISTANCE;

            double z =
                    this.witch.getZ()
                            + directionZ
                            * FLEE_STEP_DISTANCE;

            /*
             * Small vertical search so stairs/slopes don't
             * prevent an otherwise valid escape.
             */
            for (int offset = 0;
                 offset <= 2;
                 ++offset) {

                if (tryDestination(
                        x,
                        this.witch.getY()
                                + offset,
                        z)) {

                    return;
                }

                if (offset > 0
                        && tryDestination(
                                x,
                                this.witch.getY()
                                        - offset,
                                z)) {

                    return;
                }
            }
        }

        /*
         * If there's genuinely nowhere safe to path, don't
         * substitute combat behavior.
         */
        this.witch.getNavigation()
                .stop();

        this.witch.getMoveControl()
                .setWait();
    }

    private boolean tryDestination(
            double x,
            double y,
            double z
    ) {
        BlockPos destination =
                new BlockPos(
                        (int) Math.floor(x),
                        (int) Math.floor(y),
                        (int) Math.floor(z)
                );

        Path path =
                this.witch.getNavigation()
                        .createPath(
                                destination,
                                0
                        );

        if (!MobletSafeNavigation
                .isSafePath(path)) {

            return false;
        }

        return MobletSafeNavigation
                .moveToSafely(
                        this.witch,
                        path,
                        FLEE_SPEED
                );
    }

    private boolean isValidAttacker(
            LivingEntity candidate
    ) {
        if (!(candidate instanceof Mob mob)
                || !(candidate instanceof Enemy)
                || !candidate.isAlive()) {

            return false;
        }

        /*
         * Never flee from another tamed hostile-derived Moblet.
         */
        if (mob instanceof MobletTameState state
                && state.moblets$isTamed()) {

            return false;
        }

        return true;
    }

    private boolean isTamedBaby() {
        return BabyWitches.isBaby(
                this.witch
        )
                && ((MobletTameState) this.witch)
                        .moblets$isTamed();
    }
}
