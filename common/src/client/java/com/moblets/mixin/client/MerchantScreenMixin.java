package com.moblets.mixin.client;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {

    /*
     * Vanilla appends:
     *
     *   - Novice
     *   - Apprentice
     *   - Journeyman
     *   - Expert
     *   - Master
     *
     * inside MerchantScreen.extractLabels().
     *
     * Returning level 0 only for the label calculation prevents
     * that suffix. The MerchantMenu still retains its real rank,
     * XP and progress-bar state everywhere else.
     */
    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/MerchantMenu;getTraderLevel()I"
            )
    )
    private int moblets$hideVanillaWitchRank(
            MerchantMenu menu
    ) {
        MerchantScreen screen =
                (MerchantScreen) (Object) this;

        if (screen.getTitle()
                .getString()
                .startsWith("Witch - ")) {

            return 0;
        }

        return menu.getTraderLevel();
    }
}
