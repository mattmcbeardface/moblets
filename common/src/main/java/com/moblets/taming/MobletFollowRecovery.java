package com.moblets.taming;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletFollowRecovery {
    /*
     * Large enough to find stairs behind a reasonably-sized
     * wall, platform, roof, etc., without turning recovery into
     * a giant expensive world search.
     */
    private static final int HORIZONTAL_SEARCH_RADIUS = 12;

    /*
     * We only need to discover the next safe descent route.
     * Once the Moblet gets lower, normal Follow recalculates.
     */
    private static final int MAX_SEARCH_DOWN = 8;

    private static final int MIN_VERTICAL_DIFFERENCE = 2;

    private MobletFollowRecovery() {
    }

    public static Path findDescentPath(
            Mob mob,
            Player owner
    ) {
        int mobY =
                mob.blockPosition().getY();

        int ownerY =
                owner.blockPosition().getY();

        /*
         * This recovery is specifically for the case where
         * the Moblet is stranded above its owner.
         */
        if (mobY - ownerY
                < MIN_VERTICAL_DIFFERENCE) {
            return null;
        }

        BlockPos origin =
                mob.blockPosition();

        int lowestY =
                Math.max(
                        ownerY,
                        mobY - MAX_SEARCH_DOWN
                );

        List<BlockPos> candidates =
                new ArrayList<>();

        /*
         * Offer the pathfinder lots of possible LOWER standing
         * positions around the Moblet.
         *
         * We only pre-filter obvious solid locations. The
         * pathfinder itself remains responsible for deciding
         * whether the position is actually reachable.
         */
        for (int y = mobY - 1;
             y >= lowestY;
             --y) {

            for (int x = -HORIZONTAL_SEARCH_RADIUS;
                 x <= HORIZONTAL_SEARCH_RADIUS;
                 ++x) {

                for (int z = -HORIZONTAL_SEARCH_RADIUS;
                     z <= HORIZONTAL_SEARCH_RADIUS;
                     ++z) {

                    if (x * x + z * z
                            > HORIZONTAL_SEARCH_RADIUS
                                    * HORIZONTAL_SEARCH_RADIUS) {
                        continue;
                    }

                    BlockPos candidate =
                            new BlockPos(
                                    origin.getX() + x,
                                    y,
                                    origin.getZ() + z
                            );

                    /*
                     * Entity feet need at least an open block.
                     * Pathfinding handles the actual floor,
                     * stair, collision and clearance rules.
                     */
                    if (!mob.level()
                            .getBlockState(candidate)
                            .isAir()) {
                        continue;
                    }

                    candidates.add(candidate);
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        Path path =
                mob.getNavigation().createPath(
                        candidates.stream(),
                        0
                );

        if (!MobletSafeNavigation.isSafePath(path)) {
            return null;
        }

        return path;
    }
}
