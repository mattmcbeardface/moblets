package com.moblets.balance;

import com.moblets.Moblets;
import com.moblets.config.MobletsConfig;
import com.moblets.mixin.CreeperAccessor;
import com.moblets.registry.BalanceStat;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletProfile;
import com.moblets.registry.MobletRegistry;
import com.moblets.taming.MobletTameState;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;

public final class MobletBalance {
    private static final Identifier HEALTH_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "advanced_health"
            );
    private static final Identifier DAMAGE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "advanced_damage"
            );
    private static final Identifier MOVEMENT_SPEED_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "advanced_movement_speed"
            );

    private static final double DEFAULT_MULTIPLIER = 1.0D;
    private static final double EPSILON = 0.0000001D;
    private static final float ZERO_ACCURACY_INACCURACY = 1000.0F;

    private MobletBalance() {
    }

    public static void initialize(Mob mob) {
        if (mob.level().isClientSide()) {
            return;
        }

        MobletDefinition definition = definition(mob);

        if (definition == null
                || !definition.isMoblet(mob)) {
            return;
        }

        ((MobletBalanceState) mob).moblets$activateBalance();
        forceRefresh(mob);
    }

    public static void initializeLoaded(Entity entity) {
        if (entity instanceof Mob mob) {
            initialize(mob);
        }
    }

    public static void refresh(Mob mob) {
        if (mob.level().isClientSide()) {
            return;
        }

        MobletBalanceState state = (MobletBalanceState) mob;

        if (!state.moblets$isBalanceActive()) {
            return;
        }

        MobletDefinition definition = definition(mob);

        if (definition == null) {
            return;
        }

        MobletProfile profile = profile(mob, definition);
        boolean tamed = profile == MobletProfile.TAMED;
        long revision = MobletsConfig.advancedRevision();

        if (state.moblets$getBalanceRevision() == revision
                && state.moblets$getBalanceTamed() == tamed) {
            return;
        }

        applyAttributes(mob, definition, profile);
        applyBlastRadius(mob, definition, profile);
        state.moblets$markBalanceApplied(revision, tamed);
    }

    public static void forceRefresh(Mob mob) {
        MobletBalanceState state = (MobletBalanceState) mob;

        if (!state.moblets$isBalanceActive()) {
            return;
        }

        state.moblets$markBalanceApplied(
                Long.MIN_VALUE,
                false
        );
        refresh(mob);
    }

    public static double multiplier(
            Mob mob,
            BalanceStat stat
    ) {
        MobletDefinition definition = definition(mob);

        if (definition == null) {
            return DEFAULT_MULTIPLIER;
        }

        return MobletsConfig.balanceMultiplier(
                definition,
                profile(mob, definition),
                stat
        );
    }

    public static float adjustedInaccuracy(
            Mob mob,
            float currentMobletInaccuracy
    ) {
        double multiplier = multiplier(
                mob,
                BalanceStat.ACCURACY
        );

        if (multiplier <= 0.0D) {
            return ZERO_ACCURACY_INACCURACY;
        }

        if (multiplier >= MobletsConfig.PERFECT_AIM_MULTIPLIER) {
            return 0.0F;
        }

        return (float) (currentMobletInaccuracy / multiplier);
    }

    public static int blastRadius(Mob mob) {
        MobletDefinition definition = definition(mob);

        if (definition == null
                || !definition.hasBlastRadius()) {
            return 0;
        }

        return MobletsConfig.blastRadius(
                definition,
                profile(mob, definition)
        );
    }

    public static MobletProfile profile(
            Mob mob,
            MobletDefinition definition
    ) {
        if (definition.supportsTaming()
                && ((MobletTameState) mob).moblets$isTamed()) {
            return MobletProfile.TAMED;
        }

        return MobletProfile.WILD;
    }

    private static void applyAttributes(
            Mob mob,
            MobletDefinition definition,
            MobletProfile profile
    ) {
        double previousHealth = mob.getHealth();
        double previousMaxHealth = mob.getMaxHealth();

        applyMultiplier(
                mob.getAttribute(Attributes.MAX_HEALTH),
                HEALTH_ID,
                multiplier(definition, profile, BalanceStat.HEALTH)
        );
        applyMultiplier(
                mob.getAttribute(Attributes.ATTACK_DAMAGE),
                DAMAGE_ID,
                multiplier(definition, profile, BalanceStat.DAMAGE)
        );
        applyMultiplier(
                mob.getAttribute(Attributes.MOVEMENT_SPEED),
                MOVEMENT_SPEED_ID,
                multiplier(
                        definition,
                        profile,
                        BalanceStat.MOVEMENT_SPEED
                )
        );

        double newMaxHealth = mob.getMaxHealth();

        if (Math.abs(newMaxHealth - previousMaxHealth) > EPSILON
                && previousMaxHealth > 0.0D) {
            double healthRatio = previousHealth / previousMaxHealth;

            mob.setHealth(
                    (float) Math.max(
                            0.0D,
                            Math.min(
                                    newMaxHealth,
                                    newMaxHealth * healthRatio
                            )
                    )
            );
        }
    }

    private static double multiplier(
            MobletDefinition definition,
            MobletProfile profile,
            BalanceStat stat
    ) {
        return definition.supportsBalance(profile, stat)
                ? MobletsConfig.balanceMultiplier(
                        definition,
                        profile,
                        stat
                )
                : DEFAULT_MULTIPLIER;
    }

    private static void applyMultiplier(
            AttributeInstance attribute,
            Identifier id,
            double multiplier
    ) {
        if (attribute == null) {
            return;
        }

        double amount = multiplier - DEFAULT_MULTIPLIER;

        if (Math.abs(amount) <= EPSILON) {
            attribute.removeModifier(id);
            return;
        }

        AttributeModifier current = attribute.getModifier(id);

        if (current != null
                && current.operation()
                        == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                && Math.abs(current.amount() - amount) <= EPSILON) {
            return;
        }

        attribute.addOrUpdateTransientModifier(
                new AttributeModifier(
                        id,
                        amount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
        );
    }

    private static void applyBlastRadius(
            Mob mob,
            MobletDefinition definition,
            MobletProfile profile
    ) {
        if (definition.hasBlastRadius()
                && mob instanceof Creeper creeper) {
            ((CreeperAccessor) creeper).babyMobs$setExplosionRadius(
                    MobletsConfig.blastRadius(
                            definition,
                            profile
                    )
            );
        }
    }

    private static MobletDefinition definition(Mob mob) {
        MobletDefinition definition =
                MobletRegistry.byEntityType(mob.getType());

        return definition != null
                && definition.hasAdvancedBalance()
                ? definition
                : null;
    }
}
