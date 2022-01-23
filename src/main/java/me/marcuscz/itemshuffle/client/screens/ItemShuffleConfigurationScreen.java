package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.screens.widgets.ItemShuffleSliderWidget;
import me.marcuscz.itemshuffle.game.GameSettings;
import me.marcuscz.itemshuffle.game.GameType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static java.lang.Math.floor;

public class ItemShuffleConfigurationScreen extends Screen {

    private final Screen parent;
    private final GameSettings settings = ItemShuffle.getInstance().getSettings();
    private boolean changes;
    private int gameTypeIndex;

    public ItemShuffleConfigurationScreen(Screen parent) {
        super(new LiteralText("ItemShuffle Options"));
        this.parent = parent;
        gameTypeIndex = GameType.getIndex(settings.gameType);
    }

    protected void init() {

        changes = false;

        SliderWidget gameDurationWidget = new ItemShuffleSliderWidget(
                this.width / 2 - 160,
                50,
                150,
                20,
                "Duration",
                (settings.time-1200) / 12000d, (slider, title1, value) -> new LiteralText("Value: " + ((int) floor(value * 10+1)) + "m"),
                value -> {
                    settings.time = (int) floor((value * 10)) * 1200 +1200;
                    changes = true;
                }
        );
        this.addDrawableChild(gameDurationWidget);

        ButtonWidget removeItemsWidget = new ButtonWidget(
                this.width / 2 + 10,
                50,
                150,
                20,
                new LiteralText("Remove Items: " + (settings.removeItems ? "On" : "Off")),
                button -> {
                    settings.removeItems = !settings.removeItems;
                    button.setMessage(new LiteralText("Remove Items: " + (settings.removeItems ? "On" : "Off")));
                    changes = true;
                }
        );
        this.addDrawableChild(removeItemsWidget);

        ButtonWidget gameTypeWidget = new ButtonWidget(
                this.width / 2 - 160,
                75,
                150,
                20,
                new LiteralText("Game Type: " + settings.gameType.toString()),
                button -> {
                    gameTypeIndex++;
                    if (gameTypeIndex >= GameType.values().length) {
                        gameTypeIndex = 0;
                    }
                    settings.gameType = GameType.values()[gameTypeIndex];
                    button.setMessage(new LiteralText("Game Type: " + settings.gameType.toString()));
                    changes = true;
                }
        );
        this.addDrawableChild(gameTypeWidget);

        ButtonWidget showTimersWidget = new ButtonWidget(
                this.width / 2 + 10,
                75,
                150,
                20,
                new LiteralText("Show Timer: " + (settings.showTimers ? "On" : "Off")),
                button -> {
                    settings.showTimers = !settings.showTimers;
                    button.setMessage(new LiteralText("Show Timer: " + (settings.showTimers ? "On" : "Off")));
                    changes = true;
                }
        );
        this.addDrawableChild(showTimersWidget);

        ButtonWidget pauseOnFailWidget = new ButtonWidget(
                this.width / 2 - 160,
                100,
                150,
                20,
                new LiteralText("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")),
                button -> {
                    settings.pauseOnFail = !settings.pauseOnFail;
                    button.setMessage(new LiteralText("Pause On Fail: " + (settings.pauseOnFail ? "On" : "Off")));
                    changes = true;
                }
        );
        this.addDrawableChild(pauseOnFailWidget);

        ButtonWidget giveFoodWidget = new ButtonWidget(
                this.width / 2 + 10,
                100,
                150,
                20,
                new LiteralText("Give Food: " + (settings.giveFood ? "On" : "Off")),
                button -> {
                    settings.giveFood = !settings.giveFood;
                    button.setMessage(new LiteralText("Give Food: " + (settings.giveFood ? "On" : "Off")));
                    changes = true;
                }
        );
        this.addDrawableChild(giveFoodWidget);

        ButtonWidget integrationSettings = new ButtonWidget(this.width / 2 - 85, 125, 170, 20, new LiteralText("Twitch Settings"), button -> client.setScreen(new TwitchConfigurationScreen(this)));
        if (MinecraftClient.getInstance().getGame().getCurrentSession() != null) {
            integrationSettings.active = false;
        }
        this.addDrawableChild(integrationSettings);

        ButtonWidget done = new ButtonWidget(this.width / 2 - 100, this.height - 30, 200, 20, ScreenTexts.DONE, button -> onDone());
        this.addDrawableChild(done);

    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);


        TranslatableText title = new TranslatableText("ItemShuffle settings");
        drawTextWithShadow(matrices, this.textRenderer, title, this.width / 2 - textRenderer.getWidth(title)/2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void onDone() {
        if (changes) {
            settings.printSettings();
            ItemShuffleClient.getInstance().saveGameSettings();
        }
        onClose();
    }

    @Override
    public void onClose() {
        this.client.setScreen(this.parent);
    }


}
