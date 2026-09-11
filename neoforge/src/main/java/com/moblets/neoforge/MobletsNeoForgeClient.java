package com.moblets.neoforge;

import com.moblets.Moblets;
import com.moblets.client.gui.MobletsConfigScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Moblets.MOD_ID, dist = Dist.CLIENT)
public final class MobletsNeoForgeClient {
    public MobletsNeoForgeClient(ModContainer container) {
        IConfigScreenFactory factory =
                (modContainer, parent) ->
                        new MobletsConfigScreen(parent);

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                factory
        );
    }
}
