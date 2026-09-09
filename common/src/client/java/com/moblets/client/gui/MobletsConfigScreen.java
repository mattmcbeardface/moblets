package com.moblets.client.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MobletsConfigScreen extends Screen {

    private static final Component TITLE =
            Component.translatable("screen.baby_mobs.config.title");
    private static final Component TAMING =
            Component.translatable("screen.baby_mobs.config.taming");
    private static final Component SPAWNING =
            Component.translatable("screen.baby_mobs.config.spawning");
    private static final Component ENCOUNTERS =
            Component.translatable("screen.baby_mobs.config.encounters");
    private static final Component ADVANCED =
            Component.translatable("screen.baby_mobs.config.advanced");

    private static final int TITLE_Y = 20;
    private static final int CONTENT_TOP = 40;
    private static final int CONTENT_BOTTOM_MARGIN = 40;
    private static final int SCREEN_MARGIN = 12;
    private static final int BUTTON_MAX_WIDTH = 200;
    private static final int CATEGORY_BUTTON_HEIGHT = 40;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GRID_SPACING = 8;

    private final Screen parent;

    public MobletsConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = this.categoryButtonWidth();
        int gridWidth = (buttonWidth * 2) + GRID_SPACING;
        int gridHeight = (CATEGORY_BUTTON_HEIGHT * 2) + GRID_SPACING;
        int gridX = (this.width - gridWidth) / 2;
        int contentHeight = this.height
                - CONTENT_TOP
                - CONTENT_BOTTOM_MARGIN;
        int gridY = CONTENT_TOP
                + Math.max(0, (contentHeight - gridHeight) / 2);

        this.addCategoryButton(
                TAMING,
                gridX,
                gridY,
                () -> new MobletsTamingScreen(this)
        );
        this.addCategoryButton(
                SPAWNING,
                gridX + buttonWidth + GRID_SPACING,
                gridY,
                () -> new MobletsCategoryScreen(
                        this,
                        SPAWNING
                )
        );
        this.addCategoryButton(
                ENCOUNTERS,
                gridX,
                gridY + CATEGORY_BUTTON_HEIGHT + GRID_SPACING,
                () -> new MobletsCategoryScreen(
                        this,
                        ENCOUNTERS
                )
        );
        this.addCategoryButton(
                ADVANCED,
                gridX + buttonWidth + GRID_SPACING,
                gridY + CATEGORY_BUTTON_HEIGHT + GRID_SPACING,
                () -> new MobletsCategoryScreen(
                        this,
                        ADVANCED
                )
        );

        int doneWidth = Math.max(
                1,
                Math.min(
                        BUTTON_MAX_WIDTH,
                        this.width - (SCREEN_MARGIN * 2)
                )
        );

        this.addRenderableWidget(
                Button.builder(
                                CommonComponents.GUI_DONE,
                                button -> this.onClose()
                        )
                        .bounds(
                                (this.width - doneWidth) / 2,
                                this.height - 28,
                                doneWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private void addCategoryButton(
            Component categoryTitle,
            int x,
            int y,
            Supplier<Screen> screenFactory
    ) {
        this.addRenderableWidget(
                Button.builder(
                                categoryTitle,
                                button -> this.minecraft.gui.setScreen(
                                        screenFactory.get()
                                )
                        )
                        .bounds(
                                x,
                                y,
                                this.categoryButtonWidth(),
                                CATEGORY_BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private int categoryButtonWidth() {
        return Math.max(
                1,
                Math.min(
                        BUTTON_MAX_WIDTH,
                        (this.width
                                - (SCREEN_MARGIN * 2)
                                - GRID_SPACING) / 2
                )
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
        this.minecraft.gui.setScreen(this.parent);
    }
}
