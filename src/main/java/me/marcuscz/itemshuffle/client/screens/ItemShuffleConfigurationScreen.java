package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.screens.widgets.ItemShuffleSliderWidget;
import me.marcuscz.itemshuffle.game.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static java.lang.Math.floor;

public class ItemShuffleConfigurationScreen extends Screen {

    private final Screen parent;
    private final GameSettings settings = ItemShuffle.getInstance().getSettings();
    private boolean changes;
    private int gameTypeIndex, itemTypeIndex, teamDataIndex, itemDimensionIndex, showItemsIndex;

    public ItemShuffleConfigurationScreen(Screen parent) {
        super(Component.literal("ItemShuffle Options"));
        this.parent = parent;
        gameTypeIndex = GameType.getIndex(settings.gameType);
        itemTypeIndex = ItemGenType.getIndex(settings.itemType);
        teamDataIndex = TeamData.Show.getIndex(settings.teamShowType);
        itemDimensionIndex = ItemGenDimension.getIndex(settings.itemGenDimension);
        showItemsIndex = ItemShowType.getIndex(settings.showItemsMode);
    }

    protected void init() {

        changes = false;

        AbstractSliderButton gameDurationWidget = new ItemShuffleSliderWidget(
                this.width / 2 - 160,
                50,
                150,
                20,
                "Round Duration",
                (settings.time-1200) / 72000d, (slider, title1, value) -> Component.literal("Round Duration: " + ((int) floor(value * 60+1)) + "m"),
                value -> {
                    settings.time = (int) floor((value * 60)) * 1200 +1200;
                    changes = true;
                }
        );
        gameDurationWidget.active = settings.gameType != GameType.BOARD;
        this.addRenderableWidget(gameDurationWidget);

        Button removeItemsWidget = Button.builder(
                Component.literal("Remove Items: " + (settings.removeItems ? "On" : "Off")),
                button -> {
                    settings.removeItems = !settings.removeItems;
                    button.setMessage(Component.literal("Remove Items: " + (settings.removeItems ? "On" : "Off")));
                    changes = true;
                }).bounds(
                this.width / 2 + 10,
                150,
                150,
                20
                ).build();
        removeItemsWidget.active = !settings.blockMode;
        this.addRenderableWidget(removeItemsWidget);

        Button itemTypeWidget = Button.builder(
                Component.literal("Item Generator: " + settings.itemType.toString()),
                button -> {
                    itemTypeIndex++;
                    if (itemTypeIndex >= ItemGenType.values().length) {
                        itemTypeIndex = 0;
                    }
                    settings.itemType = ItemGenType.values()[itemTypeIndex];
                    button.setMessage(Component.literal("Item Generator: " + settings.itemType.toString()));
                    changes = true;
                }).bounds(
                this.width / 2 + 10,
                75,
                150,
                20
        ).build();
        itemTypeWidget.active = settings.gameType != GameType.TWITCH && !settings.gameType.isBoardType();
        this.addRenderableWidget(itemTypeWidget);

        Button itemDimensionWidget = Button.builder(
                Component.literal("Generator Dimension: " + settings.itemGenDimension.toString()),
                button -> {
                    itemDimensionIndex++;
                    if (itemDimensionIndex >= ItemGenDimension.values().length) {
                        itemDimensionIndex = 0;
                    }
                    settings.itemGenDimension = ItemGenDimension.values()[itemDimensionIndex];
                    button.setMessage(Component.literal("Generator Dimension: " + settings.itemGenDimension.toString()));
                    changes = true;
                }).bounds(
                this.width / 2 - 160,
                200,
                150,
                20
        ).build();
        this.addRenderableWidget(itemDimensionWidget);

        Button itemDifficultyButton = Button.builder(
                Component.literal("Item Difficulty: " + GameSettings.DIFFICULTY_NAMES[settings.itemDifficulty]),
                button -> {
                    settings.itemDifficulty++;
                    if (settings.itemDifficulty > GameSettings.MAX_DIFFICULTY) {
                        settings.itemDifficulty = 0;
                    }
                    button.setMessage(Component.literal("Item Difficulty: " + GameSettings.DIFFICULTY_NAMES[settings.itemDifficulty]));
                    changes = true;
                }).bounds(
                this.width / 2 + 10,
                200,
                150,
                20
        ).build();
        this.addRenderableWidget(itemDifficultyButton);


        Button integrationSettings = Button.builder(
                Component.literal("Twitch Settings"),
                button -> minecraft.setScreen(new TwitchConfigurationScreen(this))
        ).bounds(this.width / 2 - 85, 225, 170, 20).build();
        integrationSettings.active = settings.gameType == GameType.TWITCH;
        this.addRenderableWidget(integrationSettings);

        Button boardTargetWidget = Button.builder(
                Component.literal("Board Target: " + settings.boardTarget),
                button -> {
                    settings.boardTarget += 2;
                    if (settings.boardTarget > settings.boardSize) settings.boardTarget = 3;
                    button.setMessage(Component.literal("Board Target: " + settings.boardTarget));
                    changes = true;
                }).bounds(
                this.width / 2 + 10,
                175,
                150,
                20
        ).build();
        boardTargetWidget.active = settings.gameType.isBoardType() && settings.boardSize > 3;
        this.addRenderableWidget(boardTargetWidget);

        Button boardSizeWidget = Button.builder(
                Component.literal("Board Size: " + settings.boardSize),
                button -> {
                    settings.boardSize += 2;
                    boardTargetWidget.active = true;
                    if (settings.boardSize > 7) {
                        settings.boardSize = 3;
                        boardTargetWidget.active = false;
                    }
                    if (settings.boardTarget > settings.boardSize) {
                        settings.boardTarget = settings.boardSize;
                        boardTargetWidget.setMessage(Component.literal("Board Target: " + settings.boardTarget));
                    }
                    button.setMessage(Component.literal("Board Size: " + settings.boardSize));
                    changes = true;
                }).bounds(
                this.width / 2 - 160,
                175,
                150,
                20
        ).build();
        boardSizeWidget.active = settings.gameType.isBoardType();
        this.addRenderableWidget(boardSizeWidget);


        Button teamDataShowWidget = Button.builder(
                Component.literal("Show Team Data: " + settings.teamShowType.toString()),
                button -> {
                    teamDataIndex++;
                    if (teamDataIndex >= TeamData.Show.values().length) {
                        teamDataIndex = 0;
                    }
                    settings.teamShowType = TeamData.Show.values()[teamDataIndex];
                    button.setMessage(Component.literal("Show Team Data: " + settings.teamShowType.toString()));
                    changes = true;
                }
        ).bounds(
                this.width / 2 - 160,
                150,
                150,
                20
        ).build();
        teamDataShowWidget.active = settings.gameType == GameType.TEAM;
        this.addRenderableWidget(teamDataShowWidget);

        Button blockModeWidget = Button.builder(
                Component.literal("Standing mode: " + (settings.blockMode ? "On" : "Off")),
                button -> {
                    settings.blockMode = !settings.blockMode;
                    button.setMessage(Component.literal("Standing mode: " + (settings.blockMode ? "On" : "Off")));
                    removeItemsWidget.active = !settings.blockMode;
                    changes = true;
                }
        ).bounds(
                this.width / 2 + 10,
                50,
                150,
                20
        ).build();
        this.addRenderableWidget(blockModeWidget);

        Button showTimersWidget = Button.builder(
                Component.literal("Show Timer: " + (settings.showTimers ? "On" : "Off")),
                button -> {
                    settings.showTimers = !settings.showTimers;
                    button.setMessage(Component.literal("Show Timer: " + (settings.showTimers ? "On" : "Off")));
                    changes = true;
                }
        ).bounds(
                this.width / 2 - 160,
                100,
                150,
                20
        ).build();
        showTimersWidget.active = settings.gameType != GameType.BOARD;
        this.addRenderableWidget(showTimersWidget);

        Button pauseOnFailWidget = Button.builder(Component.literal("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")),
                button -> {
                    settings.pauseOnFail = !settings.pauseOnFail;
                    button.setMessage(Component.literal("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")));
                    changes = true;
                }).bounds(
                this.width / 2 + 10,
                100,
                150,
                20
        ).build();
        pauseOnFailWidget.active = !settings.gameType.isBoardType();
        this.addRenderableWidget(pauseOnFailWidget);

        Button showItemWidget = Button.builder(
                Component.literal("Show Items: " + settings.showItemsMode.toString()),
                button -> {
                    showItemsIndex++;
                    if (showItemsIndex >= ItemShowType.values().length) {
                        showItemsIndex = 0;
                    }
                    settings.showItemsMode = ItemShowType.values()[showItemsIndex];
                    button.setMessage(Component.literal("Show Items: " + settings.showItemsMode.toString()));
                    changes = true;
                }
        ).bounds(
                this.width / 2 + 10,
                125,
                150,
                20
        ).build();
        showItemWidget.active = !settings.gameType.isBoardType();
        this.addRenderableWidget(showItemWidget);

        Button gameTypeWidget = Button.builder(
                Component.literal("Game Type: " + settings.gameType.toString()),
                button -> {
                    gameTypeIndex++;
                    if (gameTypeIndex >= GameType.values().length) {
                        gameTypeIndex = 0;
                    }
                    settings.gameType = GameType.values()[gameTypeIndex];
                    button.setMessage(Component.literal("Game Type: " + settings.gameType.toString()));
                    changes = true;
                    itemTypeWidget.active = false;
                    integrationSettings.active = false;
                    teamDataShowWidget.active = false;
                    boardSizeWidget.active = false;
                    boardTargetWidget.active = false;
                    gameDurationWidget.active = true;
                    showItemWidget.active = true;
                    pauseOnFailWidget.active = true;
                    showTimersWidget.active = true;

                    switch (settings.gameType) {
                        case TWITCH:
                            integrationSettings.active = true;
                            break;
                        case TEAM:
                            teamDataShowWidget.active = true;
                            itemTypeWidget.active = true;
                            break;
                        case BOARD:
                            boardSizeWidget.active = true;
                            boardTargetWidget.active = true;
                            gameDurationWidget.active = false;
                            showItemWidget.active = false;
                            pauseOnFailWidget.active = false;
                            showTimersWidget.active = false;
                            break;
                        case TIC_TAC_TOE:
                            boardSizeWidget.active = true;
                            boardTargetWidget.active = true;
                            showItemWidget.active = false;
                            pauseOnFailWidget.active = false;
                            showTimersWidget.active = true;
                            break;
                        case SOLO:
                            itemTypeWidget.active = true;
                            break;
                    }
                }).bounds(
                this.width / 2 - 160,
                75,
                150,
                20
                ).build();
        this.addRenderableWidget(gameTypeWidget);

        Button giveFoodWidget = Button.builder(
                Component.literal("Give Food: " + (settings.giveFood ? "On" : "Off")),
                button -> {
                    settings.giveFood = !settings.giveFood;
                    button.setMessage(Component.literal("Give Food: " + (settings.giveFood ? "On" : "Off")));
                    changes = true;
                }
        ).bounds(
                this.width / 2 - 160,
                125,
                150,
                20
        ).build();
        this.addRenderableWidget(giveFoodWidget);

        Button done = Button.builder(CommonComponents.GUI_DONE, button -> onDone()).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build();
        this.addRenderableWidget(done);

    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
//        this.renderBackground(context, mouseX, mouseY, delta);


        context.drawString(this.font, "ItemShuffle settings", this.width / 2 - font.width("ItemShuffle settings")/2, 10, 16777215);
        super.render(context, mouseX, mouseY, delta);
    }

    private void onDone() {
        if (changes) {
            if (settings.gameType == GameType.TWITCH) settings.itemType = ItemGenType.RANDOM;
            settings.printSettings();
            ItemShuffleClient.getInstance().saveGameSettings();
        }
        onClose();
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }


}
