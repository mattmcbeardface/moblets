package com.moblets.client.gui;

import java.util.List;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MobletsTamingScreen extends Screen {

    private static final Component TITLE =
            Component.translatable("screen.baby_mobs.config.taming");
    private static final Component TAMEABLE_MOBS =
            Component.translatable(
                    "screen.baby_mobs.config.taming.global"
            );
    private static final Component TAMEABLE =
            Component.translatable(
                    "screen.baby_mobs.config.taming.tameable"
            );
    private static final Component CHANCE_HEADING =
            Component.translatable(
                    "screen.baby_mobs.config.taming.chance.heading"
            );
    private static final Component CHANCE_DESCRIPTION =
            Component.translatable(
                    "screen.baby_mobs.config.taming.chance.description"
            );
    private static final Component CHANCE_DESCRIPTION_MORE =
            Component.translatable(
                    "screen.baby_mobs.config.taming.chance.description_more"
            );
    private static final Component RESET_TO_DEFAULTS =
            Component.translatable(
                    "screen.baby_mobs.config.taming.reset"
            );

    private static final int TITLE_Y = 12;
    private static final int GLOBAL_CONTROL_Y = 34;
    private static final int SECTION_HEADING_Y = 64;
    private static final int DESCRIPTION_Y = 78;
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
    private TamingSpeciesList speciesList;

    public MobletsTamingScreen(Screen parent) {
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

        boolean globalEnabled =
                MobletsConfig.globalTamingEnabled();

        this.addRenderableWidget(
                CycleButton.onOffBuilder(globalEnabled)
                        .displayOnlyValue()
                        .create(
                                this.contentLeft
                                        + this.contentWidth
                                        - TOGGLE_WIDTH,
                                GLOBAL_CONTROL_Y,
                                TOGGLE_WIDTH,
                                BUTTON_HEIGHT,
                                TAMEABLE_MOBS,
                                (button, enabled) -> {
                                    MobletsConfig
                                            .setGlobalTamingEnabled(
                                                    enabled
                                            );

                                    this.speciesList
                                            .setGlobalEnabled(enabled);
                                }
                        )
        );

        int firstDescriptionHeight =
                this.wrappedHeight(CHANCE_DESCRIPTION);
        this.secondDescriptionY = DESCRIPTION_Y
                + firstDescriptionHeight
                + HEADER_TEXT_GAP;
        int listTop = this.secondDescriptionY
                + this.wrappedHeight(CHANCE_DESCRIPTION_MORE)
                + LIST_GAP;
        int listHeight = Math.max(
                1,
                this.height - FOOTER_HEIGHT - listTop
        );

        this.speciesList = this.addRenderableWidget(
                new TamingSpeciesList(
                        this.minecraft,
                        this.width,
                        listHeight,
                        listTop,
                        globalEnabled
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
                                    MobletsConfig.resetTaming();
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
        graphics.text(
                this.font,
                TAMEABLE_MOBS,
                this.contentLeft,
                GLOBAL_CONTROL_Y + 6,
                -1
        );
        graphics.text(
                this.font,
                CHANCE_HEADING,
                this.contentLeft,
                SECTION_HEADING_Y,
                -1
        );
        graphics.textWithWordWrap(
                this.font,
                CHANCE_DESCRIPTION,
                this.contentLeft,
                DESCRIPTION_Y,
                this.contentWidth,
                SECONDARY_TEXT_COLOR
        );
        graphics.textWithWordWrap(
                this.font,
                CHANCE_DESCRIPTION_MORE,
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

    private final class TamingSpeciesList
            extends ContainerObjectSelectionList<
                    TamingSpeciesList.Entry> {

        private static final int ROW_HEIGHT = 28;
        private static final int ROW_GAP = 8;
        private static final int MIN_NAME_WIDTH = 60;
        private static final int MAX_NAME_WIDTH = 130;
        private static final int MIN_TAMEABLE_WIDTH = 90;
        private static final int MAX_TAMEABLE_WIDTH = 130;

        private boolean globalEnabled;

        TamingSpeciesList(
                Minecraft minecraft,
                int width,
                int height,
                int y,
                boolean globalEnabled
        ) {
            super(
                    minecraft,
                    width,
                    height,
                    y,
                    ROW_HEIGHT
            );
            this.globalEnabled = globalEnabled;

            for (MobletDefinition definition
                    : MobletRegistry.tameableMoblets()) {
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

        void setGlobalEnabled(boolean enabled) {
            this.globalEnabled = enabled;

            for (Entry entry : this.children()) {
                entry.updateEnabledState();
            }
        }

        private final class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {

            private final MobletDefinition definition;
            private final TamingChanceSlider chanceSlider;
            private final CycleButton<Boolean> tameableButton;
            private final List<AbstractWidget> controls;

            Entry(MobletDefinition definition) {
                this.definition = definition;
                this.chanceSlider =
                        new TamingChanceSlider(definition);
                this.tameableButton =
                        CycleButton.onOffBuilder(
                                        MobletsConfig
                                                .speciesTamingEnabled(
                                                        definition
                                                )
                                )
                                .create(
                                        TAMEABLE,
                                        (button, enabled) -> {
                                            MobletsConfig
                                                    .setSpeciesTamingEnabled(
                                                            definition,
                                                            enabled
                                                    );
                                            this.updateEnabledState();
                                        }
                                );
                this.controls = List.of(
                        this.tameableButton,
                        this.chanceSlider
                );
                this.updateEnabledState();
            }

            void updateEnabledState() {
                this.tameableButton.active =
                        TamingSpeciesList.this.globalEnabled;
                this.chanceSlider.active =
                        TamingSpeciesList.this.globalEnabled
                                && MobletsConfig
                                        .speciesTamingEnabled(
                                                this.definition
                                        );
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                int availableWidth = Math.max(
                        1,
                        this.getContentWidth()
                                - (ROW_GAP * 2)
                );
                int nameWidth = Math.min(
                        MAX_NAME_WIDTH,
                        Math.max(
                                MIN_NAME_WIDTH,
                                availableWidth / 3
                        )
                );
                int tameableWidth = Math.min(
                        MAX_TAMEABLE_WIDTH,
                        Math.max(
                                MIN_TAMEABLE_WIDTH,
                                availableWidth / 3
                        )
                );
                int sliderWidth = Math.max(
                        1,
                        availableWidth
                                - nameWidth
                                - tameableWidth
                );
                int controlY = this.getContentY()
                        + (this.getContentHeight()
                                - BUTTON_HEIGHT) / 2;
                int tameableX = this.getContentX()
                        + nameWidth
                        + ROW_GAP;
                int sliderX = tameableX
                        + tameableWidth
                        + ROW_GAP;

                graphics.text(
                        MobletsTamingScreen.this.font,
                        this.definition.entityType()
                                .getDescription(),
                        this.getContentX(),
                        controlY + 6,
                        -1
                );

                this.tameableButton.setRectangle(
                        tameableWidth,
                        BUTTON_HEIGHT,
                        tameableX,
                        controlY
                );
                this.tameableButton.extractRenderState(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick
                );

                this.chanceSlider.setRectangle(
                        sliderWidth,
                        BUTTON_HEIGHT,
                        sliderX,
                        controlY
                );
                this.chanceSlider.extractRenderState(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick
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

    private static final class TamingChanceSlider
            extends AbstractSliderButton {

        private final MobletDefinition definition;

        TamingChanceSlider(MobletDefinition definition) {
            super(
                    0,
                    0,
                    100,
                    BUTTON_HEIGHT,
                    CommonComponents.EMPTY,
                    MobletsConfig.tamingPercent(definition)
                            / 100.0D
            );
            this.definition = definition;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                    Component.translatable(
                            "screen.baby_mobs.config.taming.chance_value",
                            Math.round(this.value * 100.0D)
                    )
            );
        }

        @Override
        protected void applyValue() {
            MobletsConfig.setTamingPercent(
                    this.definition,
                    Math.round(this.value * 100.0D)
            );
        }
    }
}
