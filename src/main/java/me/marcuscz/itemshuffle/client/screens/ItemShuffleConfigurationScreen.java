package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.screens.widgets.ItemShuffleSliderWidget;
import me.marcuscz.itemshuffle.game.GameSettings;
import me.marcuscz.itemshuffle.game.GameType;
import me.marcuscz.itemshuffle.game.ItemGenType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import static java.lang.Math.floor;

public class ItemShuffleConfigurationScreen extends Screen {

    private final Screen parent;
    private final GameSettings settings = ItemShuffle.getInstance().getSettings();
    private boolean changes;
    private int gameTypeIndex, itemTypeIndex, teamDataIndex;

    public ItemShuffleConfigurationScreen(Screen parent) {
        super(Text.literal("ItemShuffle Options"));
        this.parent = parent;
        gameTypeIndex = GameType.getIndex(settings.gameType);
        itemTypeIndex = ItemGenType.getIndex(settings.itemType);
        teamDataIndex = TeamData.Show.getIndex(settings.teamShowType);
    }

    protected void init() {

        changes = false;

        SliderWidget gameDurationWidget = new ItemShuffleSliderWidget(
                this.width / 2 - 160,
                50,
                150,
                20,
                "Round Duration",
                (settings.time-1200) / 72000d, (slider, title1, value) -> Text.literal("Round Duration: " + ((int) floor(value * 60+1)) + "m"),
                value -> {
                    settings.time = (int) floor((value * 60)) * 1200 +1200;
                    changes = true;
                }
        );
        this.addDrawableChild(gameDurationWidget);

        ButtonWidget removeItemsWidget = ButtonWidget.builder(
                Text.literal("Remove Items: " + (settings.removeItems ? "On" : "Off")),
                button -> {
                    settings.removeItems = !settings.removeItems;
                    button.setMessage(Text.literal("Remove Items: " + (settings.removeItems ? "On" : "Off")));
                    changes = true;
                }).dimensions(
                this.width / 2 + 10,
                150,
                150,
                20
                ).build();
        removeItemsWidget.active = !settings.blockMode;
        this.addDrawableChild(removeItemsWidget);

        ButtonWidget itemTypeWidget = ButtonWidget.builder(
                Text.literal("Item Generator: " + settings.itemType.toString()),
                button -> {
                    itemTypeIndex++;
                    if (itemTypeIndex >= ItemGenType.values().length) {
                        itemTypeIndex = 0;
                    }
                    settings.itemType = ItemGenType.values()[itemTypeIndex];
                    button.setMessage(Text.literal("Item Generator: " + settings.itemType.toString()));
                    changes = true;
                }).dimensions(
                this.width / 2 + 10,
                75,
                150,
                20
        ).build();
        itemTypeWidget.active = settings.gameType != GameType.TWITCH;
        this.addDrawableChild(itemTypeWidget);


        ButtonWidget integrationSettings = ButtonWidget.builder(
                Text.literal("Twitch Settings"),
                button -> client.setScreen(new TwitchConfigurationScreen(this))
        ).dimensions(this.width / 2 - 85, 175, 170, 20).build();
        integrationSettings.active = settings.gameType == GameType.TWITCH;
        this.addDrawableChild(integrationSettings);


        ButtonWidget teamDataShowWidget = ButtonWidget.builder(
                Text.literal("Show Team Data: " + settings.teamShowType.toString()),
                button -> {
                    teamDataIndex++;
                    if (teamDataIndex >= TeamData.Show.values().length) {
                        teamDataIndex = 0;
                    }
                    settings.teamShowType = TeamData.Show.values()[teamDataIndex];
                    button.setMessage(Text.literal("Show Team Data: " + settings.teamShowType.toString()));
                    changes = true;
                }
        ).dimensions(
                this.width / 2 - 160,
                150,
                150,
                20
        ).build();
        teamDataShowWidget.active = settings.gameType == GameType.TEAM;
        this.addDrawableChild(teamDataShowWidget);

        ButtonWidget blockModeWidget = ButtonWidget.builder(
                Text.literal("Standing mode: " + (settings.blockMode ? "On" : "Off")),
                button -> {
                    settings.blockMode = !settings.blockMode;
                    button.setMessage(Text.literal("Standing mode: " + (settings.blockMode ? "On" : "Off")));
                    removeItemsWidget.active = !settings.blockMode;
                    changes = true;
                }
        ).dimensions(
                this.width / 2 + 10,
                50,
                150,
                20
        ).build();
        this.addDrawableChild(blockModeWidget);

        ButtonWidget gameTypeWidget = ButtonWidget.builder(
                Text.literal("Game Type: " + settings.gameType.toString()),
                button -> {
                    gameTypeIndex++;
                    if (gameTypeIndex >= GameType.values().length) {
                        gameTypeIndex = 0;
                    }
                    settings.gameType = GameType.values()[gameTypeIndex];
                    button.setMessage(Text.literal("Game Type: " + settings.gameType.toString()));
                    changes = true;
                    if (settings.gameType == GameType.TWITCH) {
                        itemTypeWidget.active = false;
                        integrationSettings.active = true;
                    } else {
                        itemTypeWidget.active = true;
                        integrationSettings.active = false;
                    }
                    if (settings.gameType == GameType.TEAM) {
                        teamDataShowWidget.active = true;
                    } else {
                        teamDataShowWidget.active = false;
                    }
                }).dimensions(
                this.width / 2 - 160,
                75,
                150,
                20
                ).build();
        this.addDrawableChild(gameTypeWidget);

        ButtonWidget showTimersWidget = ButtonWidget.builder(
                Text.literal("Show Timer: " + (settings.showTimers ? "On" : "Off")),
                button -> {
                    settings.showTimers = !settings.showTimers;
                    button.setMessage(Text.literal("Show Timer: " + (settings.showTimers ? "On" : "Off")));
                    changes = true;
                }
                ).dimensions(
                this.width / 2 - 160,
                100,
                150,
                20
                ).build();
        this.addDrawableChild(showTimersWidget);

        ButtonWidget pauseOnFailWidget = ButtonWidget.builder(Text.literal("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")),
                button -> {
                    settings.pauseOnFail = !settings.pauseOnFail;
                    button.setMessage(Text.literal("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")));
                    changes = true;
                }).dimensions(
                this.width / 2 + 10,
                100,
                150,
                20
                ).build();
        this.addDrawableChild(pauseOnFailWidget);

        ButtonWidget giveFoodWidget = ButtonWidget.builder(
                Text.literal("Give Food: " + (settings.giveFood ? "On" : "Off")),
                button -> {
                    settings.giveFood = !settings.giveFood;
                    button.setMessage(Text.literal("Give Food: " + (settings.giveFood ? "On" : "Off")));
                    changes = true;
                }
        ).dimensions(
                this.width / 2 - 160,
                125,
                150,
                20
        ).build();
        this.addDrawableChild(giveFoodWidget);

        ButtonWidget showItemWidget = ButtonWidget.builder(
                Text.literal("Show Items: " + (settings.showItems ? "On" : "Off")),
                button -> {
                    settings.showItems = !settings.showItems;
                    button.setMessage(Text.literal("Show Items: " + (settings.showItems ? "On" : "Off")));
                    changes = true;
                }
        ).dimensions(
                this.width / 2 + 10,
                125,
                150,
                20
        ).build();
        this.addDrawableChild(showItemWidget);

        ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, button -> onDone()).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build();
        this.addDrawableChild(done);

    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);


        drawTextWithShadow(matrices, this.textRenderer, "ItemShuffle settings", this.width / 2 - textRenderer.getWidth("ItemShuffle settings")/2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
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
        this.client.setScreen(this.parent);
    }


}
