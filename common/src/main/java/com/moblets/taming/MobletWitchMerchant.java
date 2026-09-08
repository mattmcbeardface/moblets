package com.moblets.taming;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;

public interface MobletWitchMerchant
        extends Merchant {

    void moblets$openWitchShop(
            Player player
    );
}
