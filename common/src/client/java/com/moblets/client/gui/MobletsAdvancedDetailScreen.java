package com.moblets.client.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.moblets.balance.MobletBalanceValues;
import com.moblets.balance.MobletBalanceValues.Presentation;
import com.moblets.config.MobletsConfig;
import com.moblets.registry.BalanceStat;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;

public final class MobletsAdvancedDetailScreen extends Screen {
    private static final String TRANSLATION_PREFIX =
            "screen.baby_mobs.config.advanced.";

    private static final Component WILD_PROFILE =
            Component.translatable(
                    TRANSLATION_PREFIX + "profile.wild"
            ).withStyle(ChatFormatting.BOLD);
    private static final Component TAMED_PROFILE =
            Component.translatable(
                    TRANSLATION_PREFIX + "profile.tamed"
            ).withStyle(ChatFormatting.BOLD);
    private static final Component MOBLET_PROFILE =
            Component.translatable(
                    TRANSLATION_PREFIX + "profile.moblet"
            ).withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION =
            Component.translatable(
                    TRANSLATION_PREFIX + "detail.description"
            );
    private static final Component DESCRIPTION_MORE =
            Component.translatable(
                    TRANSLATION_PREFIX + "detail.description_more"
            );
    private static final Component NATIVE_NOTE =
            Component.translatable(
                    TRANSLATION_PREFIX + "detail.native_note"
            );
    private static final Component CREEPER_NOTE =
            Component.translatable(
                    TRANSLATION_PREFIX + "detail.creeper_note"
            );

    private static final int TITLE_Y = 12;
    private static final int DESCRIPTION_Y = 32;
    private static final int TEXT_GAP = 2;
    private static final int LIST_GAP = 8;
    private static final int FOOTER_HEIGHT = 60;
    private static final int SCREEN_MARGIN = 16;
    private static final int MAX_CONTENT_WIDTH = 520;
    private static final int MAX_SLIDER_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int RESET_BUTTON_WIDTH = 220;
    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int SECONDARY_TEXT_COLOR = -8355712;

    private final Screen parent;
    private final MobletDefinition definition;

    private int contentLeft;
    private int contentWidth;
    private int descriptionMoreY;
    private int nativeNoteY;
    private int creeperNoteY;
    private int difficultyId;

