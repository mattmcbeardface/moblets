package com.moblets.forge;

import com.moblets.client.gui.MobletsConfigScreen;

import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

final class MobletsForgeClient {
    private MobletsForgeClient() {
    }

    static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenFactory.class,
                () -> new ConfigScreenFactory(
                        MobletsConfigScreen::new
                )
        );
    }
}
