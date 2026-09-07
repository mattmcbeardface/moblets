package com.moblets.taming;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletSafeNavigation {
    public static final int MAX_SAFE_DROP = 2;

    private MobletSafeNavigation() {
    }

    public static boolean moveToSafely(
            Mob mob,
            Entity target,
            double speed
    ) {
        Path path =
                mob.getNavigation().createPath(
                        target,
                        0
                );

        if (!isSafePath(path)) {
            mob.getNavigation().stop();
            return false;
        }

        return mob.getNavigation().moveTo(
                path,
                speed
        );
    }

    public static boolean moveToSafely(
            Mob mob,
            Path path,
            double speed
    ) {
        if (!isSafePath(path)) {
            mob.getNavigation().stop();
            return false;
        }

        return mob.getNavigation().moveTo(
                path,
                speed
        );
    }

    public static boolean isSafePath(
            Path path
    ) {
        if (path == null
                || !path.canReach()
                || path.getNodeCount() == 0) {
            return false;
        }

        Node previous =
                path.getNode(0);

        for (int i = 1;
             i < path.getNodeCount();
             ++i) {

            Node current =
                    path.getNode(i);

            int drop =
                    previous.y - current.y;

            if (drop > MAX_SAFE_DROP) {
                return false;
            }

            previous = current;
        }

        return true;
    }
}
