package me.marcuscz.itemshuffle.client.voting;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.client.ItemShuffleClient;
import net.minecraft.client.MinecraftClient;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PingEvent;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwitchClient extends ListenerAdapter {

    private final Configuration config;
    private PircBotX pircBotX;
    private ExecutorService executorService;
    private final VotingClient votingClient;
    private long lastJoinMessage = 0;

    public TwitchClient(VotingClient votingClient) {
        this.votingClient = votingClient;
        TwitchSettings twitchSettings = ItemShuffleClient.getInstance().getTwitchSettings();
        config = new Configuration.Builder()
                .setAutoNickChange(false)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .setEncoding(StandardCharsets.UTF_8)
                .addServer("irc.chat.twitch.tv", 6697)
                .setSocketFactory(SSLSocketFactory.getDefault())
                .setName(twitchSettings.channel.toLowerCase())
                .setServerPassword(twitchSettings.authToken.startsWith("oauth:") ? twitchSettings.authToken : "oauth:" + twitchSettings.authToken)
                .addAutoJoinChannel("#" + twitchSettings.channel.toLowerCase())
                .addListener(this)
                .setAutoSplitMessage(false)
                .buildConfiguration();
        this.start();
    }

    public void start() {
        pircBotX = new PircBotX(config);
        executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            try {
                pircBotX.startBot();
            } catch (IOException e) {
                ItemShuffle.getLogger().error("IO Exception while starting bot: " + e.getMessage());
//                ItemShuffleClient.sendPlayerMessage("§4Failed to connect to the Twitch Chat!");
//                ItemShuffleClient.sendPlayerMessage("§4IO Exception while starting bot: " + e.getMessage());
                e.printStackTrace();
            } catch (IrcException e) {
                ItemShuffle.getLogger().error("IRC Exception while starting bot: " + e.getMessage());
//                ItemShuffleClient.sendPlayerMessage("§4Failed to connect to the Twitch Chat!");
//                ItemShuffleClient.sendPlayerMessage("§4IRC Exception while starting bot: " + e.getMessage());
                e.printStackTrace();
            }
        });
        votingClient.enableVoting();
    }

    public void stop() {
        pircBotX.stopBotReconnect();
        pircBotX.close();
        executorService.shutdown();
    }

    @Override
    public void onMessage(MessageEvent event) {
        votingClient.processVote(event.getMessage());
    }

    @Override
    public void onJoin(JoinEvent event) {
        long currentTime = System.currentTimeMillis();
        if(currentTime-lastJoinMessage>30000){
            ItemShuffleClient.sendPlayerMessage("§2Connected to the Twitch Chat: §a" + ItemShuffleClient.getInstance().getTwitchSettings().channel);
            sendMessage("/me [ItemShuffle] Connected to the game to user: " + MinecraftClient.getInstance().getName());
            lastJoinMessage=currentTime;
        }
    }

    @Override
    public void onPing(PingEvent event) {
        System.out.println("Received Ping from twitch, answering...");
        pircBotX.sendRaw().rawLineNow(String.format("PONG %s\r\n", event.getPingValue()));
    }

    public void sendMessage(String message) {
        pircBotX.sendIRC().message(ItemShuffleClient.getInstance().getTwitchSettings().channel.toLowerCase(), message);
    }

}
