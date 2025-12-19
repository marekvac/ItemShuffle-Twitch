package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

public class GameManager {

    public static final int RUN_SIZE = 64;
    private static GameManager instance;
    private static boolean active;
    private static boolean paused;
    private static int time;
    private static int currentTime;

    private boolean pausedDueFail;
    private boolean itemMsgSent;
    private boolean timesUp;
    private PlayerManager playerManager;
    private ItemManager itemManager;
    private boolean disclaimer;

    public GameManager() {
        instance = this;
    }

    public void initPlayerManager() {
        playerManager = new PlayerManager();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public boolean start() {
        if (active) {
            return false;
        }

        ItemShuffle.logDebug("Starting game manager");

        String type = ItemShuffle.ITEM_GEN_DIMENSION_TYPES.getOrDefault(ItemShuffle.getInstance().getSettings().itemGenDimension, "");
        try {
            itemManager = new ItemManager(type);
        } catch (IOException | ParseException e) {
            ItemShuffle.getInstance().broadcast("§4Failed to load items from config! Please check phases.json file.");
            e.printStackTrace();
            return true;
        }

        ItemShuffle.logDebug("Item manager initialized");

        if (ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD && playerManager.getTeams().isEmpty()) {
            if (ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().size() > 4) {
                ItemShuffle.getInstance().broadcast("§cYou need to manually specify teams!");
                return true;
            }
            try {
                playerManager.autoTeams(ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().size(), false);
                for (int i = 0; i < ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().size(); i++) {
                    playerManager.joinPlayer(PlayerManager.TEAM_NAMES[i], ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().get(i));
                }
            } catch (Exception e) {
                ItemShuffle.getInstance().broadcast("§cFailed to auto create teams for board. Create teams manually.");
                e.printStackTrace();
                return true;
            }
        }

        ItemShuffle.logDebug("Board condition checked");

        nextRound();

        ItemShuffle.logDebug("next round initialized");

        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
            playerManager.startVotingClients();
            playerManager.createNewVotes(itemManager);
        }
        if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
            playerManager.resetPlayerScores();
        }
        if (ItemShuffle.getInstance().getSettings().giveFood) {
            playerManager.giveFoods();
        }
        active = true;
        if (!disclaimer) {
            ItemShuffle.getInstance().broadcast("§7§oItemShuffle Twitch by MarcusCZ");
            disclaimer = true;
        }
        if (!type.isEmpty()) {
            ItemShuffle.getInstance().broadcast("§e§oItem Generator Dimension is set to: §2§o" + type.toUpperCase());
        }
        return true;
    }

    public boolean stop() {
        if (active) {
            active = false;
            playerManager.hideTimers();
            playerManager.hideItems();
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD) playerManager.hideBoards();
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
                playerManager.stopVotingClients();
            }
            if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
                showVsScore();
            }
            playerManager.clearPlayers();
            ItemShuffle.getInstance().broadcast("§cStopped");
            return true;
        }
        return false;
    }

    public boolean pause() {
        if (!active || paused) {
            return false;
        }
        paused = true;
        playerManager.hideTimers();
        playerManager.hideItems();
        ItemShuffle.getInstance().broadcast(Component.literal("§6Game has been paused! ").append(Component.literal("§2§nResume").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/itemshuffle resume")))));
        return true;
    }

    public boolean resume() {
        if (!active || !paused) {
            return false;
        }
        if (pausedDueFail) {
            nextRound();
            return true;
        }
        playerManager.updateTimers();
        paused = false;
        ItemShuffle.getInstance().broadcast("§aResumed!");
        return true;
    }

    private void nextRound() {
        playerManager.refreshPlayers();
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD) {
            time = -1;
            currentTime = time;
            itemMsgSent = true;
            paused = pausedDueFail = false;
            timesUp = false;

            reRollBoard(true);
        } else {
            time = ItemShuffle.getInstance().getSettings().time;
            currentTime = time;

            if (ItemShuffle.getInstance().getSettings().itemType != ItemGenType.RUN) {
                itemManager.nextRound(pausedDueFail ? 0.5 : 1);
                if (PlayerManager.teamMode()) {
                    itemManager.getRandomItemsForTeams(playerManager.getTeams().values());
                } else {
                    itemManager.getRandomItemsForPlayers(playerManager.getPlayers().values());
                }
            } else {
                playerManager.setItemQueues(itemManager.createRunQueue(RUN_SIZE));
            }

            itemMsgSent = false;
            paused = pausedDueFail = false;
            timesUp = false;
            playerManager.updateTimers();
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
                playerManager.createNewVotes(itemManager);
            }
            if (ItemShuffle.getInstance().getSettings().showItemsMode != ItemShowType.OFF) {
                playerManager.showItems();
                playerManager.refreshOtherItems(true);
            }
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TEAM) {
                playerManager.refreshTeamData(true);
            }
        }
    }

    public boolean reRollBoard(boolean start) {
        if (ItemShuffle.getInstance().getSettings().gameType != GameType.BOARD || !start && !active) return false;

        List<Item> boardItems = itemManager.createBoard(ItemShuffle.getInstance().getSettings().boardSize);
        playerManager.initBoards(ItemShuffle.getInstance().getSettings().boardSize, boardItems);
        playerManager.getPlayers().values().forEach(p -> p.setCompleted(false));
        itemManager.increaseRounds(1);
        return true;
    }

    public boolean skip() {
        if (!active || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN || ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD) {
            return false;
        }
        endRound(true);
        return true;
    }

    public void endRound(boolean isSkip) {
        playerManager.hideTimers();
        playerManager.hideItems();
        playerManager.refreshTeamData(false);
        playerManager.refreshOtherItems(false);

        if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
            nextRound();
            return;
        }

        if (playerManager.allFailed()) {
            ItemShuffle.getInstance().broadcast("§4Everyone failed their item!");
            pauseOrContinue(isSkip);
        } else if (!playerManager.allCompleted()) {
            showScore();
            pauseOrContinue(isSkip);
        } else {
            ItemShuffle.getInstance().broadcast("§aEveryone found their item!");
            nextRound();
        }
    }

    private void pauseOrContinue(boolean isSkip) {
        pausedDueFail = true;
        if (isSkip || !ItemShuffle.getInstance().getSettings().pauseOnFail) {
            nextRound();
        } else {
            pause();
        }
    }

    public void showScore() {
        ItemShuffle.getInstance().broadcast("§7Score:");
        playerManager.broadcastScore(true);
    }

    public void showRunScore() {
        ItemShuffle.getInstance().broadcast("§7RUN Score:");
        playerManager.broadcastRunScore();
    }

    public void showVsScore() {
        ItemShuffle.getInstance().broadcast("§7VERSUS Points:");
        playerManager.broadcastRunScore();
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static int getCurrentTime() {
        return currentTime;
    }

    public static int getTime() {
        return time;
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void completeBoardItem(Item item, ItemShufflePlayer player) {
        int n = itemManager.boardItems.indexOf(item);
        if (n == -1) {
            ItemShuffle.getLogger().error("completeBoardItem: item is not in list, but it should be");
            return;
        }
        ItemShuffleTeam team = playerManager.getPlayersTeam(player.getUuid());
        if (team == null) {
            return;
        }
        ItemShuffle.getInstance().broadcast("§7Team §" + team.getColor() + team.getName() + "§7 found item §f" + item.getName().getString());
        itemManager.completedBoardItems.add(item);
        itemManager.boardStatus.set(n, team.getColor());
        playerManager.setBoardStatus(itemManager.boardStatus);

        if (checkBoardStatus(itemManager.boardStatus)) {
            ItemShuffle.getInstance().broadcast("§7Team §" + team.getColor() + team.getName() + "§7 has won!");
            pausedDueFail = true;
            pause();
        }
    }

    public boolean checkBoardStatus(List<Character> status) {
        for (int i = 0; i < ItemShuffle.getInstance().getSettings().boardSize; i++) {
            for (int j = 0; j < ItemShuffle.getInstance().getSettings().boardSize; j++) {
                Character s = status.get(i*ItemShuffle.getInstance().getSettings().boardSize + j);
                if (s == null) continue;
                // 0, -1 ; 1, -1 ; 1, 0 ; 1 , 1
                int t = ItemShuffle.getInstance().getSettings().boardTarget;
                if (checkDir(status, s, i, j, 0,-1,1) >= t || checkDir(status, s, i, j, 1, -1, 1) >= t || checkDir(status, s, i, j, 1, 0, 1) >= t || checkDir(status, s, i, j, 1, 1, 1) >= t) {
                    return true;
                }
            }
        }
        return false;
    }

    public int checkDir(List<Character> status, Character s, int x, int y, int dx, int dy, int n) {
        if (getListItem(status, x+dx, y+dy) != s) return n;
        return checkDir(status, s, x+dx, y+dy, dx, dy, n+1);
    }

    private Character getListItem(List<Character> status, int x, int y) {
        if (x < 0 || x >= ItemShuffle.getInstance().getSettings().boardSize || y < 0 || y >= ItemShuffle.getInstance().getSettings().boardSize) {
            return null;
        }
        return status.get(x*ItemShuffle.getInstance().getSettings().boardSize + y);
    }

    public void tick(MinecraftServer minecraftServer) {
        if (!active || paused) {
            return;
        }

        if (currentTime > 0) currentTime--;

        // Send item messages to players later, because networking is faster than broadcast
        if (!itemMsgSent && time - currentTime > 5) {
            playerManager.sendItems();
            itemMsgSent = true;
        }

        if ((currentTime % 20) == 0) {

            // Get Voting Items winner from clients
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
                playerManager.askVotingClientsForWinners();
            }

            // Check players items
            playerManager.checkAllPlayersItem();

            if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
                if (playerManager.isAnyoneCompleted()) {
                    showVsScore();
                    endRound(false);
                }
            } else {
                if (playerManager.isEveryoneCompleted()) {
                    if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                        showRunScore();
                        stop();
                    } else {
                        endRound(false);
                    }
                }
            }


            // <= 10 seconds remain
            if (currentTime <= 200 && currentTime >= 0) {
                if (!timesUp) {
                    timesUp = true;
                    playerManager.updateTimers(ARGB.color(150,170,50,50));
                }
                if (currentTime == 0) {
                    if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                        showRunScore();
                        stop();
                    } else {
                        endRound(false);
                    }
                    return;
                }

                int time = currentTime / 20;
                String sec = time > 1 ? "seconds" : "second";
                ItemShuffle.getInstance().broadcast("§c§l" + time + " " + sec + " remain!", true);
            }
        }
    }
}
