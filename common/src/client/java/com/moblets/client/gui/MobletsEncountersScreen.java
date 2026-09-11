package com.moblets.client.gui;

import java.util.List;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.EncounterDefinition;
import com.moblets.registry.EncounterRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MobletsEncountersScreen extends Screen {

    private static final String TRANSLATION_PREFIX =
            "screen.baby_mobs.config.encounters.";

    private static final Component TITLE =
            Component.translatable("screen.baby_mobs.config.encounters");
    private static final Component DESCRIPTION =
            Component.translatable(
                    "screen.baby_mobs.config.encounters.description"
            );
    private static final Component DESCRIPTION_MORE =
            Component.translatable(
                    "screen.baby_mobs.config.encounters.description_more"
            );
    private static final Component RESET_TO_DEFAULTS =
            Component.translatable(
                    "screen.baby_mobs.config.encounters.reset"
            );

    private static final int TITLE_Y = 12;
    private static final int DESCRIPTION_Y = 34;
    private static final int HEADER_TEXT_GAP = 2;
    private static final int LIST_GAP = 8;
    private static final int FOOTER_HEIGHT = 60;
    private static final int SCREEN_MARGIN = 16;
    private static final int MAX_CONTENT_WIDTH = 520;
    private static final int TOGGLE_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int RESET_BUTTON_WIDTH = 150;
    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int SECONDARY_TEXT_COLOR = -8355712;

    private final Screen parent;

    private int contentLeft;
    private int contentWidth;
    private int secondDescriptionY;

    public MobletsEncountersScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.contentWidth = Math.max(
                1,
                Math.min(
                        MAX_CONTENT_WIDTH,
                        this.width - (SCREEN_MARGIN * 2)
                )
        );
        this.contentLeft = (this.width - this.contentWidth) / 2;

        int firstDescriptionHeight =
                this.wrappedHeight(DESCRIPTION);
        this.secondDescriptionY = DESCRIPTION_Y
                + firstDescriptionHeight
                + HEADER_TEXT_GAP;
        int listTop = this.secondDescriptionY
                + this.wrappedHeight(DESCRIPTION_MORE)
                + LIST_GAP;
        int listHeight = Math.max(
                1,
                this.height - FOOTER_HEIGHT - listTop
        );

        this.addRenderableWidget(
                new EncounterList(
                        this.minecraft,
                        this.width,
                        listHeight,
                        listTop
                )
        );

        int resetWidth = Math.max(
                1,
                Math.min(
                        RESET_BUTTON_WIDTH,
                        this.width - (SCREEN_MARGIN * 2)
                )
        );

        this.addRenderableWidget(
                Button.builder(
                                RESET_TO_DEFAULTS,
                                button -> {
                                    MobletsConfig.resetEncounters();
                                    this.rebuildWidgets();
                                }
                        )
                        .bounds(
                                (this.width - resetWidth) / 2,
                                this.height - 52,
                                resetWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        int backWidth = Math.max(
                1,
                Math.min(
                        BACK_BUTTON_WIDTH,
                        this.width - (SCREEN_MARGIN * 2)
                )
        );

        this.addRenderableWidget(
                Button.builder(
                                CommonComponents.GUI_BACK,
                                button -> this.onClose()
                        )
                        .bounds(
                                (this.width - backWidth) / 2,
                                this.height - 28,
                                backWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private int wrappedHeight(Component text) {
        return this.font.split(text, this.contentWidth).size()
                * this.font.lineHeight;
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
        graphics.textWithWordWrap(
                this.font,
                DESCRIPTION,
                this.contentLeft,
                DESCRIPTION_Y,
                this.contentWidth,
                SECONDARY_TEXT_COLOR
        );
        graphics.textWithWordWrap(
                this.font,
                DESCRIPTION_MORE,
                this.contentLeft,
                this.secondDescriptionY,
                this.contentWidth,
                SECONDARY_TEXT_COLOR
        );
    }

    @Override
    public void onClose() {
        MobletsConfig.save();
        this.minecraft.setScreen(this.parent);
    }

    private final class EncounterList
            extends ContainerObjectSelectionList<
                    EncounterList.Entry> {

        private static final int ROW_HEIGHT = 48;

        EncounterList(
                Minecraft minecraft,
                int width,
                int height,
                int y
        ) {
            super(
                    minecraft,
                    width,
                    height,
                    y,
                    ROW_HEIGHT
            );

            for (EncounterDefinition definition
                    : EncounterRegistry.all()) {
                this.addEntry(new Entry(definition));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.max(
                    1,
                    Math.min(
                            MAX_CONTENT_WIDTH,
                            this.getWidth()
                                    - (SCREEN_MARGIN * 2)
                    )
            );
        }

        private final class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {

            private final EncounterDefinition definition;
            private final Component name;
            private final Component description;
            private final CycleButton<Boolean> enabledButton;
            private final List<AbstractWidget> controls;

            Entry(EncounterDefinition definition) {
                this.definition = definition;
                this.name = Component.translatable(
                        TRANSLATION_PREFIX
                                + definition.id()
                                + ".name"
                );
                this.description = Component.translatable(
                        TRANSLATION_PREFIX
                                + definition.id()
                                + ".description"
                );
                this.enabledButton = CycleButton.onOffBuilder(
                                MobletsConfig.encounterEnabled(
                                        definition
                                )
                        )
                        .displayOnlyValue()
                        .create(
                                0,
                                0,
                                TOGGLE_WIDTH,
                                BUTTON_HEIGHT,
                                this.name,
                                (button, enabled) ->
                                        MobletsConfig
                                                .setEncounterEnabled(
                                                        definition,
                                                        enabled
                                                )
                        );
                this.controls = List.of(this.enabledButton);
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                int contentX = this.getContentX();
                int contentY = this.getContentY();
                int contentWidth = this.getContentWidth();
                int buttonX = contentX
                        + contentWidth
                        - TOGGLE_WIDTH;

                graphics.text(
                        MobletsEncountersScreen.this.font,
                        this.name,
                        contentX,
                        contentY + 6,
                        -1
                );

                this.enabledButton.setRectangle(
                        TOGGLE_WIDTH,
                        BUTTON_HEIGHT,
                        buttonX,
                        contentY
                );
                this.enabledButton.extractRenderState(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick
                );

                graphics.textWithWordWrap(
                        MobletsEncountersScreen.this.font,
                        this.description,
                        contentX,
                        contentY + 26,
                        contentWidth,
                        SECONDARY_TEXT_COLOR
                );
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.controls;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.controls;
            }
        }
    }
}
