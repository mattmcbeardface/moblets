package com.moblets.mixin;

import com.moblets.BabyCreepers;
import com.moblets.balance.MobletBalance;
import com.moblets.registry.BalanceStat;
import com.moblets.taming.MobletTameState;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

    @Accessor("source")
    protected abstract @Nullable Entity babyMobs$getSource();

    @Redirect(
            method = {
                    "hurtEntities()V",
                    "hurtEntities(Ljava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean babyMobs$controlBabyCreeperDamage(
            Entity entity,
            ServerLevel level,
            DamageSource damageSource,
            float vanillaDamage
    ) {
        Entity source =
                babyMobs$getSource();

        if (!(source instanceof Creeper creeper)
                || !BabyCreepers.isBaby(creeper)) {

            return entity.hurtServer(
                    level,
                    damageSource,
                    vanillaDamage
            );
        }

        MobletTameState creeperState =
                (MobletTameState) creeper;

        /*
         * Wild Baby Creepers retain their existing reduced
         * explosion damage.
         */
        if (!creeperState.moblets$isTamed()) {
            return entity.hurtServer(
                    level,
                    damageSource,
                    vanillaDamage
                            / 3.0F
                            * (float) MobletBalance.multiplier(
                                    creeper,
                                    BalanceStat.DAMAGE
                            )
            );
        }

        /*
         * Controlled companion explosions never hurt players.
         */
        if (entity instanceof Player) {
            return false;
        }

        /*
         * Never hurt another tamed Moblet.
         */
        if (entity instanceof Mob mob
                && ((MobletTameState) mob)
                        .moblets$isTamed()) {
            return false;
        }

        /*
         * Controlled explosions only damage hostile mobs.
         * Villagers, animals and other passive entities remain
         * unharmed.
         */
        if (!(entity instanceof Enemy)) {
            return false;
        }

        return entity.hurtServer(
                level,
                damageSource,
                vanillaDamage
                        * (float) MobletBalance.multiplier(
                                creeper,
                                BalanceStat.DAMAGE
                        )
        );
    }
}
