package com.moblets.taming;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MobletWitchTradeAttentionGoal
        extends Goal {

    private final Witch witch;

    public MobletWitchTradeAttentionGoal(
            Witch witch
    ) {
        this.witch = witch;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        Player customer =
                getCustomer();

        return customer != null
                && customer.isAlive()
                && this.witch.isAlive();
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
        holdPosition();
    }

    @Override
    public void tick() {
        holdPosition();
    }

    private Player getCustomer() {
        return ((MobletWitchMerchant) this.witch)
                .getTradingPlayer();
    }

    private void holdPosition() {
        Player customer =
                getCustomer();

        if (customer == null) {
            return;
        }

        /*
         * Trading completely overrides idle wandering,
         * Follow movement and ordinary navigation.
         */
        this.witch.getNavigation().stop();
        this.witch.getMoveControl().setWait();

        this.witch.xxa = 0.0F;
        this.witch.zza = 0.0F;

        /*
         * Kill residual horizontal movement without interfering
         * with gravity/jumping physics.
         */
        Vec3 movement =
                this.witch.getDeltaMovement();

        this.witch.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );

        /*
         * Behave like a proper merchant: remain attentive to
         * whoever currently has the trade screen open.
         */
        this.witch.getLookControl()
                .setLookAt(
                        customer,
                        30.0F,
                        30.0F
                );
    }
}
