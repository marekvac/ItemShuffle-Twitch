package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import org.json.simple.parser.ParseException;
import org.lwjgl.util.freetype.TT_Header;

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
    
    private GameSettings settings() {
        return ItemShuffle.getInstance().getSettings();
    }

    public boolean start() {
        if (active) {
            return false;
        }

        ItemShuffle.logDebug("Starting game manager: " + settings().gameType + ", ItemGenType: " + settings().itemType);

        String type = ItemShuffle.ITEM_GEN_DIMENSION_TYPES.getOrDefault(settings().itemGenDimension, "");
        try {
            itemManager = new ItemManager(type);
        } catch (IOException | ParseException e) {
            ItemShuffle.getInstance().broadcast("§4Failed to load items from config! Please check phases.json file.");
            e.printStackTrace();
            return true;
        }

        ItemShuffle.logDebug("Item manager initialized");

        if (settings().gameType.isBoardType()) {
            if (playerManager.getTeams().isEmpty()) {
                if (settings().gameType == GameType.BOARD && ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().size() > 4) {
                    ItemShuffle.getInstance().broadcast("§cYou need to manually specify teams!");
                    return true;
                }
                try {
                    int nteams = settings().gameType == GameType.BOARD ? 4 : 2;
                    playerManager.autoTeams(nteams, false);
                    for (int i = 0; i < ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().size(); i++) {
                        var p = ItemShuffle.getInstance().getServer().getPlayerList().getPlayers().get(i);
                        playerManager.joinPlayer(PlayerManager.TEAM_NAMES[i % nteams], p);
                    }
                } catch (Exception e) {
                    ItemShuffle.getInstance().broadcast("§cFailed to auto create teams for board. Create teams manually.");
                    e.printStackTrace();
                    return true;
                }
            }
            if (settings().itemDifficulty > 0) itemManager.increaseRounds(2);
            if (settings().itemDifficulty > 1) itemManager.increaseRounds(4);
            if (settings().itemDifficulty > 2) itemManager.increaseRounds(5);
        }

        if (settings().gameType == GameType.TIC_TAC_TOE && playerManager.getTeams().size() != 2) {
            ItemShuffle.getInstance().broadcast("§cTic Tac Toe requires exactly 2 teams!");
            return true;
        }

        if (settings().gameType.isBoardType()) {
            itemManager.createBoard(settings().boardSize);
        }


        ItemShuffle.logDebug("Board condition checked");

        ItemShuffle.logDebug("Next round from start");
        nextRound();

        ItemShuffle.logDebug("next round initialized");

        if (settings().gameType == GameType.TWITCH) {
            playerManager.startVotingClients();
            playerManager.createNewVotes(itemManager);
        }
        if (settings().itemType == ItemGenType.ALL_SAME_VS) {
            playerManager.resetPlayerScores();
        }
        if (settings().giveFood) {
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
            if (settings().gameType.isBoardType()) playerManager.hideBoards();
            if (settings().gameType == GameType.TWITCH) {
                playerManager.stopVotingClients();
            }
            if (!settings().gameType.isBoardType() && settings().itemType == ItemGenType.ALL_SAME_VS) {
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
            ItemShuffle.logDebug("Next round from resume");
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
        playerManager.hideBoards();
        if (settings().gameType.isBoardType()) {
            if (settings().gameType == GameType.BOARD) {
                time = -1;
                currentTime = time;
                itemMsgSent = true;
                paused = pausedDueFail = false;
                timesUp = false;
            }
            if (settings().gameType == GameType.TIC_TAC_TOE) {
                time = settings().time;
                currentTime = time;
                itemMsgSent = true;
                paused = pausedDueFail = false;
                timesUp = false;
                playerManager.updateTimers();
            }
            reRollBoard(false);
        } else {
            time = settings().time;
            currentTime = time;

            if (settings().itemType != ItemGenType.RUN) {
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
            if (settings().gameType == GameType.TWITCH) {
                playerManager.createNewVotes(itemManager);
            }
            if (settings().showItemsMode != ItemShowType.OFF) {
                playerManager.showItems();
                playerManager.refreshOtherItems(true);
            }
            if (settings().gameType == GameType.TEAM) {
                playerManager.refreshTeamData(true);
            }
        }
    }


    public boolean reRollBoard(boolean start) {
        if (!settings().gameType.isBoardType()) return false;

        List<Item> boardItems;
        if (start) {
            boardItems = itemManager.createBoard(settings().boardSize);
        } else {
            boardItems = itemManager.boardItems;
        }

        if (settings().gameType == GameType.BOARD) {
            playerManager.initBoards(settings().boardSize, boardItems);
            playerManager.getPlayers().values().forEach(p -> p.setCompleted(false));
            itemManager.increaseRounds(1);
        } else if (settings().gameType == GameType.TIC_TAC_TOE) {
            swapTicTacToeTeam();
        }
        return true;
    }

    public void swapTicTacToeTeam() {
        if (settings().gameType != GameType.TIC_TAC_TOE) return;
        playerManager.hideBoardsCurrentTeam();
        playerManager.swapActiveTeam();
        playerManager.initBoardsCurrentTeam(settings().boardSize, itemManager.boardItems);
        playerManager.setBoardStatusCurrentTeam(itemManager.boardStatus);
        ItemShuffle.getInstance().broadcast("§eIt's now §" + playerManager.getActiveTeam().getColor() + playerManager.getActiveTeam().getName() + "§e's turn!");
    }

    public boolean skip() {
        if (!active || settings().itemType == ItemGenType.RUN || settings().gameType == GameType.BOARD) {
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

        if (settings().itemType == ItemGenType.ALL_SAME_VS || settings().gameType == GameType.TIC_TAC_TOE) {
            ItemShuffle.logDebug("Next round from endRound ALL_SAME_VS or TIC_TAC_TOE");
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
            ItemShuffle.logDebug("Next round from endRound everyone completed");
            nextRound();
        }
    }

    private void pauseOrContinue(boolean isSkip) {
        pausedDueFail = true;
        if (isSkip || !settings().pauseOnFail) {
            ItemShuffle.logDebug("Next round from pauseOrContinue");
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
            ItemShuffle.getLogger().error("completeBoardItem: player is not in a team, but should be");
            return;
        }
        ItemShuffle.getInstance().broadcast("§7Team §" + team.getColor() + team.getName() + "§7 found item §f" + item.getName().getString());
        itemManager.completedBoardItems.add(item);
        itemManager.boardStatus.set(n, team.getColor());
        if (settings().gameType == GameType.BOARD) playerManager.setBoardStatus(itemManager.boardStatus);

        if (checkBoardStatus(itemManager.boardStatus)) {
            playerManager.initBoards(settings().boardSize, itemManager.boardItems);
            playerManager.setBoardStatus(itemManager.boardStatus);
            ItemShuffle.getInstance().broadcast("§7Team §" + team.getColor() + team.getName() + "§7 has won!");
            playerManager.setActiveTeam(null);
            pausedDueFail = true;
            pause();
        } else {
            if (settings().gameType == GameType.TIC_TAC_TOE) {
                nextRound();
            }
        }
    }

    public boolean checkBoardStatus(List<Character> status) {
        for (int i = 0; i < settings().boardSize; i++) {
            for (int j = 0; j < settings().boardSize; j++) {
                Character s = status.get(i*settings().boardSize + j);
                if (s == null) continue;
                // 0, -1 ; 1, -1 ; 1, 0 ; 1 , 1
                int t = settings().boardTarget;
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
        if (x < 0 || x >= settings().boardSize || y < 0 || y >= settings().boardSize) {
            return null;
        }
        return status.get(x*settings().boardSize + y);
    }

    public void tick(MinecraftServer minecraftServer) {
        if (!active || paused) {
            return;
        }

        currentTime--;

        // Send item messages to players later, because networking is faster than broadcast
        if (!itemMsgSent && time - currentTime > 5) {
            playerManager.sendItems();
            itemMsgSent = true;
        }

        if ((currentTime % 20) == 0) {

            // Get Voting Items winner from clients
            if (settings().gameType == GameType.TWITCH) {
                playerManager.askVotingClientsForWinners();
            }

            // Check players items
            playerManager.checkAllPlayersItem();

            if (!settings().gameType.isBoardType() && settings().itemType == ItemGenType.ALL_SAME_VS) {
                if (playerManager.isAnyoneCompleted()) {
                    showVsScore();
                    endRound(false);
                }
            } else {
                if (!settings().gameType.isBoardType() && playerManager.isEveryoneCompleted()) {
                    if (settings().itemType == ItemGenType.RUN) {
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
                    if (settings().itemType == ItemGenType.RUN) {
                        showRunScore();
                        stop();
                    } else {
                        if (settings().gameType == GameType.TIC_TAC_TOE) {
                            ItemShuffle.getInstance().broadcast("§cTeam §" + playerManager.getActiveTeam().getColor() + playerManager.getActiveTeam().getName() + "§c ran out of time!");
                        }
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
