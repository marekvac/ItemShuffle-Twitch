package me.marcuscz.itemshuffle.client;

import com.google.gson.Gson;
import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.client.voting.TwitchSettings;
import me.marcuscz.itemshuffle.client.voting.VotingClient;
import me.marcuscz.itemshuffle.client.voting.VotingItem;
import me.marcuscz.itemshuffle.game.GameSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static me.marcuscz.itemshuffle.NetworkingConstants.*;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class ItemShuffleClient implements ClientModInitializer {

    private static ItemShuffleClient instance;
    private final HudRender hudRender = new HudRender();
    private TwitchSettings twitchSettings;
    private VotingClient votingClient;
    public boolean votingEnabled = false;
    public Item lastItem;

    @Override
    public void onInitializeClient() {

        instance = this;
        loadSettings();

        ClientTickEvents.START_CLIENT_TICK.register(hudRender::tick);
        HudRenderCallback.EVENT.register(hudRender::renderTimer);

        ClientPlayNetworking.registerGlobalReceiver(ITEM_MESSAGES, (client, handler, buf, responseSender) -> {
//            String key = buf.readString();
            ItemStack itemStack = buf.readItemStack();
            lastItem = itemStack.getItem();
            hudRender.setItem(lastItem);
            String key = lastItem.getTranslationKey();
            String lang = TranslationStorage.getInstance().get(key);
            if (client.player != null) {
                client.player.sendMessage(new LiteralText("§aYour material: §6" + lang), false);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TIMER_SHOW, (client, handler, buf, responseSender) -> {
            hudRender.setCurrentTime(buf.readInt());
            hudRender.setTime(buf.readInt());
            hudRender.setColor(buf.readInt());
            hudRender.showTimer(true);
        });

        ClientPlayNetworking.registerGlobalReceiver(TIMER_HIDE, (client, handler, buf, responseSender) -> hudRender.showTimer(false));

        ClientPlayNetworking.registerGlobalReceiver(SHOW_ITEM, (client, handler, buf, responseSender) -> hudRender.showItem(true));
        ClientPlayNetworking.registerGlobalReceiver(HIDE_ITEM, (client, handler, buf, responseSender) -> hudRender.showItem(false));

        ClientPlayNetworking.registerGlobalReceiver(SETTING_SYNC, (client, handler, buf, responseSender) -> {
            ItemShuffle.getInstance().setSettings(new GameSettings(buf));
            ItemShuffle.getLogger().info("Client settings updated");
        });

        ClientPlayNetworking.registerGlobalReceiver(VOTING_INIT, (client, handler, buf, responseSender) -> {
            PacketByteBuf response = PacketByteBufs.create();
            if (twitchSettings.twitchEnabled) {
                try {
                    votingClient = new VotingClient();
                    votingEnabled = true;
                    hudRender.showVotes(true);
                } catch (IOException | ParseException e) {
                    ItemShuffle.getLogger().error("Failed to start voting client");
                    e.printStackTrace();
                    response.writeBoolean(false);
                    responseSender.sendPacket(VOTING_CONFIRMATION, response);
                    return;
                }
            }
            response.writeBoolean(true);
            responseSender.sendPacket(VOTING_CONFIRMATION, response);
        });

        ClientPlayNetworking.registerGlobalReceiver(VOTING_GET_WINNER, (client, handler, buf, responseSender) -> {
            PacketByteBuf response = PacketByteBufs.create();
            if (votingEnabled) {
                VotingItem item = votingClient.getWinner();
                if (item != null) {
                    response.writeItemStack(new ItemStack(item.getItem()));
                    responseSender.sendPacket(VOTING_SEND_WINNER, response);
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GAME_PAUSED, ((client, handler, buf, responseSender) -> {
            votingClient.pauseVoting();
            hudRender.showVotes(false);
        }));

        ClientPlayNetworking.registerGlobalReceiver(GAME_RESUMED, ((client, handler, buf, responseSender) -> {
            votingClient.resumeVoting();
            hudRender.showVotes(true);
        }));

        ClientPlayNetworking.registerGlobalReceiver(NEXT_ROUND, (client, handler, buf, responseSender) -> {
            if (votingEnabled) {
                int[] ids = buf.readIntArray();
//                votingClient.getItemManager().nextRound(1);
                votingClient.nextVote(ids);
                hudRender.showVotes(true);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GAME_STOP, (client, handler, buf, responseSender) -> {
            if (votingEnabled) {
                votingEnabled = false;
                votingClient.stop();
            }
        });

    }

    public static ItemShuffleClient getInstance() {
        return instance;
    }

    public void saveGameSettings() {
        if (MinecraftClient.getInstance().getNetworkHandler() != null && !MinecraftClient.getInstance().getNetworkHandler().getConnection().isLocal()) {
            ClientPlayNetworking.send(SETTING_SYNC, ItemShuffle.getInstance().getSettings().toPacket());
        } else {
            ItemShuffle.getInstance().saveSettings();
        }
    }

    public TwitchSettings getTwitchSettings() {
        return twitchSettings;
    }

    public VotingClient getVotingClient() {
        return votingClient;
    }

    public void loadSettings() {
        File file = new File("./config/itemshuffle/twitchSettings.json");
        Gson gson = new Gson();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                twitchSettings = gson.fromJson(fileReader, TwitchSettings.class);
                fileReader.close();
            } catch (IOException e) {
                ItemShuffle.getLogger().warn("Could not load entropy integration settings: " + e.getLocalizedMessage());
            }
        } else {
            twitchSettings = new TwitchSettings();
            saveSettings();
        }
    }

    public void saveSettings() {
        Gson gson = new Gson();
        File file = new File("./config/itemshuffle/twitchSettings.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(twitchSettings));
            fileWriter.close();
        } catch (IOException e) {
            ItemShuffle.getLogger().warn("Could not save entropy integration settings: " + e.getLocalizedMessage());
        }
    }

    public static void sendPlayerMessage(String message) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }
        MinecraftClient.getInstance().player.sendMessage(new LiteralText(message), false);
    }
}
