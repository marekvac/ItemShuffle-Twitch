package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager {

    private final MinecraftServer server;
    private final Map<UUID, ItemShufflePlayer> players;
    private final Map<String, ItemShuffleTeam> teams;

    public PlayerManager() {
        this.players = new HashMap<>();
        server = ItemShuffle.getInstance().getServer();
        this.teams = new HashMap<>();
    }

    public static boolean teamMode() {
        return ItemShuffle.getInstance().getSettings().gameType == GameType.TEAM;
    }

    public void refreshPlayers() {
        if (teamMode()) {
            players.clear();
            teams.values().forEach(t -> players.putAll(t.getPlayers()));
        } else {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                UUID uuid = player.getUuid();
                if (!players.containsKey(uuid)) {
                    players.put(uuid, new ItemShufflePlayer(player));
                }
            });
        }
    }

    public Map<String, ItemShuffleTeam> getTeams() {
        return teams;
    }

    public void createTeam(String name) throws Exception {
        if (teams.containsKey(name)) throw new Exception("Team name already used!");
        ItemShuffleTeam team = new ItemShuffleTeam(name);
        teams.put(name, team);
    }

    public void addPlayerToTeam(String teamName, ServerPlayerEntity player) throws Exception {
        if (!teams.containsKey(teamName)) throw new Exception("Invalid team!");
        ItemShuffleTeam team = teams.get(teamName);

        AtomicBoolean inTeam = new AtomicBoolean(false);
        teams.forEach((s, t) -> {
            if (t.hasPlayer(player.getUuid())) inTeam.set(true);
        });

        if (inTeam.get()) throw new Exception("Player is already in some team!");

        team.addPlayer(new ItemShufflePlayer(player));
    }

    public void removePlayerFromTeam(String teamName, ServerPlayerEntity player) throws Exception  {
        if (!teams.containsKey(teamName)) throw new Exception("Invalid team!");
        ItemShuffleTeam team = teams.get(teamName);

        if (!team.hasPlayer(player.getUuid())) throw new Exception("Player is not in this team!");

        team.removePlayer(player.getUuid());
    }

    public void removeTeam(String teamName) throws Exception {
        if (!teams.containsKey(teamName)) throw new Exception("Invalid team!");
        teams.remove(teamName);
    }

    public void setPlayerItemQueues(Queue<Item> items) {
        players.values().forEach(p -> p.setItemQueue(items));
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

    public boolean allCompleted() {
        AtomicBoolean allCompleted = new AtomicBoolean(true);

        if (teamMode()) {
            teams.values().forEach(t -> {
                if (t.failed()) {
                    allCompleted.set(false);
                }
            });
        } else {
            players.values().forEach(player -> {
                if (player.failed()) {
                    allCompleted.set(false);
                }
            });
        }
        return allCompleted.get();
    }

    public boolean allFailed() {
        AtomicBoolean allFailed = new AtomicBoolean(true);

        if (teamMode()) {
            teams.values().forEach(t -> {
                if (t.isCompleted()) {
                    allFailed.set(false);
                }
            });
        } else {
            players.values().forEach(player -> {
                if (player.isCompleted()) {
                    allFailed.set(false);
                }
            });
        }
        return allFailed.get();
    }

    public void sendItems() {
        players.values().forEach(ItemShufflePlayer::sendItem);
    }

    public void broadcastScore(boolean onlyFailed) {
        if (teamMode()) {
            teams.values().forEach(t -> t.broadcastScore(onlyFailed));
        } else {
            players.values().forEach(player -> player.broadcastScore(onlyFailed));
        }
    }

    public void broadcastRunScore() {
        players.values().forEach(ItemShufflePlayer::broadcastRunScore);
    }

    public void checkAllPlayersItem() {
        if (teamMode()) {
            teams.values().forEach(ItemShuffleTeam::checkItem);
        } else {
            players.values().forEach(ItemShufflePlayer::checkItem);
        }
    }

    public boolean isEveryoneCompleted() {
        AtomicBoolean allCompleted = new AtomicBoolean(true);

        if (teamMode()) {
            teams.values().forEach(t -> {
                if (!t.isCompleted()) {
                    allCompleted.set(false);
                }
            });
        } else {
            players.values().forEach(player -> {
                if (!player.isCompleted()) {
                    allCompleted.set(false);
                }
            });
        }
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
