package com.moblets.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

final class MobletsCategoryScreen extends Screen {

    private static final int TITLE_Y = 20;
    private static final int SCREEN_MARGIN = 12;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;

    MobletsCategoryScreen(
            Screen parent,
            Component title
    ) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.max(
                1,
                Math.min(
                        BUTTON_WIDTH,
                        this.width - (SCREEN_MARGIN * 2)
                )
        );

        this.addRenderableWidget(
                Button.builder(
                                CommonComponents.GUI_BACK,
                                button -> this.onClose()
                        )
                        .bounds(
                                (this.width - buttonWidth) / 2,
                                this.height - 28,
                                buttonWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                TITLE_Y,
                -1
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
