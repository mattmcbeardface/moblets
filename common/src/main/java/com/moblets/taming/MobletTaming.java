package com.moblets.taming;

import com.moblets.BabyCreepers;
import com.moblets.BabyPillagers;
import com.moblets.BabySkeletons;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MobletTaming {
    private static final int CONSIDERATION_TICKS = 20;

    private MobletTaming() {
    }

    public static InteractionResult tryTame(
            Mob mob,
            Player player,
            InteractionHand hand
    ) {
        MobletDefinition definition =
                MobletRegistry.byEntityType(
                        mob.getType()
                );

        MobletTamingRule rule =
                MobletTamingRegistry.byEntityType(
                        mob.getType()
                );

        if (definition == null
                || rule == null
                || !definition.supportsTaming()
                || !MobletsConfig.tamingEnabled(definition)
                || !rule.appliesTo(mob)) {
            return InteractionResult.PASS;
        }

        ItemStack stack =
                player.getItemInHand(hand);

        if (!rule.accepts(stack)) {
            return InteractionResult.PASS;
        }

        MobletTameState state =
                (MobletTameState) mob;

        if (state.moblets$isTamed()) {
            return InteractionResult.PASS;
        }

        /*
         * Don't allow players to queue multiple tame rolls
         * while the Moblet is considering the previous one.
         */
        if (state.moblets$isConsideringTame()) {
            return InteractionResult.SUCCESS;
        }

        if (mob.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!state.moblets$isCuriousAbout(player)) {
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        boolean success =
                mob.getRandom().nextFloat()
                        < rule.chance();

        state.moblets$beginConsideringTame(
                player.getUUID(),
                success,
                CONSIDERATION_TICKS
        );

        /*
         * Freeze the transition immediately. The curiosity goal
         * owns the one-second "thinking" animation from here.
         */
        mob.setTarget(null);
        mob.setAggressive(false);
        mob.stopUsingItem();
        mob.getNavigation().stop();
        mob.getMoveControl().setWait();

        mob.xxa = 0.0F;
        mob.zza = 0.0F;

        var movement = mob.getDeltaMovement();

        mob.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );

        return InteractionResult.SUCCESS;
    }

    public static void tickConsideration(Mob mob) {
        MobletTameState state =
                (MobletTameState) mob;

        if (!state.moblets$isConsideringTame()) {
            return;
        }

        mob.setTarget(null);
        mob.setAggressive(false);
        mob.stopUsingItem();
        mob.getNavigation().stop();
        mob.getMoveControl().setWait();

        mob.xxa = 0.0F;
        mob.zza = 0.0F;

        int ticks =
                state.moblets$getConsideringTicks();

        if (ticks > 0) {
            state.moblets$setConsideringTicks(
                    ticks - 1
            );

            /*
             * Look downward while "thinking".
             */
            mob.getLookControl().setLookAt(
                    mob.getX(),
                    mob.getY() - 0.75D,
                    mob.getZ(),
                    30.0F,
                    30.0F
            );

            return;
        }

        UUIDPlayerResult result =
                findConsideringPlayer(mob, state);

        if (result.player != null) {
            /*
             * Look back up at the player immediately before
             * revealing the result.
             */
            mob.getLookControl().setLookAt(
                    result.player,
                    30.0F,
                    30.0F
            );
        }

        boolean success =
                state.moblets$getPendingTameSuccess();

        if (success && result.player != null) {
            state.moblets$setOwnerUuid(
                    result.player.getUUID()
            );

            /*
             * Apply the permanent companion stat package
             * at the moment ownership is established.
             */
            if (mob instanceof AbstractSkeleton skeleton
                    && BabySkeletons.isBaby(skeleton)) {

                BabySkeletons.applyTamedStats(
                        skeleton
                );
            } else if (mob instanceof Creeper creeper
                    && BabyCreepers.isBaby(creeper)) {

                BabyCreepers.applyTamedStats(
                        creeper
                );
            } else if (mob instanceof Pillager pillager
                    && BabyPillagers.isBaby(pillager)) {

                BabyPillagers.applyTamedStats(
                        pillager
                );
            }

            state.moblets$setCuriousPlayerUuid(null);
        }

        spawnFeedback(mob, success);

        state.moblets$clearConsideringTame();
    }

    private static UUIDPlayerResult findConsideringPlayer(
            Mob mob,
            MobletTameState state
    ) {
        var uuid =
                state.moblets$getConsideringPlayerUuid();

        if (uuid == null) {
            return new UUIDPlayerResult(null);
        }

        return new UUIDPlayerResult(
                mob.level().getPlayerByUUID(uuid)
        );
    }

    private static void spawnFeedback(
            Mob mob,
            boolean success
    ) {
        if (!(mob.level()
                instanceof ServerLevel level)) {
            return;
        }

        level.sendParticles(
                success
                        ? ParticleTypes.HEART
                        : ParticleTypes.SMOKE,
                mob.getX(),
                mob.getY()
                        + mob.getBbHeight() * 0.65D,
                mob.getZ(),
                7,
                0.30D,
                0.30D,
                0.30D,
                0.02D
        );
    }

    private record UUIDPlayerResult(
            Player player
    ) {
    }
}
