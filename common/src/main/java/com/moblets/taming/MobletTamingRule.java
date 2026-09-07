package com.moblets.taming;

import java.util.function.Predicate;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public record MobletTamingRule(
        Predicate<Mob> candidate,
        Predicate<ItemStack> item,
        float chance
) {
    public boolean appliesTo(Mob mob) {
        return candidate.test(mob);
    }

    public boolean accepts(ItemStack stack) {
        return item.test(stack);
    }
}
