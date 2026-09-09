package com.moblets.client.gui;

import java.util.List;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.EncounterRegistry;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MobletsSpawningScreen extends Screen {

    private static final Component TITLE =
            Component.translatable("screen.baby_mobs.config.spawning");
    private static final Component MOBLET_SPAWNING =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.global"
            );
    private static final Component SPAWNING =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.species"
            );
    private static final Component CHANCE_HEADING =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.chance.heading"
            );
    private static final Component CHANCE_DESCRIPTION =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.chance.description"
            );
    private static final Component CHANCE_DESCRIPTION_MORE =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.chance.description_more"
            );
    private static final Component PILLAGER_OUTPOST =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.pillager_outpost"
            );
    private static final Component MOBLETS_PER_OUTPOST =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.moblets_per_outpost"
            );
    private static final Component RESET_TO_DEFAULTS =
            Component.translatable(
                    "screen.baby_mobs.config.spawning.reset"
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
    private SpawningSettingsList settingsList;

    public MobletsSpawningScreen(Screen parent) {
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

        boolean globalEnabled = MobletsConfig.spawningEnabled();

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
                                MOBLET_SPAWNING,
                                (button, enabled) -> {
                                    MobletsConfig.setSpawningEnabled(
                                            enabled
                                    );
                                    this.settingsList
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

        this.settingsList = this.addRenderableWidget(
                new SpawningSettingsList(
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
                                    MobletsConfig.resetSpawning();
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
                MOBLET_SPAWNING,
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
        this.minecraft.gui.setScreen(this.parent);
    }

    private final class SpawningSettingsList
            extends ContainerObjectSelectionList<
                    SpawningSettingsList.Entry> {

        private static final int ROW_HEIGHT = 28;
        private static final int ROW_GAP = 8;
        private static final int MIN_NAME_WIDTH = 60;
        private static final int MAX_NAME_WIDTH = 130;
        private static final int MIN_SPAWNING_WIDTH = 90;
        private static final int MAX_SPAWNING_WIDTH = 130;
        private static final int OUTPOST_COUNT_SLIDER_WIDTH = 200;

        private boolean globalEnabled;
        private final OutpostCountEntry outpostCountEntry;

        SpawningSettingsList(
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
                    : MobletRegistry.randomSpawnMoblets()) {
                this.addEntry(new SpeciesEntry(definition));
            }

            this.outpostCountEntry = new OutpostCountEntry();
            this.addEntry(new OutpostEntry());
            this.addEntry(this.outpostCountEntry);
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

        private abstract class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {

            void updateEnabledState() {
            }
        }

        private final class SpeciesEntry extends Entry {
            private final MobletDefinition definition;
            private final SpawningChanceSlider chanceSlider;
            private final CycleButton<Boolean> spawningButton;
            private final List<AbstractWidget> controls;

            SpeciesEntry(MobletDefinition definition) {
                this.definition = definition;
                this.chanceSlider =
                        new SpawningChanceSlider(definition);
                this.spawningButton =
                        CycleButton.onOffBuilder(
                                        MobletsConfig
                                                .speciesSpawningEnabled(
                                                        definition
                                                )
                                )
                                .create(
                                        SPAWNING,
                                        (button, enabled) -> {
                                            MobletsConfig
                                                    .setSpeciesSpawningEnabled(
                                                            definition,
                                                            enabled
                                                    );
                                            this.updateEnabledState();
                                        }
                                );
                this.controls = List.of(
                        this.spawningButton,
                        this.chanceSlider
                );
                this.updateEnabledState();
            }

            @Override
            void updateEnabledState() {
                this.spawningButton.active =
                        SpawningSettingsList.this.globalEnabled;
                this.chanceSlider.active =
                        SpawningSettingsList.this.globalEnabled
                                && MobletsConfig
                                        .speciesSpawningEnabled(
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
                int spawningWidth = Math.min(
                        MAX_SPAWNING_WIDTH,
                        Math.max(
                                MIN_SPAWNING_WIDTH,
                                availableWidth / 3
                        )
                );
                int sliderWidth = Math.max(
                        1,
                        availableWidth
                                - nameWidth
                                - spawningWidth
                );
                int controlY = this.getContentY()
                        + (this.getContentHeight()
                                - BUTTON_HEIGHT) / 2;
                int spawningX = this.getContentX()
                        + nameWidth
                        + ROW_GAP;
                int sliderX = spawningX
                        + spawningWidth
                        + ROW_GAP;

                graphics.text(
                        MobletsSpawningScreen.this.font,
                        this.definition.entityType()
                                .getDescription(),
                        this.getContentX(),
                        controlY + 6,
                        -1
                );

                this.spawningButton.setRectangle(
                        spawningWidth,
                        BUTTON_HEIGHT,
                        spawningX,
                        controlY
                );
                this.spawningButton.extractRenderState(
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

        private final class OutpostEntry extends Entry {
            private final CycleButton<Boolean> outpostButton;
            private final List<AbstractWidget> controls;

            OutpostEntry() {
                this.outpostButton = CycleButton.onOffBuilder(
                                MobletsConfig.encounterEnabled(
                                        EncounterRegistry
                                                .PILLAGER_OUTPOST
                                )
                        )
                        .displayOnlyValue()
                        .create(
                                0,
                                0,
                                TOGGLE_WIDTH,
                                BUTTON_HEIGHT,
                                PILLAGER_OUTPOST,
                                (button, enabled) ->
                                        this.setEnabled(enabled)
                        );
                this.controls = List.of(this.outpostButton);
                this.updateEnabledState();
            }

            private void setEnabled(boolean enabled) {
                MobletsConfig.setEncounterEnabled(
                        EncounterRegistry.PILLAGER_OUTPOST,
                        enabled
                );
                SpawningSettingsList.this.outpostCountEntry
                        .updateEnabledState();
            }

            @Override
            void updateEnabledState() {
                this.outpostButton.active =
                        SpawningSettingsList.this.globalEnabled;
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                int controlY = this.getContentY()
                        + (this.getContentHeight()
                                - BUTTON_HEIGHT) / 2;
                int buttonX = this.getContentX()
                        + this.getContentWidth()
                        - TOGGLE_WIDTH;

                graphics.text(
                        MobletsSpawningScreen.this.font,
                        PILLAGER_OUTPOST,
                        this.getContentX(),
                        controlY + 6,
                        -1
                );

                this.outpostButton.setRectangle(
                        TOGGLE_WIDTH,
                        BUTTON_HEIGHT,
                        buttonX,
                        controlY
                );
                this.outpostButton.extractRenderState(
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

        private final class OutpostCountEntry extends Entry {
            private final OutpostCountSlider countSlider =
                    new OutpostCountSlider();
            private final List<AbstractWidget> controls =
                    List.of(this.countSlider);

            OutpostCountEntry() {
                this.updateEnabledState();
            }

            @Override
            void updateEnabledState() {
                this.countSlider.active =
                        SpawningSettingsList.this.globalEnabled
                                && MobletsConfig.encounterEnabled(
                                        EncounterRegistry
                                                .PILLAGER_OUTPOST
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
                int textY = this.getContentY()
                        + (this.getContentHeight()
                                - MobletsSpawningScreen.this.font
                                        .lineHeight) / 2;
                int sliderWidth = Math.max(
                        1,
                        Math.min(
                                OUTPOST_COUNT_SLIDER_WIDTH,
                                this.getContentWidth() / 2
                        )
                );
                int sliderX = this.getContentX()
                        + this.getContentWidth()
                        - sliderWidth;
                int controlY = this.getContentY()
                        + (this.getContentHeight()
                                - BUTTON_HEIGHT) / 2;

                graphics.text(
                        MobletsSpawningScreen.this.font,
                        MOBLETS_PER_OUTPOST,
                        this.getContentX(),
                        textY,
                        SECONDARY_TEXT_COLOR
                );
                this.countSlider.setRectangle(
                        sliderWidth,
                        BUTTON_HEIGHT,
                        sliderX,
                        controlY
                );
                this.countSlider.extractRenderState(
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

    private static final class OutpostCountSlider
            extends AbstractSliderButton {

        OutpostCountSlider() {
            super(
                    0,
                    0,
                    100,
                    BUTTON_HEIGHT,
                    CommonComponents.EMPTY,
                    MobletsConfig.pillagerOutpostMobletCount()
                            / (double) MobletsConfig
                                    .MAX_PILLAGER_OUTPOST_MOBLETS
            );
            this.updateMessage();
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (this.canChangeValue
                    && (event.isLeft() || event.isRight())) {
                double direction = event.isLeft()
                        ? -1.0D
                        : 1.0D;
                this.setValue(
                        this.value
                                + direction
                                / MobletsConfig
                                        .MAX_PILLAGER_OUTPOST_MOBLETS
                );
                return true;
            }

            return super.keyPressed(event);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                    Component.translatable(
                            "screen.baby_mobs.config.spawning.outpost_count_value",
                            this.currentCount()
                    )
            );
        }

        @Override
        protected void applyValue() {
            int count = this.currentCount();
            this.value = count
                    / (double) MobletsConfig
                            .MAX_PILLAGER_OUTPOST_MOBLETS;
            MobletsConfig.setPillagerOutpostMobletCount(count);
        }

        private int currentCount() {
            return (int) Math.round(
                    this.value
                            * MobletsConfig
                                    .MAX_PILLAGER_OUTPOST_MOBLETS
            );
        }
    }

    private static final class SpawningChanceSlider
            extends AbstractSliderButton {

        private final MobletDefinition definition;

        SpawningChanceSlider(MobletDefinition definition) {
            super(
                    0,
                    0,
                    100,
                    BUTTON_HEIGHT,
                    CommonComponents.EMPTY,
                    MobletsConfig.spawnPercent(definition)
                            / 100.0D
            );
            this.definition = definition;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                    Component.translatable(
                            "screen.baby_mobs.config.spawning.chance_value",
                            Math.round(this.value * 100.0D)
                    )
            );
        }

        @Override
        protected void applyValue() {
            MobletsConfig.setSpawnPercent(
                    this.definition,
                    Math.round(this.value * 100.0D)
            );
        }
    }
}
