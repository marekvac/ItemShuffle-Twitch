package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerManager {

    private final MinecraftServer server;
    private final Map<UUID, ItemShufflePlayer> players;
    private final Map<String, ItemShuffleTeam> teams;
    private static final char[] TEAM_COLORS = {'9', '2', 'e', 'c', '5', 'b', 'd', '6'};
    private static final String[] TEAM_NAMES = {"BLUE", "GREEN", "YELLOW", "RED", "PURPLE", "AQUA", "PINK", "GOLD"};

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

    public void createTeam(String name, String color) throws Exception {
        if (teams.size() > 4) throw new Exception("Team size is limited to 4!");
        if (teams.containsKey(name)) throw new Exception("Team name already used!");
        ItemShuffleTeam team = new ItemShuffleTeam(name, color.charAt(0));
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

    public void autoTeams(int size) throws Exception {
        if (size > 4) throw new Exception("Team size is limited to 4!");
        MutableText text = Text.literal("§7Created §b" + size + " §7teams:");
        teams.clear();
        server.getScoreboard().getTeams().clear();
        for (int i = 0; i < size; i++) {
            char c = TEAM_COLORS[i];
            String n = TEAM_NAMES[i];
            Team t = server.getScoreboard().addTeam(n);
            t.setColor(Formatting.byCode(c));
            t.setFriendlyFireAllowed(false);
            ItemShuffleTeam team = new ItemShuffleTeam(n, c);
            teams.put(n, team);
            text.append(Text.literal(" §" + c + "§nJOIN " + n + "§r").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemshuffle teams join " + n))));
        }
        ItemShuffle.getInstance().broadcast(text);
    }

    public void joinPlayer(String teamName, ServerPlayerEntity player) throws Exception {
        if (!teams.containsKey(teamName)) throw new Exception("Invalid team!");
        ItemShuffleTeam oldTeam = getPlayersTeam(player.getUuid());
        if (oldTeam != null) {
            oldTeam.removePlayer(player.getUuid());
        }
        ItemShuffleTeam team = teams.get(teamName);
        ItemShufflePlayer player1 = new ItemShufflePlayer(player);
        team.addPlayer(player1);
        player1.setTeamName(team.getColor());
        server.getScoreboard().clearPlayerTeam(player.getName().getString());
        server.getScoreboard().addPlayerToTeam(player.getName().getString(), server.getScoreboard().getTeam(team.getName()));
        ItemShuffle.getInstance().broadcast("§7Player §f" + player.getName().getString() + " §7joined team §" + team.getColor() + team.getName());
    }

    public ItemShuffleTeam getPlayersTeam(UUID player) {
        for (ItemShuffleTeam t : teams.values()) {
            for (ItemShufflePlayer p : t.getPlayers().values()) {
                if (p.getUuid() == player) return t;
            }
        }
        return null;
    }

    public void setItemQueues(Queue<Item> items) {
        if (teamMode()) {
            setTeamItemQueues(items);
        } else {
            setPlayerItemQueues(items);
        }
    }

    private void setPlayerItemQueues(Queue<Item> items) {
        players.values().forEach(p -> p.setItemQueue(items));
    }

    private void setTeamItemQueues(Queue<Item> items) {
        teams.values().forEach(t -> t.setItemQueue(items));
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

    public void refreshTeamData(boolean show) {
        PacketByteBuf buf = PacketByteBufs.create();
        if (show) {
            buf.writeInt(teams.size());
            for (ItemShuffleTeam team : teams.values()) {
                buf.writeBytes(TeamData.fromTeam(team).toPacket());
            }
        } else {
            buf.writeInt(0);
        }
        players.values().forEach(p -> p.sendTeamData(buf));
    }

    public void refreshOtherItems(boolean show) {
        PacketByteBuf buf = PacketByteBufs.create();
        if (show) {
            buf.writeInt(players.size());
            for (ItemShufflePlayer player : players.values()) {
                buf.writeString(player.getPlayer().getName().getString());
                if (player.getItem() != null) {
                    buf.writeItemStack(new ItemStack(player.getItem()));
                    buf.writeBoolean(player.isCompleted());
                } else {
                    buf.writeItemStack(new ItemStack(Items.AIR));
                    buf.writeBoolean(false);
                }
            }
        } else {
            buf.writeInt(0);
        }
        players.values().forEach(p -> p.sendOtherItems(buf));
    }

    public void broadcastScore(boolean onlyFailed) {
        if (teamMode()) {
            teams.values().forEach(t -> t.broadcastScore(onlyFailed));
        } else {
            players.values().forEach(player -> player.broadcastScore(onlyFailed));
        }
    }

    public void broadcastRunScore() {
        if (teamMode()) {
            teams.values().forEach(ItemShuffleTeam::broadcastRunScore);
        } else {
            players.values().forEach(ItemShufflePlayer::broadcastRunScore);
        }
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

        int color = ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH ? ColorHelper.Argb.getArgb(150,100,65,165) : ColorHelper.Argb.getArgb(150,70,150,70);
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
