package com.moblets.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletSentryMovement {
    private static final double MOVEMENT_RADIUS =
            5.0D;

    private static final double MOVEMENT_RADIUS_SQR =
            MOVEMENT_RADIUS * MOVEMENT_RADIUS;

    private static final float EVADE_BACKWARDS =
            -0.50F;

    private static final float EVADE_SIDEWAYS =
            0.50F;

    private static final double EVADE_PROBE_DISTANCE =
            1.25D;

    private static final double EVADE_TERRAIN_SPEED =
            1.15D;

    private static final int MAX_VERTICAL_OFFSET =
            2;

    private static final int REPOSITION_INTERVAL =
            5;

    private static final double MOVEMENT_SPEED =
            1.0D;

    private MobletSentryMovement() {
    }

    public static void tick(
            Mob mob,
            BlockPos anchor,
            LivingEntity target
    ) {
        if (anchor == null
                || target == null
                || !target.isAlive()) {
            return;
        }

        if (mob.tickCount % REPOSITION_INTERVAL != 0) {
            return;
        }

        /*
         * Do not pull an actively fighting sentry back toward
         * its anchor.
         *
         * The five-block radius limits deliberate sentry
         * repositioning, but melee evasion may temporarily
         * carry the Moblet farther away. MobletStayGoal returns
         * it to the anchor after combat ends.
         */
        /*
         * If we already have a shot, hold the position.
         *
         * No random bow strafing.
         */
        if (mob.getSensing()
                .hasLineOfSight(target)) {

            mob.getNavigation().stop();
            return;
        }

        /*
         * No sight: deliberately work toward a safe firing
         * position in the target's direction.
         */
        BlockPos destination =
                findRepositionDestination(
                        mob,
                        anchor,
                        target
                );

        if (destination != null) {
            moveToward(
                    mob,
                    anchor,
                    destination
            );
        }
    }

    public static boolean tryEvasionStep(
            Mob mob,
            BlockPos anchor,
            LivingEntity target,
            boolean clockwise
    ) {
        return tryEvasionStep(
                mob,
                anchor,
                target,
                clockwise,
                Double.POSITIVE_INFINITY
        );
    }

    public static boolean tryEvasionStep(
            Mob mob,
            BlockPos anchor,
            LivingEntity target,
            boolean clockwise,
            double maxAnchorRadius
    ) {
        /*
         * A terrain-aware retreat may have started an actual
         * navigation path to climb or descend one block.
         *
         * Do not replace that path with another strafe command on
         * the next tick. Let vanilla navigation complete the step,
         * including its normal jump handling.
         */
        if (!mob.getNavigation().isDone()) {
            mob.lookAt(
                    target,
                    30.0F,
                    30.0F
            );

            return true;
        }

        double towardX =
                target.getX() - mob.getX();

        double towardZ =
                target.getZ() - mob.getZ();

        double length =
                Math.sqrt(
                        towardX * towardX
                                + towardZ * towardZ
                );

        if (length < 0.001D) {
            return false;
        }

        towardX /= length;
        towardZ /= length;

        /*
         * Backward vector relative to the target.
         */
        double awayX =
                -towardX;

        double awayZ =
                -towardZ;

        /*
         * Perpendicular vector for vanilla-like lateral
         * strafing.
         */
        double sideX =
                -towardZ;

        double sideZ =
                towardX;

        double sideSign =
                clockwise ? 1.0D : -1.0D;

        double moveX =
                awayX
                        + sideX * sideSign;

        double moveZ =
                awayZ
                        + sideZ * sideSign;

        double moveLength =
                Math.sqrt(
                        moveX * moveX
                                + moveZ * moveZ
                );

        if (moveLength < 0.001D) {
            return false;
        }

        moveX /= moveLength;
        moveZ /= moveLength;

        int candidateX =
                (int) Math.floor(
                        mob.getX()
                                + moveX
                                * EVADE_PROBE_DISTANCE
                );

        int candidateZ =
                (int) Math.floor(
                        mob.getZ()
                                + moveZ
                                * EVADE_PROBE_DISTANCE
                );

        BlockPos candidate =
                findEvasionCandidate(
                        mob,
                        anchor,
                        candidateX,
                        candidateZ,
                        maxAnchorRadius
                );

        if (candidate == null) {
            return false;
        }

        mob.lookAt(
                target,
                30.0F,
                30.0F
        );

        /*
         * Flat ground retains the responsive backward/lateral
         * strafe behavior.
         *
         * If the safest retreat square is one block higher or
         * lower, hand the move to normal path navigation so the
         * Moblet can actually climb/drop with the terrain instead
         * of repeatedly strafing into the obstacle.
         */
        if (candidate.getY()
                != mob.blockPosition().getY()) {

            Path path =
                    mob.getNavigation()
                            .createPath(
                                    candidate,
                                    0
                            );

            return MobletSafeNavigation.moveToSafely(
                    mob,
                    path,
                    EVADE_TERRAIN_SPEED
            );
        }

        mob.getNavigation().stop();

        mob.getMoveControl().strafe(
                EVADE_BACKWARDS,
                clockwise
                        ? EVADE_SIDEWAYS
                        : -EVADE_SIDEWAYS
        );

        return true;
    }

    private static BlockPos findEvasionCandidate(
            Mob mob,
            BlockPos anchor,
            int candidateX,
            int candidateZ,
            double maxAnchorRadius
    ) {
        int baseY =
                mob.blockPosition().getY();

        /*
         * Prefer staying level. If the retreat direction runs
         * into terrain, allow ordinary one-block climbing or
         * descending rather than abandoning the retreat.
         */
        int[] verticalOffsets = {
                0,
                1,
                -1
        };

        for (int verticalOffset : verticalOffsets) {
            BlockPos candidate =
                    new BlockPos(
                            candidateX,
                            baseY + verticalOffset,
                            candidateZ
                    );

            if (isSafeEvasionCandidate(
                    mob,
                    anchor,
                    candidate,
                    maxAnchorRadius)) {

                return candidate;
            }
        }

        return null;
    }


    private static boolean isSafeEvasionCandidate(
            Mob mob,
            BlockPos anchor,
            BlockPos candidate,
            double maxAnchorRadius
    ) {
        if (Double.isFinite(maxAnchorRadius)) {
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
                    > maxAnchorRadius
                    * maxAnchorRadius) {
                return false;
            }
        }

        Level level =
                mob.level();

        /*
         * A path may technically resolve near an obstructed block
         * without that exact square being valid standing space.
         * Verify the Moblet's real bounding box fits there.
         */
        var candidateBox =
                mob.getBoundingBox().move(
                        candidate.getX() + 0.5D
                                - mob.getX(),
                        candidate.getY()
                                - mob.getY(),
                        candidate.getZ() + 0.5D
                                - mob.getZ()
                );

        if (!level.noCollision(
                mob,
                candidateBox
        )) {
            return false;
        }

        BlockPos floor =
                candidate.below();

        if (!level.getBlockState(floor)
                .isFaceSturdy(
                        level,
                        floor,
                        Direction.UP
                )) {
            return false;
        }

        Path path =
                mob.getNavigation()
                        .createPath(
                                candidate,
                                0
                        );

        return MobletSafeNavigation
                .isSafePath(path);
    }

    private static BlockPos findRepositionDestination(
            Mob mob,
            BlockPos anchor,
            LivingEntity target
    ) {
        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        double dx =
                target.getX() - anchorX;

        double dz =
                target.getZ() - anchorZ;

        double length =
                Math.sqrt(
                        dx * dx + dz * dz
                );

        if (length < 0.001D) {
            return null;
        }

        double distance =
                Math.min(
                        MOVEMENT_RADIUS,
                        length
                );

        double wantedX =
                anchorX
                        + dx / length
                        * distance;

        double wantedZ =
                anchorZ
                        + dz / length
                        * distance;

        int baseX =
                (int) Math.floor(wantedX);

        int baseZ =
                (int) Math.floor(wantedZ);

        /*
         * Search around the projected firing position.
         *
         * This is useful on battlements where the mathematically
         * ideal position may actually be one block beyond the
         * wall edge. The search backs up until it finds footing.
         */
        for (int searchRadius = 0;
             searchRadius <= 2;
             ++searchRadius) {

            for (int xOffset = -searchRadius;
                 xOffset <= searchRadius;
                 ++xOffset) {

                for (int zOffset = -searchRadius;
                     zOffset <= searchRadius;
                     ++zOffset) {

                    for (int yOffset = 0;
                         yOffset <= MAX_VERTICAL_OFFSET;
                         ++yOffset) {

                        BlockPos candidate =
                                new BlockPos(
                                        baseX + xOffset,
                                        anchor.getY()
                                                + yOffset,
                                        baseZ + zOffset
                                );

                        if (isSafeCandidate(
                                mob,
                                anchor,
                                candidate)) {
                            return candidate;
                        }

                        if (yOffset != 0) {
                            candidate =
                                    new BlockPos(
                                            baseX
                                                    + xOffset,
                                            anchor.getY()
                                                    - yOffset,
                                            baseZ
                                                    + zOffset
                                    );

                            if (isSafeCandidate(
                                    mob,
                                    anchor,
                                    candidate)) {
                                return candidate;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSafeCandidate(
            Mob mob,
            BlockPos anchor,
            BlockPos candidate
    ) {
        return isSafeCandidate(
                mob,
                anchor,
                candidate,
                MOVEMENT_RADIUS_SQR
        );
    }

    private static boolean isSafeCandidate(
            Mob mob,
            BlockPos anchor,
            BlockPos candidate,
            double allowedRadiusSqr
    ) {
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
                > allowedRadiusSqr) {
            return false;
        }

        if (Math.abs(
                candidate.getY()
                        - anchor.getY()
        ) > MAX_VERTICAL_OFFSET) {
            return false;
        }

        Level level =
                mob.level();

        BlockPos floor =
                candidate.below();

        if (!level.getBlockState(floor)
                .isFaceSturdy(
                        level,
                        floor,
                        Direction.UP
                )) {
            return false;
        }

        Path path =
                mob.getNavigation()
                        .createPath(
                                candidate,
                                0
                        );

        if (!MobletSafeNavigation
                .isSafePath(path)) {
            return false;
        }

        /*
         * A sentry path itself must stay inside the assigned
         * movement envelope. It cannot solve a five-block
         * reposition by wandering around the entire castle.
         */
        for (int i = 0;
             i < path.getNodeCount();
             ++i) {

            Node node =
                    path.getNode(i);

            double nodeDx =
                    node.x + 0.5D
                            - anchorX;

            double nodeDz =
                    node.z + 0.5D
                            - anchorZ;

            if (nodeDx * nodeDx
                    + nodeDz * nodeDz
                    > allowedRadiusSqr) {
                return false;
            }

            if (Math.abs(
                    node.y - anchor.getY()
            ) > MAX_VERTICAL_OFFSET) {
                return false;
            }
        }

        return true;
    }

    private static void moveToward(
            Mob mob,
            BlockPos anchor,
            BlockPos destination
    ) {
        PathNavigation navigation =
                mob.getNavigation();

        Path path =
                navigation.createPath(
                        destination,
                        0
                );

        if (path == null) {
            navigation.stop();
            return;
        }

        if (!pathStaysInsideSentryArea(
                path,
                anchor)) {
            navigation.stop();
            return;
        }

        MobletSafeNavigation.moveToSafely(
                mob,
                path,
                MOVEMENT_SPEED
        );
    }

    private static boolean pathStaysInsideSentryArea(
            Path path,
            BlockPos anchor
    ) {
        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        for (int i = 0;
             i < path.getNodeCount();
             ++i) {

            Node node =
                    path.getNode(i);

            double dx =
                    node.x + 0.5D - anchorX;

            double dz =
                    node.z + 0.5D - anchorZ;

            if (dx * dx + dz * dz
                    > MOVEMENT_RADIUS_SQR) {
                return false;
            }

            if (Math.abs(
                    node.y - anchor.getY()
            ) > MAX_VERTICAL_OFFSET) {
                return false;
            }
        }

        return true;
    }
}
