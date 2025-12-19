package me.marcuscz.itemshuffle.client.screens;

import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import me.marcuscz.itemshuffle.client.voting.TwitchSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class TwitchConfigurationScreen extends Screen {

    private final TwitchSettings twitchSettings;

    EditBox twitchToken;
    EditBox twitchChannel;
    Component tokenTranslatable = Component.literal("Twitch OAuth2 Token");
    Component channelTranslatable = Component.literal("Twitch channel name");
    Button twitchEnabled;

    Button done;

    private final Screen parent;

    protected TwitchConfigurationScreen(Screen parent) {
        super(Component.literal("Twitch settings"));
        this.parent = parent;
        twitchSettings = ItemShuffleClient.getInstance().getTwitchSettings();
    }

    protected void init() {
        twitchEnabled = Button.builder(
                Component.literal("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")),
                button -> {
                    twitchSettings.twitchEnabled = !twitchSettings.twitchEnabled;
                    button.setMessage(Component.literal("Twitch Integration: " + (twitchSettings.twitchEnabled ? "Enabled" : "Disabled")));
                }
        ).bounds(
                this.width/2-100,
                30,
                200,
                20
        ).build();
        this.addRenderableWidget(twitchEnabled);

        twitchToken = new EditBox(this.font,  this.width / 2 - 125, 80, 250, 20, Component.literal("Twitch OAuth2 Token"));
        twitchToken.setMaxLength(64);
        twitchToken.setValue(twitchSettings.authToken);
        twitchToken.addFormatter((string, firstCharacterIndex) -> FormattedCharSequence.forward("*".repeat(string.length()), Style.EMPTY));
        this.addRenderableWidget(twitchToken);
        twitchChannel = new EditBox(this.font, this.width / 2 - 125, 110, 250, 20, Component.literal("Twitch channel name"));
        twitchChannel.setValue(twitchSettings.channel);
        this.addRenderableWidget(twitchChannel);

        this.done = Button.builder(
                CommonComponents.GUI_DONE,
                button -> onDone()
        ).bounds(
                this.width / 2 - 100,
                this.height - 30,
                200,
                20
        ).build();
        this.addRenderableWidget(done);
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
//        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawString(this.font, tokenTranslatable, this.width / 2 - 135 - font.width(tokenTranslatable), 86, 16777215);
        context.drawString(this.font, channelTranslatable, this.width / 2 - 135 - font.width(channelTranslatable), 116, 16777215);

        super.render(context, mouseX, mouseY, delta);
    }

    private void onDone() {
        twitchSettings.authToken = twitchToken.getValue();
        twitchSettings.channel = twitchChannel.getValue();

        ItemShuffleClient.getInstance().saveSettings();
        onClose();
    }


    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

}
