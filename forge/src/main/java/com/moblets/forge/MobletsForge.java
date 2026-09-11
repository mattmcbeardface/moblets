package com.moblets.forge;

import com.moblets.Moblets;
import com.moblets.config.MobletsConfig;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Moblets.MOD_ID)
public final class MobletsForge {

    public MobletsForge(
            FMLJavaModLoadingContext context
    ) {
        MobletsConfig.initialize(
                FMLPaths.CONFIGDIR.get()
        );

        if (FMLEnvironment.dist.isClient()) {
            MobletsForgeClient.register(context);
        }

        MobletsForgeEvents.register();

        Moblets.LOGGER.info(
                "Moblets initialized on Forge."
        );
    }
}
