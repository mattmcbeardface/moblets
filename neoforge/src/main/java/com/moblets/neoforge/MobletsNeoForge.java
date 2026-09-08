package com.moblets.neoforge;

import com.moblets.Moblets;
import com.moblets.config.MobletsConfig;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Moblets.MOD_ID)
public final class MobletsNeoForge {

    public MobletsNeoForge() {
        MobletsConfig.initialize(
                FMLPaths.CONFIGDIR.get()
        );

        NeoForge.EVENT_BUS.register(
                new MobletsNeoForgeEvents()
        );

        Moblets.LOGGER.info(
                "Moblets initialized on NeoForge."
        );
    }
}
