package me.marcuscz.itemshuffle;

import com.google.gson.Gson;
import me.marcuscz.itemshuffle.game.GameManager;
import me.marcuscz.itemshuffle.game.GameSettings;
import me.marcuscz.itemshuffle.game.ItemShufflePlayer;
import me.marcuscz.itemshuffle.game.PlayerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class ItemShuffle implements ModInitializer {

    public static String MC_VERSION = "1.19";
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

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);

        ServerTickEvents.START_SERVER_TICK.register(gameManager::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            ServerPlayNetworking.send(handler.getPlayer(), NetworkingConstants.SETTING_SYNC, settings.toPacket());
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
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.SETTING_SYNC, (server1, player, handler, buf, responseSender) -> {
            System.out.println("Received settings");
            settings = new GameSettings(buf);
            saveSettings();
            server1.getPlayerManager().getPlayerList().forEach(player1 -> ServerPlayNetworking.send(player1, NetworkingConstants.SETTING_SYNC, settings.toPacket()));
            broadcast("§bItemShuffle settings updated!");
        });

        // Receive voting client startup confirmation
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VOTING_CONFIRMATION, (server1, player, handler, buf, responseSender) -> {
            if (buf.readBoolean() && gameManager.getPlayerManager().isGamePlayer(player.getUuid())) {
                gameManager.getPlayerManager().getPlayer(player.getUuid()).setTwitchEnabled(true);
            }
        });

        // Receive voting item winner from the voting client
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VOTING_SEND_WINNER, (server1, player, handler, buf, responseSender) -> {
            ItemStack itemStack = buf.readItemStack();
            if (itemStack != null) {
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
}
