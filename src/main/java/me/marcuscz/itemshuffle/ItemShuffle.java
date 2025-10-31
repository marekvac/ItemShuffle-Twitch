package me.marcuscz.itemshuffle;

import com.google.gson.Gson;
import me.marcuscz.itemshuffle.game.*;
import me.marcuscz.itemshuffle.networking.GenericPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ItemShuffle implements ModInitializer {

    public static String MC_VERSION = "1.21";
    public static final HashMap<ItemGenDimension, String> ITEM_GEN_DIMENSION_TYPES = new HashMap<>() {{
        put(ItemGenDimension.OFF, "");
        put(ItemGenDimension.NETHER, "nether");
    }};
    private static ItemShuffle instance;
    private static final Logger logger = LogManager.getLogger();
    private MinecraftServer server;
    private GameManager gameManager;
    private GameSettings settings;

    @Override
    public void onInitialize() {
        instance = this;
        ItemShuffleCommandManager commandManager = new ItemShuffleCommandManager();
        commandManager.register();
        loadSettings();
        gameManager = new GameManager();

        PayloadTypeRegistry.playS2C().register(NetworkingConstants.ITEM_MESSAGES_ID, GenericPayload.CODEC);
//        PayloadTypeRegistry.playS2C().register(NetworkingConstants.ITEM_MESSAGES_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.TIMER_SHOW_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.TIMER_HIDE_ID, GenericPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(NetworkingConstants.SHOW_ITEM_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.HIDE_ITEM_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.COMPLETE_ITEM_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.TEAM_DATA_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.OTHER_ITEMS_ID, GenericPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(NetworkingConstants.INIT_BOARD_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.BOARD_SET_STATUS_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.HIDE_BOARD_ID, GenericPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(NetworkingConstants.SETTING_SYNC_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.VOTING_INIT_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.VOTING_GET_WINNER_ID, GenericPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(NetworkingConstants.GAME_PAUSED_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.GAME_RESUMED_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.NEXT_ROUND_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingConstants.GAME_STOP_ID, GenericPayload.CODEC);

        // CLIENT TO SERVER
        PayloadTypeRegistry.playC2S().register(NetworkingConstants.SETTING_SYNC_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkingConstants.VOTING_CONFIRMATION_ID, GenericPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkingConstants.VOTING_SEND_WINNER_ID, GenericPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);

        ServerTickEvents.START_SERVER_TICK.register(gameManager::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            getLogger().info("SENDING SETTINGS TO PLAYER");
            ServerPlayNetworking.send(handler.getPlayer(), new GenericPayload(NetworkingConstants.SETTING_SYNC_ID, settings.toPacket()));
            if (!GameManager.isActive() || GameManager.isPaused()) {
                ItemShufflePlayer.hideTimerPlayer(handler.getPlayer());
                ItemShufflePlayer.hideItemPlayer(handler.getPlayer());
                // If player has active voting, stop it
                ItemShufflePlayer.sendGameStopped(handler.getPlayer());
                return;
            }
            UUID uuid = handler.getPlayer().getUuid();
            if (PlayerManager.teamMode() && GameManager.isActive() && !GameManager.isPaused()) {
                gameManager.getPlayerManager().refreshTeamData(true);
            }
            if (gameManager.getPlayerManager().isGamePlayer(uuid)) {
                ItemShufflePlayer player = gameManager.getPlayerManager().getPlayer(uuid);
                player.setPlayer(handler.getPlayer());
                if (!player.isCompleted()) {
                    player.sendItem();
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server1) -> {
            if (server1.getPlayerManager().getCurrentPlayerCount() <= 1) {
                gameManager.stop();
            }
        });

        // Receive Game Settings from client
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.SETTING_SYNC_ID, (payload, ctx) -> {
            System.out.println("Received settings");
            settings = new GameSettings(payload.buf());
            saveSettings();
            ctx.server().getPlayerManager().getPlayerList().forEach(player1 -> ServerPlayNetworking.send(player1, payload));
            broadcast("§bItemShuffle settings updated!");
        });

        // Receive voting client startup confirmation
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VOTING_CONFIRMATION_ID, (payload, ctx) -> {
            var buf = payload.buf();
            var player = ctx.player();
            if (buf.readBoolean() && gameManager.getPlayerManager().isGamePlayer(player.getUuid())) {
                gameManager.getPlayerManager().getPlayer(player.getUuid()).setTwitchEnabled(true);
            }
        });

        // Receive voting item winner from the voting client
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VOTING_SEND_WINNER_ID, (payload, ctx) -> {
            var buf = payload.buf();
            var player = ctx.player();
            Identifier itemId = buf.readIdentifier();
            if (itemId != null) {
                ItemStack itemStack = new ItemStack(Registries.ITEM.get(itemId));
                gameManager.getItemManager().setQueueItem(player.getUuid(), itemStack.getItem());
            }
        });

        // Auto give food feature
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!GameManager.isActive()) {
                return;
            }
            if (gameManager.getPlayerManager().isGamePlayer(newPlayer.getUuid())) {
                gameManager.getPlayerManager().getPlayer(newPlayer.getUuid()).setPlayer(newPlayer);
                if (settings.giveFood) {
                    gameManager.getPlayerManager().getPlayer(newPlayer.getUuid()).giveFood();
                }
            }
        });

        getLogger().info("Initialized!");
    }

    public void onServerStart(MinecraftServer server) {
        this.server = server;
        gameManager.initPlayerManager();
    }

    public static ItemShuffle getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void logDebug(String message) {
        if (instance.settings.debug) {
            getLogger().info("[ItmSf DEBUG]: " + message);
        }
    }

    public MinecraftServer getServer() {
        return server;
    }

    public void broadcast(Text message) {
        server.getPlayerManager().broadcast(message, false);
//        server.getPlayerManager().broadcastChatMessage(message, MessageType.CHAT, Util.NIL_UUID);
    }

    public void broadcast(String message) {
        broadcast(Text.literal(message));
    }

    public void broadcast(String message, boolean actionBar) {
        if (!actionBar) {
            broadcast(message);
            return;
        }
        server.getPlayerManager().getPlayerList().forEach(player -> player.sendMessage(Text.literal(message), true));
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public void loadSettings() {
        File file = new File("./config/itemshuffle/itemshuffle.json");
        Gson gson = new Gson();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                settings = gson.fromJson(fileReader, GameSettings.class);
                fileReader.close();
            } catch (IOException e) {
                logger.warn("Could not load entropy settings: " + e.getLocalizedMessage());
            }
        } else {
            settings = new GameSettings();
            saveSettings();
        }
    }

    public void saveSettings() {
        Gson gson = new Gson();
        File file = new File("./config/itemshuffle/itemshuffle.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(settings));
            fileWriter.close();
        } catch (IOException e) {
            logger.warn("Could not save entropy settings: " + e.getLocalizedMessage());
        }
    }

    public static File getPhasesFile() {
        return new File("./config/itemshuffle/phases-" + ItemShuffle.MC_VERSION + ".json");
    }

    public static File getPhasesFile(String type) {
        if (type == null || type.isEmpty()) {
            return getPhasesFile();
        }
        return new File("./config/itemshuffle/phases-" + ItemShuffle.MC_VERSION + type + ".json");
    }
}
