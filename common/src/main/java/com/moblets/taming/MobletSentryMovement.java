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

        double anchorX =
                anchor.getX() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

        double mobDx =
                mob.getX() - anchorX;

        double mobDz =
                mob.getZ() - anchorZ;

        double currentRadiusSqr =
                mobDx * mobDx
                        + mobDz * mobDz;

        /*
         * If knockback or another external force displaced the
         * sentry outside its working area, prioritize returning
         * to the post.
         */
        if (currentRadiusSqr
                > MOVEMENT_RADIUS_SQR
                || Math.abs(
                        mob.getY() - anchor.getY()
                ) > MAX_VERTICAL_OFFSET) {

            moveToward(
                    mob,
                    anchor,
                    anchor
            );

            return;
        }

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
                > MOVEMENT_RADIUS_SQR) {
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