    public MobletsAdvancedDetailScreen(
            Screen parent,
            MobletDefinition definition
    ) {
        super(
                Component.translatable(
                        TRANSLATION_PREFIX + "detail_title",
                        definition.entityType().getDescription()
                )
        );
        this.parent = parent;
        this.definition = definition;
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
        this.difficultyId = this.minecraft.level == null
                ? Difficulty.NORMAL.getId()
                : this.minecraft.level.getDifficulty().getId();

        this.descriptionMoreY = DESCRIPTION_Y
                + this.wrappedHeight(DESCRIPTION)
                + TEXT_GAP;
        this.nativeNoteY = this.descriptionMoreY
                + this.wrappedHeight(DESCRIPTION_MORE)
                + TEXT_GAP;
        this.creeperNoteY = this.nativeNoteY
                + this.wrappedHeight(NATIVE_NOTE)
                + TEXT_GAP;

        int listTop = this.definition.hasBlastRadius()
                ? this.creeperNoteY
                        + this.wrappedHeight(CREEPER_NOTE)
                        + LIST_GAP
                : this.nativeNoteY
                        + this.wrappedHeight(NATIVE_NOTE)
                        + LIST_GAP;
        int listHeight = Math.max(
                1,
                this.height - FOOTER_HEIGHT - listTop
        );

        this.addRenderableWidget(
                new SettingsList(
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
                                Component.translatable(
                                        TRANSLATION_PREFIX
                                                + "reset_moblet",
                                        this.definition.entityType()
                                                .getDescription()
                                ),
                                button -> {
                                    MobletsConfig.resetAdvanced(
                                            this.definition
                                    );
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
                this.descriptionMoreY,
                this.contentWidth,
                SECONDARY_TEXT_COLOR
        );
        graphics.textWithWordWrap(
                this.font,
                NATIVE_NOTE,
                this.contentLeft,
                this.nativeNoteY,
                this.contentWidth,
                SECONDARY_TEXT_COLOR
        );

        if (this.definition.hasBlastRadius()) {
            graphics.textWithWordWrap(
                    this.font,
                    CREEPER_NOTE,
                    this.contentLeft,
                    this.creeperNoteY,
                    this.contentWidth,
                    SECONDARY_TEXT_COLOR
            );
        }
    }

    @Override
    public void onClose() {
        MobletsConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private final class SettingsList
            extends ContainerObjectSelectionList<SettingsList.Entry> {

        private static final int ROW_HEIGHT = 28;

        SettingsList(
                Minecraft minecraft,
                int width,
                int height,
                int y
        ) {
            super(minecraft, width, height, y, ROW_HEIGHT);

            if (definition.supportsTaming()) {
                this.addProfile(WILD_PROFILE, MobletProfile.WILD);
                this.addProfile(TAMED_PROFILE, MobletProfile.TAMED);
            } else {
                this.addProfile(MOBLET_PROFILE, MobletProfile.WILD);
            }
        }

        private void addProfile(
                Component label,
                MobletProfile profile
        ) {
            this.addEntry(new SectionEntry(label));

            for (BalanceStat stat
                    : definition.balanceStats(profile)) {
                this.addEntry(new BalanceEntry(profile, stat));
            }

            if (definition.hasBlastRadius()) {
                this.addEntry(new BlastRadiusEntry(profile));
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

        private abstract class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {
        }

        private final class SectionEntry extends Entry {
            private final Component label;

            SectionEntry(Component label) {
                this.label = label;
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
                                - MobletsAdvancedDetailScreen.this.font
                                        .lineHeight) / 2;

                graphics.text(
                        MobletsAdvancedDetailScreen.this.font,
                        this.label,
                        this.getContentX(),
                        textY,
                        -1
                );
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }

        private final class BalanceEntry extends Entry {
            private final Component label;
            private final BalanceSlider slider;
            private final List<AbstractWidget> controls;

            BalanceEntry(
                    MobletProfile profile,
                    BalanceStat stat
            ) {
                this.label = balanceLabel(profile, stat);
                this.slider = new BalanceSlider(profile, stat);
                this.controls = List.of(this.slider);
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                SettingsList.this.extractRow(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick,
                        this.label,
                        this.slider,
                        this.getContentX(),
                        this.getContentY(),
                        this.getContentWidth(),
                        this.getContentHeight()
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

        private final class BlastRadiusEntry extends Entry {
            private final Component label = Component.translatable(
                    TRANSLATION_PREFIX + "blast_radius"
            );
            private final BlastRadiusSlider slider;
            private final List<AbstractWidget> controls;

            BlastRadiusEntry(MobletProfile profile) {
                this.slider = new BlastRadiusSlider(profile);
                this.controls = List.of(this.slider);
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                SettingsList.this.extractRow(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick,
                        this.label,
                        this.slider,
                        this.getContentX(),
                        this.getContentY(),
                        this.getContentWidth(),
                        this.getContentHeight()
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

        private void extractRow(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTick,
                Component label,
                AbstractWidget slider,
                int contentX,
                int contentY,
                int contentWidth,
                int contentHeight
        ) {
            int sliderWidth = Math.max(
                    1,
                    Math.min(
                            MAX_SLIDER_WIDTH,
                            contentWidth / 2
                    )
            );
            int sliderX = contentX
                    + contentWidth
                    - sliderWidth;
            int controlY = contentY
                    + (contentHeight - BUTTON_HEIGHT) / 2;
            int textY = contentY
                    + (contentHeight
                            - MobletsAdvancedDetailScreen.this.font
                                    .lineHeight) / 2;

            graphics.text(
                    MobletsAdvancedDetailScreen.this.font,
                    label,
                    contentX,
                    textY,
                    -1
            );
            slider.setRectangle(
                    sliderWidth,
                    BUTTON_HEIGHT,
                    sliderX,
                    controlY
            );
            slider.extractRenderState(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
        }
    }

    private Component balanceLabel(
            MobletProfile profile,
            BalanceStat stat
    ) {
        if (stat == BalanceStat.ACCURACY) {
            String key;

            if (this.definition.id().equals("witch")) {
                key = profile == MobletProfile.TAMED
                        ? "support_throw_spread"
                        : "potion_spread";
            } else {
                key = "projectile_spread";
            }

            return Component.translatable(
                    TRANSLATION_PREFIX + key
            );
        }

        if (this.definition.id().equals("creeper")
                && stat == BalanceStat.DAMAGE) {
            return Component.translatable(
                    TRANSLATION_PREFIX
                            + "explosion_damage_scale"
            );
        }

        return Component.translatable(
                TRANSLATION_PREFIX
                        + "stat."
                        + stat.configKey()
        );
    }

    private SliderState sliderState(
            MobletProfile profile,
            BalanceStat stat
    ) {
        Presentation presentation =
                MobletBalanceValues.presentation(
                        this.definition,
                        stat
                );
        double displayedValue =
                MobletBalanceValues.displayedValue(
                        this.definition,
                        profile,
                        stat,
                        this.difficultyId
                );
        double maximum = presentation == Presentation.SPREAD
                ? Math.max(
                        MobletBalanceValues.MAX_SPREAD,
                        Math.ceil(displayedValue)
                )
                : Math.max(
                        MobletBalanceValues.MAX_PERCENT,
                        Math.ceil(displayedValue)
                );

        return new SliderState(
                presentation,
                displayedValue,
                maximum
        );
    }

    private static double sliderPosition(
            Presentation presentation,
            double displayedValue,
            double maximum
    ) {
        double clamped = Math.max(
                0.0D,
                Math.min(maximum, displayedValue)
        );

        if (presentation == Presentation.SPREAD) {
            return Math.sqrt(clamped / maximum);
        }

        return clamped / maximum;
    }

    private static String formatValue(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private record SliderState(
            Presentation presentation,
            double displayedValue,
            double maximum
    ) {
    }

    private final class BalanceSlider extends AbstractSliderButton {
        private final MobletProfile profile;
        private final BalanceStat stat;
        private final Presentation presentation;
        private final double maximum;

        BalanceSlider(
                MobletProfile profile,
                BalanceStat stat
        ) {
            this(profile, stat, sliderState(profile, stat));
        }

        private BalanceSlider(
                MobletProfile profile,
                BalanceStat stat,
                SliderState state
        ) {
            super(
                    0,
                    0,
                    100,
                    BUTTON_HEIGHT,
                    CommonComponents.EMPTY,
                    sliderPosition(
                            state.presentation(),
                            state.displayedValue(),
                            state.maximum()
                    )
            );
            this.profile = profile;
            this.stat = stat;
            this.presentation = state.presentation();
            this.maximum = state.maximum();
            this.updateMessage();
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (this.canChangeValue
                    && (event.isLeft() || event.isRight())) {
                double direction = event.isLeft()
                        ? -1.0D
                        : 1.0D;
                double current = this.rawDisplayedValue();
                double selected;

                if (this.presentation == Presentation.SPREAD) {
                    selected = Math.round(
                            (current
                                    + direction
                                    * MobletBalanceValues.SPREAD_STEP)
                                    / MobletBalanceValues.SPREAD_STEP
                    ) * MobletBalanceValues.SPREAD_STEP;
                } else if (direction < 0.0D) {
                    selected = Math.ceil(current) - 1.0D;
                } else {
                    selected = Math.floor(current) + 1.0D;
                }

                this.setValue(
                        sliderPosition(
                                this.presentation,
                                selected,
                                this.maximum
                        )
                );
                return true;
            }

            return super.keyPressed(event);
        }

        @Override
        protected void updateMessage() {
            String value = formatValue(this.rawDisplayedValue());
            String key = this.presentation == Presentation.SPREAD
                    ? "spread_value"
                    : "percent_value";

            this.setMessage(
                    Component.translatable(
                            TRANSLATION_PREFIX + key,
                            value
                    )
            );
        }

        @Override
        protected void applyValue() {
            double selected;

            if (this.presentation == Presentation.SPREAD) {
                selected = Math.round(
                        this.rawDisplayedValue()
                                / MobletBalanceValues.SPREAD_STEP
                ) * MobletBalanceValues.SPREAD_STEP;
            } else {
                selected = Math.round(this.rawDisplayedValue());
            }

            this.value = sliderPosition(
                    this.presentation,
                    selected,
                    this.maximum
            );
            MobletBalanceValues.setDisplayedValue(
                    definition,
                    this.profile,
                    this.stat,
                    difficultyId,
                    selected
            );
        }

        private double rawDisplayedValue() {
            if (this.presentation == Presentation.SPREAD) {
                return this.value * this.value * this.maximum;
            }

            return this.value * this.maximum;
        }
    }

    private final class BlastRadiusSlider extends AbstractSliderButton {
        private static final int MAX_RADIUS = 10;

        private final MobletProfile profile;

        BlastRadiusSlider(MobletProfile profile) {
            super(
                    0,
                    0,
                    100,
                    BUTTON_HEIGHT,
                    CommonComponents.EMPTY,
                    MobletsConfig.blastRadius(
                            definition,
                            profile
                    ) / (double) MAX_RADIUS
            );
            this.profile = profile;
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
                        this.value + direction / MAX_RADIUS
                );
                return true;
            }

            return super.keyPressed(event);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                    Component.translatable(
                            TRANSLATION_PREFIX + "radius_value",
                            this.currentRadius()
                    )
            );
        }

        @Override
        protected void applyValue() {
            int radius = this.currentRadius();
            this.value = radius / (double) MAX_RADIUS;
            MobletsConfig.setBlastRadius(
                    definition,
                    this.profile,
                    radius
            );
        }

        private int currentRadius() {
            return (int) Math.round(this.value * MAX_RADIUS);
        }
    }
}
