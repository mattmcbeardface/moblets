package com.moblets.forge;

import com.moblets.Moblets;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Moblets.MOD_ID)
public final class MobletsForge {

    public MobletsForge(FMLJavaModLoadingContext context) {
        MobletsForgeEvents.register();

        Moblets.LOGGER.info("Moblets initialized on Forge.");
    }
}
