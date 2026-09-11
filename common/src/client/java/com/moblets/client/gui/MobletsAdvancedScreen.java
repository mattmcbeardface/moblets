package com.moblets.client.gui;

import java.util.List;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MobletsAdvancedScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("screen.baby_mobs.config.advanced");
    private static final Component DESCRIPTION =
            Component.translatable(
                    "screen.baby_mobs.config.advanced.description"
            );
    private static final Component CONFIGURE =
            Component.translatable(
                    "screen.baby_mobs.config.advanced.configure"
            );
    private static final Component RESET_ALL =
            Component.translatable(
                    "screen.baby_mobs.config.advanced.reset_all"
            );

    private static final int TITLE_Y = 12;
    private static final int DESCRIPTION_Y = 34;
    private static final int LIST_GAP = 8;
    private static final int FOOTER_HEIGHT = 60;
    private static final int SCREEN_MARGIN = 16;
    private static final int MAX_CONTENT_WIDTH = 520;
    private static final int CONFIGURE_BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int RESET_BUTTON_WIDTH = 170;
    private static final int BACK_BUTTON_WIDTH = 200;
    private static final int SECONDARY_TEXT_COLOR = -8355712;

    private final Screen parent;

    private int contentLeft;
    private int contentWidth;

    public MobletsAdvancedScreen(Screen parent) {
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

        int listTop = DESCRIPTION_Y
                + this.wrappedHeight(DESCRIPTION)
                + LIST_GAP;
        int listHeight = Math.max(
                1,
                this.height - FOOTER_HEIGHT - listTop
        );

        this.addRenderableWidget(
                new MobletList(
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
                                RESET_ALL,
                                button -> {
                                    MobletsConfig.resetAllAdvanced();
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
    }

    @Override
    public void onClose() {
        MobletsConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private final class MobletList
            extends ContainerObjectSelectionList<MobletList.Entry> {

        private static final int ROW_HEIGHT = 28;

        MobletList(
                Minecraft minecraft,
                int width,
                int height,
                int y
        ) {
            super(minecraft, width, height, y, ROW_HEIGHT);

            for (MobletDefinition definition
                    : MobletRegistry.advancedMoblets()) {
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

            private final MobletDefinition definition;
            private final Component label;
            private final Button configureButton;
            private final List<AbstractWidget> controls;

            Entry(MobletDefinition definition) {
                this.definition = definition;
                this.label = definition.entityType()
                        .getDescription()
                        .copy()
                        .withStyle(ChatFormatting.BOLD);
                this.configureButton = Button.builder(
                                CONFIGURE,
                                button -> Minecraft.getInstance()
                                        .gui.setScreen(
                                                new MobletsAdvancedDetailScreen(
                                                        MobletsAdvancedScreen.this,
                                                        definition
                                                )
                                        )
                        )
                        .size(
                                CONFIGURE_BUTTON_WIDTH,
                                BUTTON_HEIGHT
                        )
                        .build();
                this.controls = List.of(this.configureButton);
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
                        - CONFIGURE_BUTTON_WIDTH;

                graphics.text(
                        MobletsAdvancedScreen.this.font,
                        this.label,
                        this.getContentX(),
                        controlY + 6,
                        -1
                );

                this.configureButton.setPosition(buttonX, controlY);
                this.configureButton.extractRenderState(
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
}
