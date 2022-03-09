package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager {

    private final MinecraftServer server;
    private final Map<UUID, ItemShufflePlayer> players;

    public PlayerManager() {
        this.players = new HashMap<>();
        server = ItemShuffle.getInstance().getServer();
    }

    public void refreshPlayers() {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            if (!players.containsKey(uuid)) {
                players.put(uuid, new ItemShufflePlayer(player));
            }
        });
    }

    public Map<UUID, ItemShufflePlayer> getPlayers() {
        return players;
    }

    public ItemShufflePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public boolean isGamePlayer(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean someoneFailed() {
        AtomicBoolean allCompleted = new AtomicBoolean(true);
        players.values().forEach(player -> {
            if (player.failed()) {
                allCompleted.set(false);
            }
        });
        return allCompleted.get();
    }

    public void sendItems() {
        players.values().forEach(ItemShufflePlayer::sendItem);
    }

    public void broadcastScore(boolean onlyFailed) {
        players.values().forEach(player -> player.broadcastScore(onlyFailed));
    }

    public void checkAllPlayersItem() {
        players.values().forEach(ItemShufflePlayer::checkItem);
    }

    public boolean isEveryoneCompleted() {
        AtomicBoolean allCompleted = new AtomicBoolean(true);
        players.values().forEach(player -> {
            if (!player.isCompleted()) {
                allCompleted.set(false);
            }
        });
        return allCompleted.get();
    }

    public void giveFoods() {
        players.values().forEach(ItemShufflePlayer::giveFood);
    }

    public void updateTimers(int color) {
        if (ItemShuffle.getInstance().getSettings().showTimers) {
            players.values().forEach(player -> player.updateTimer(color));
        }
    }

    public void updateTimers() {
        int color = ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH ? MathHelper.packRgb(100,65,165) : MathHelper.packRgb(70, 150, 70);
        this.updateTimers(color);
    }

    public void hideTimers() {
        players.values().forEach(ItemShufflePlayer::hideTimer);
    }

    public void clearPlayers() {
        players.clear();
    }

    public void startVotingClients() {
        players.values().forEach(ItemShufflePlayer::startVotingClient);
    }

    public void askVotingClientsForWinners() {
        players.values().forEach(ItemShufflePlayer::askClientForWinner);
    }

    public void createNewVotes(ItemManager itemManager) {
        players.values().forEach(player -> {
            List<Item> items = itemManager.getVotingItems(4);
            player.createNewVoting(items);
        });
    }

    public void pauseVotingClients() {
        players.values().forEach(ItemShufflePlayer::pauseVotingClient);
    }

    public void resumeVotingClients() {
        players.values().forEach(ItemShufflePlayer::resumeVotingClient);
    }

    public void stopVotingClients() {
        players.values().forEach(ItemShufflePlayer::stopVotingClient);
    }

    public void showItems() {
        players.values().forEach(ItemShufflePlayer::showItem);
    }

    public void hideItems() {
        players.values().forEach(ItemShufflePlayer::hideItem);
    }
}
