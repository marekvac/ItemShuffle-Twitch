package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.voting.TwitchSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;

public class TwitchConfigurationScreen extends Screen {

    private final TwitchSettings twitchSettings;

    TextFieldWidget twitchToken;
    TextFieldWidget twitchChannel;
    Text tokenTranslatable = new LiteralText("Twitch OAuth2 Token");
    Text channelTranslatable = new LiteralText("Twitch channel name");
    ButtonWidget twitchEnabled;

    ButtonWidget done;

    private final Screen parent;

    protected TwitchConfigurationScreen(Screen parent) {
        super(new LiteralText("Twitch settings"));
        this.parent = parent;
        twitchSettings = ItemShuffleClient.getInstance().getTwitchSettings();
    }

    protected void init() {
        twitchEnabled = new ButtonWidget(this.width/2-100,30,200,20, new LiteralText("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")), button -> {
            twitchSettings.twitchEnabled = !twitchSettings.twitchEnabled;
            button.setMessage(new LiteralText("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")));
        });
        this.addDrawableChild(twitchEnabled);

        twitchToken = new TextFieldWidget(this.textRenderer, this.width / 2 + 10, 80, 125, 20, new TranslatableText("entropy.options.integrations.twitch.OAuthToken"));
        twitchToken.setMaxLength(64);
        twitchToken.setText(twitchSettings.authToken);
        twitchToken.setRenderTextProvider((s, integer) -> OrderedText.styledForwardsVisitedString("*".repeat(s.length()), Style.EMPTY));
        this.addDrawableChild(twitchToken);
        twitchChannel = new TextFieldWidget(this.textRenderer, this.width / 2 + 10, 110, 125, 20, new TranslatableText("entropy.options.integrations.twitch.channelName"));
        twitchChannel.setText(twitchSettings.channel);
        this.addDrawableChild(twitchChannel);

        this.done = new ButtonWidget(this.width / 2 - 100, this.height - 30, 200, 20, ScreenTexts.DONE, button -> onDone());
        this.addDrawableChild(done);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        drawTextWithShadow(matrices, this.textRenderer, tokenTranslatable, this.width / 2 - 10 - textRenderer.getWidth(tokenTranslatable), 86, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, channelTranslatable, this.width / 2 - 10 - textRenderer.getWidth(channelTranslatable), 116, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void onDone() {
        twitchSettings.authToken = twitchToken.getText();
        twitchSettings.channel = twitchChannel.getText();

        ItemShuffleClient.getInstance().saveSettings();
        onClose();
    }

    @Override
    public void onClose() {
        this.client.setScreen(this.parent);
    }

}
