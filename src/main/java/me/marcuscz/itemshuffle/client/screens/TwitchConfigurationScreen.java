package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.voting.TwitchSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.*;

public class TwitchConfigurationScreen extends Screen {

    private final TwitchSettings twitchSettings;

    TextFieldWidget twitchToken;
    TextFieldWidget twitchChannel;
    Text tokenTranslatable = Text.literal("Twitch OAuth2 Token");
    Text channelTranslatable = Text.literal("Twitch channel name");
    ButtonWidget twitchEnabled;

    ButtonWidget done;

    private final Screen parent;

    protected TwitchConfigurationScreen(Screen parent) {
        super(Text.literal("Twitch settings"));
        this.parent = parent;
        twitchSettings = ItemShuffleClient.getInstance().getTwitchSettings();
    }

    protected void init() {
        twitchEnabled = ButtonWidget.builder(
                Text.literal("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")),
                button -> {
                    twitchSettings.twitchEnabled = !twitchSettings.twitchEnabled;
                    button.setMessage(Text.literal("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")));
                }
        ).dimensions(
                this.width/2-100,
                30,
                200,
                20
        ).build();
        this.addDrawableChild(twitchEnabled);

        twitchToken = new TextFieldWidget(this.textRenderer, this.width / 2 + 10, 80, 125, 20, Text.translatable("entropy.options.integrations.twitch.OAuthToken"));
        twitchToken.setMaxLength(64);
        twitchToken.setText(twitchSettings.authToken);
        twitchToken.setRenderTextProvider((s, integer) -> OrderedText.styledForwardsVisitedString("*".repeat(s.length()), Style.EMPTY));
        this.addDrawableChild(twitchToken);
        twitchChannel = new TextFieldWidget(this.textRenderer, this.width / 2 + 10, 110, 125, 20, Text.translatable("entropy.options.integrations.twitch.channelName"));
        twitchChannel.setText(twitchSettings.channel);
        this.addDrawableChild(twitchChannel);

        this.done = ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> onDone()
        ).dimensions(
                this.width / 2 - 100,
                this.height - 30,
                200,
                20
        ).build();
        this.addDrawableChild(done);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(this.textRenderer, tokenTranslatable, this.width / 2 - 10 - textRenderer.getWidth(tokenTranslatable), 86, 16777215);
        context.drawTextWithShadow(this.textRenderer, channelTranslatable, this.width / 2 - 10 - textRenderer.getWidth(channelTranslatable), 116, 16777215);

        super.render(context, mouseX, mouseY, delta);
    }

    private void onDone() {
        twitchSettings.authToken = twitchToken.getText();
        twitchSettings.channel = twitchChannel.getText();

        ItemShuffleClient.getInstance().saveSettings();
        onClose();
    }


    public void onClose() {
        this.client.setScreen(this.parent);
    }

}
