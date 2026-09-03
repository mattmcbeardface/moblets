package com.pagesofatlas.babymobs;

import java.util.EnumSet;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class BabyEndermanLootFleeGoal extends Goal {
    private static final double ALERT_DISTANCE =
            12.0D;

    private static final double ESCAPE_DISTANCE =
            16.0D;

    private static final double ESCAPE_DISTANCE_SQR =
            ESCAPE_DISTANCE * ESCAPE_DISTANCE;

    private static final double FLEE_SPEED =
            0.95D;

    private final EnderMan enderman;
    private final PathNavigation navigation;

    private Player player;
    private Path path;

    public BabyEndermanLootFleeGoal(
            EnderMan enderman
    ) {
        this.enderman = enderman;
        this.navigation = enderman.getNavigation();

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE
        ));
    }

    @Override
    public boolean canUse() {
        if (!BabyEndermen.isBaby(this.enderman)
                || this.enderman
                        .getItemBySlot(
                                EquipmentSlot.MAINHAND
                        )
                        .isEmpty()) {
            return false;
        }

        this.player =
                this.enderman.level()
                        .getNearestPlayer(
                                this.enderman,
                                ALERT_DISTANCE
                        );

        return this.player != null
                && this.makeEscapePath();
    }

    @Override
    public boolean canContinueToUse() {
        return BabyEndermen.isBaby(this.enderman)
                && !this.enderman
                        .getItemBySlot(
                                EquipmentSlot.MAINHAND
                        )
                        .isEmpty()
                && this.player != null
                && this.player.isAlive()
                && this.enderman.distanceToSqr(this.player)
                        < ESCAPE_DISTANCE_SQR;
    }

    @Override
    public void start() {
        if (this.path != null) {
            this.navigation.moveTo(
                    this.path,
                    FLEE_SPEED
            );
        }
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.path = null;
        this.player = null;
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

        if (this.navigation.isDone()
                && this.enderman.distanceToSqr(this.player)
                        < ESCAPE_DISTANCE_SQR) {

            if (this.makeEscapePath()) {
                this.navigation.moveTo(
                        this.path,
                        FLEE_SPEED
                );
            }
        }

        this.navigation.setSpeedModifier(
                FLEE_SPEED
        );
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
}
