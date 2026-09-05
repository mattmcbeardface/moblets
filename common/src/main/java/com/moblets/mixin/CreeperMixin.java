package com.moblets.mixin;

import com.moblets.BabyCreeperFleeGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {

    protected CreeperMixin(
            EntityType<? extends Monster> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void babyMobs$addShyBabyGoal(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;

        // Priority 1 beats vanilla SwellGoal at priority 2.
        this.goalSelector.addGoal(
                1,
                new BabyCreeperFleeGoal(creeper)
        );
    }
}
