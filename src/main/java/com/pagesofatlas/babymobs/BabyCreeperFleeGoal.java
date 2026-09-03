package com.pagesofatlas.babymobs;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class BabyCreeperFleeGoal extends AvoidEntityGoal<Player> {
    /*
     * Looking at the Moblet Creeper from within this distance
     * frightens it.
     */
    private static final float FLEE_DISTANCE = 16.0F;

    /*
     * Once frightened, it runs just beyond the detection radius
     * before resetting.
     *
     * The extra block prevents it from rapidly toggling at exactly
     * the edge of the detection range.
     */
    private static final double RESET_DISTANCE = 17.0D;
    private static final double RESET_DISTANCE_SQR =
            RESET_DISTANCE * RESET_DISTANCE;

    private static final double WALK_SPEED = 1.25D;
    private static final double SPRINT_SPEED = 1.50D;

    /*
     * Fairly generous stare cone because the Moblet Creeper
     * is physically small.
     */
    private static final double LOOK_THRESHOLD = 0.10D;

    private final Creeper creeper;

    public BabyCreeperFleeGoal(Creeper creeper) {
        super(
                creeper,
                Player.class,
                entity -> entity instanceof Player player
                        && isLookingAtCreeper(creeper, player),
                FLEE_DISTANCE,
                WALK_SPEED,
                SPRINT_SPEED,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR
        );

        this.creeper = creeper;
    }

    @Override
    public boolean canUse() {
        return BabyCreepers.isBaby(this.creeper)
                && !this.creeper.isIgnited()
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!BabyCreepers.isBaby(this.creeper)
                || this.creeper.isIgnited()
                || this.toAvoid == null
                || this.toAvoid.isDeadOrDying()) {
            return false;
        }

        /*
         * Once frightened, commit to escaping just beyond the
         * detection radius even if the player looks away.
         */
        return this.creeper.distanceToSqr(this.toAvoid)
                < RESET_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.creeper.setSwellDir(-1);

        /*
         * Startled hop the instant the Moblet realizes
         * the player has spotted it.
         */
        if (this.creeper.onGround()) {
            this.creeper.getJumpControl().jump();
        }

        super.start();
    }

    @Override
    public void tick() {
        if (this.toAvoid == null) {
            return;
        }

        /*
         * Stay defused while escaping.
         */
        this.creeper.setSwellDir(-1);

        /*
         * If the first escape path finishes before reaching the
         * reset distance, choose another route away.
         */
        if (this.pathNav.isDone()
                && this.creeper.distanceToSqr(this.toAvoid)
                < RESET_DISTANCE_SQR) {

            Vec3 escape = DefaultRandomPos.getPosAway(
                    this.creeper,
                    12,
                    5,
                    this.toAvoid.position()
            );

            if (escape != null
                    && this.toAvoid.distanceToSqr(
                            escape.x,
                            escape.y,
                            escape.z
                    ) > this.toAvoid.distanceToSqr(this.creeper)) {

                this.path = this.pathNav.createPath(
                        escape.x,
                        escape.y,
                        escape.z,
                        0
                );

                if (this.path != null) {
                    this.pathNav.moveTo(
                            this.path,
                            SPRINT_SPEED
                    );
                }
            }
        }

        this.pathNav.setSpeedModifier(SPRINT_SPEED);
    }

    @Override
    public void stop() {
        this.creeper.setSwellDir(-1);

        /*
         * Important: don't let it continue following an old flee
         * path after it has escaped far enough.
         */
        this.creeper.getNavigation().stop();

        super.stop();
    }

    private static boolean isLookingAtCreeper(
            Creeper creeper,
            Player player
    ) {
        if (player.isSpectator()) {
            return false;
        }

        Vec3 playerLook =
                player.getViewVector(1.0F).normalize();

        Vec3 towardCreeper = new Vec3(
                creeper.getX() - player.getX(),
                creeper.getEyeY() - player.getEyeY(),
                creeper.getZ() - player.getZ()
        );

        double distance = towardCreeper.length();

        if (distance < 0.0001D) {
            return true;
        }

        towardCreeper = towardCreeper.normalize();

        double alignment =
                playerLook.dot(towardCreeper);

        return alignment
                > 1.0D - LOOK_THRESHOLD / distance
                && player.hasLineOfSight(creeper);
    }
}
