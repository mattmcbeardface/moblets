package com.moblets.taming;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletWitchStayWanderGoal
        extends Goal {

    /*
     * Stay anchor is the center of the Witch's little territory,
     * rather than a point she must stand directly on.
     */
    private static final double WANDER_RADIUS =
            5.5D;

    private static final double WANDER_RADIUS_SQR =
            WANDER_RADIUS * WANDER_RADIUS;

    /*
     * Give her a little breathing room between destinations so
     * she looks like she's milling around rather than pacing.
     */
    private static final int MIN_IDLE_TICKS =
            30;

    private static final int EXTRA_IDLE_TICKS =
            60;

    private static final int DESTINATION_ATTEMPTS =
            12;

    private static final int MAX_VERTICAL_OFFSET =
            2;

    private static final double WANDER_SPEED =
            0.60D;

    private static final double RETURN_SPEED =
            0.80D;

    private final Mob mob;

    private int idleTicks;

    public MobletWitchStayWanderGoal(
            Mob mob
    ) {
        this.mob = mob;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE
                )
        );
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        LivingEntity target =
                this.mob.getTarget();

        return state.moblets$isTamed()
                && state.moblets$isOrderedToStay()
                && state.moblets$getStayAnchor() != null
                && ((MobletWitchMerchant) this.mob)
                        .getTradingPlayer() == null
                && (target == null
                        || !target.isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        scheduleNextWander();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        MobletTameState state =
                (MobletTameState) this.mob;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            this.mob.getNavigation().stop();
            return;
        }

        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        double dx =
                this.mob.getX() - anchorX;

        double dz =
                this.mob.getZ() - anchorZ;

        double distanceFromAnchorSqr =
                dx * dx + dz * dz;

        /*
         * Combat or knockback may temporarily displace the Witch.
         * Once idle again, first bring her back into her territory.
         */
        if (distanceFromAnchorSqr
                > WANDER_RADIUS_SQR) {

            Path returnPath =
                    this.mob.getNavigation()
                            .createPath(
                                    anchor,
                                    0
                            );

            if (MobletSafeNavigation.isSafePath(
                    returnPath)) {

                this.mob.getNavigation()
                        .moveTo(
                                returnPath,
                                RETURN_SPEED
                        );
            } else {
                this.mob.getNavigation().stop();
            }

            return;
        }

        /*
         * Let an existing stroll finish.
         */
        if (!this.mob.getNavigation().isDone()) {
            return;
        }

        if (this.idleTicks > 0) {
            --this.idleTicks;
            return;
        }

        Path wanderPath =
                findWanderPath(
                        anchor
                );

        if (wanderPath != null) {
            this.mob.getNavigation()
                    .moveTo(
                            wanderPath,
                            WANDER_SPEED
                    );
        }

        scheduleNextWander();
    }

    private Path findWanderPath(
            BlockPos anchor
    ) {
        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        for (int attempt = 0;
             attempt < DESTINATION_ATTEMPTS;
             ++attempt) {

            /*
             * Don't continually pick tiny half-block shuffles.
             * Destinations range from about 1.5 blocks away
             * from the anchor out to the edge of the territory.
             */
            double distance =
                    1.5D
                            + this.mob.getRandom()
                            .nextDouble()
                            * (WANDER_RADIUS - 1.5D);

            double angle =
                    this.mob.getRandom()
                            .nextDouble()
                            * Math.PI
                            * 2.0D;

            int x =
                    (int) Math.floor(
                            anchorX
                                    + Math.cos(angle)
                                    * distance
                    );

            int z =
                    (int) Math.floor(
                            anchorZ
                                    + Math.sin(angle)
                                    * distance
                    );

            /*
             * Try the anchor elevation first, then nearby terrain.
             */
            for (int offset = 0;
                 offset <= MAX_VERTICAL_OFFSET;
                 ++offset) {

                Path path =
                        tryDestination(
                                anchor,
                                x,
                                anchor.getY() + offset,
                                z
                        );

                if (path != null) {
                    return path;
                }

                if (offset != 0) {
                    path =
                            tryDestination(
                                    anchor,
                                    x,
                                    anchor.getY() - offset,
                                    z
                            );

                    if (path != null) {
                        return path;
                    }
                }
            }
        }

        return null;
    }

    private Path tryDestination(
            BlockPos anchor,
            int x,
            int y,
            int z
    ) {
        BlockPos candidate =
                new BlockPos(
                        x,
                        y,
                        z
                );

        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        double candidateX =
                candidate.getX() + 0.5D;

        double candidateZ =
                candidate.getZ() + 0.5D;

        double dx =
                candidateX - anchorX;

        double dz =
                candidateZ - anchorZ;

        if (dx * dx + dz * dz
                > WANDER_RADIUS_SQR) {
            return null;
        }

        Path path =
                this.mob.getNavigation()
                        .createPath(
                                candidate,
                                0
                        );

        if (!MobletSafeNavigation.isSafePath(
                path)) {
            return null;
        }

        /*
         * Reject paths that themselves leave the Witch's
         * territory just to reach an otherwise-valid destination.
         */
        for (int i = 0;
             i < path.getNodeCount();
             ++i) {

            var node =
                    path.getNode(i);

            double nodeX =
                    node.x + 0.5D;

            double nodeZ =
                    node.z + 0.5D;

            double nodeDx =
                    nodeX - anchorX;

            double nodeDz =
                    nodeZ - anchorZ;

            if (nodeDx * nodeDx
                    + nodeDz * nodeDz
                    > WANDER_RADIUS_SQR) {

                return null;
            }
        }

        return path;
    }

    private void scheduleNextWander() {
        this.idleTicks =
                MIN_IDLE_TICKS
                        + this.mob.getRandom()
                        .nextInt(
                                EXTRA_IDLE_TICKS + 1
                        );
    }
}
