package me.marcuscz.itemshuffle.client;

import com.google.gson.Gson;
import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.client.voting.TwitchSettings;
import me.marcuscz.itemshuffle.client.voting.VotingClient;
import me.marcuscz.itemshuffle.client.voting.VotingItem;
import me.marcuscz.itemshuffle.game.GameSettings;
import me.marcuscz.itemshuffle.networking.GenericPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    public ItemStack lastItem;
    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {

        instance = this;
        loadSettings();

        ClientTickEvents.START_CLIENT_TICK.register(hudRender::tick);
        HudElementRegistry.addLast(Identifier.of("itemshuffle", "hud"), hudRender::renderItemShuffleHUD);

        ClientPlayNetworking.registerGlobalReceiver(ITEM_MESSAGES_ID, (payload, ctx) -> {
            var client = ctx.client();
            lastItem  = new ItemStack(Registries.ITEM.get(payload.buf().readIdentifier()));
            hudRender.setItem(lastItem);
            hudRender.setItemCompleted(false);
            String key = lastItem.getItem().getTranslationKey();
            String lang = TranslationStorage.getInstance().get(key);
            String message = ItemShuffle.getInstance().getSettings().blockMode ? "You must stand on" : "Your material";
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a" + message + ": §6" + lang), false);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TIMER_SHOW_ID, (payload, ctx) -> {
            PacketByteBuf buf = payload.buf();
            hudRender.setCurrentTime(buf.readInt());
            hudRender.setTime(buf.readInt());
            hudRender.setColor(buf.readInt());
            hudRender.showTimer(true);
        });

        ClientPlayNetworking.registerGlobalReceiver(TIMER_HIDE_ID, (payload, ctx) -> hudRender.showTimer(false));

        ClientPlayNetworking.registerGlobalReceiver(SHOW_ITEM_ID, (payload, ctx) -> hudRender.showItem(true));
        ClientPlayNetworking.registerGlobalReceiver(HIDE_ITEM_ID, (payload, ctx) -> hudRender.showItem(false));
        ClientPlayNetworking.registerGlobalReceiver(COMPLETE_ITEM_ID, (payload, ctx) -> hudRender.setItemCompleted(true));
        ClientPlayNetworking.registerGlobalReceiver(TEAM_DATA_ID, ((payload, ctx) -> hudRender.setTeamData(payload.buf())));
        ClientPlayNetworking.registerGlobalReceiver(OTHER_ITEMS_ID, ((payload, ctx) -> hudRender.setOtherItems(payload.buf())));

        ClientPlayNetworking.registerGlobalReceiver(INIT_BOARD_ID, (payload, ctx) -> hudRender.initBoard(payload.buf()));
        ClientPlayNetworking.registerGlobalReceiver(BOARD_SET_STATUS_ID, (payload, ctx) -> hudRender.setBoardStatus(payload.buf()));
        ClientPlayNetworking.registerGlobalReceiver(HIDE_BOARD_ID, (payload, ctx) -> hudRender.hideBoard());

        ClientPlayNetworking.registerGlobalReceiver(SETTING_SYNC_ID, (payload, ctx) -> {
            ItemShuffle.getInstance().setSettings(new GameSettings(payload.buf()));
            ItemShuffle.getLogger().info("Client settings updated");
        });

        ClientPlayNetworking.registerGlobalReceiver(VOTING_INIT_ID, (payload, ctx) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            GenericPayload payload1 = new GenericPayload(VOTING_CONFIRMATION_ID, buf);
            var responseSender = ctx.responseSender();
            if (twitchSettings.twitchEnabled) {
                try {
                    votingClient = new VotingClient();
                    votingEnabled = true;
                    hudRender.showVotes(true);
                } catch (IOException | ParseException e) {
                    ItemShuffle.getLogger().error("Failed to start voting client");
                    e.printStackTrace();
                    buf.writeBoolean(false);
                    responseSender.sendPacket(payload1);
                    return;
                }
            }
            buf.writeBoolean(true);
            responseSender.sendPacket(payload1);
        });

        ClientPlayNetworking.registerGlobalReceiver(VOTING_GET_WINNER_ID, (payload, ctx) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            var responseSender = ctx.responseSender();
            if (votingEnabled) {
                VotingItem item = votingClient.getWinner();
                if (item != null) {
                    buf.writeIdentifier(Registries.ITEM.getId(item.getItem()));
                    responseSender.sendPacket(new GenericPayload(VOTING_SEND_WINNER_ID, buf));
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GAME_PAUSED_ID, ((payload, ctx) -> {
            votingClient.pauseVoting();
            hudRender.showVotes(false);
        }));

        ClientPlayNetworking.registerGlobalReceiver(GAME_RESUMED_ID, ((payload, ctx) -> {
            votingClient.resumeVoting();
            hudRender.showVotes(true);
        }));

        ClientPlayNetworking.registerGlobalReceiver(NEXT_ROUND_ID, (payload, ctx) -> {
            if (votingEnabled) {
                PacketByteBuf buf = payload.buf();
                int[] ids = buf.readIntArray();
//                votingClient.getItemManager().nextRound(1);
                votingClient.nextVote(ids);
                hudRender.showVotes(true);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GAME_STOP_ID, (payload, ctx) -> {
            if (votingEnabled) {
                votingEnabled = false;
                votingClient.stop();
            }
            hudRender.setShowTeamData(false);
        });

    }

    public static ItemShuffleClient getInstance() {
        return instance;
    }

    public void saveGameSettings() {
        if (MinecraftClient.getInstance().getNetworkHandler() != null && !MinecraftClient.getInstance().getNetworkHandler().getConnection().isLocal()) {
            ClientPlayNetworking.send(new GenericPayload(SETTING_SYNC_ID, ItemShuffle.getInstance().getSettings().toPacket()));
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
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message), false);
    }
}
