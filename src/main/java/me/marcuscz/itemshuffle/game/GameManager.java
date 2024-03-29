package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;
import org.json.simple.parser.ParseException;

import java.io.IOException;

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
        ItemShuffle.getLogger().info("starring 1");
        if (active) {
            return false;
        }
        ItemShuffle.getLogger().info("starring 2");
        try {
            itemManager = new ItemManager();
        } catch (IOException | ParseException e) {
            ItemShuffle.getInstance().broadcast("§4Failed to load items from config! Please check phases.json file.");
            e.printStackTrace();
            return true;
        }
        ItemShuffle.getLogger().info("starring 3");

        nextRound();
        ItemShuffle.getLogger().info("starring 4");
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
            playerManager.startVotingClients();
            playerManager.createNewVotes(itemManager);
        }
        if (ItemShuffle.getInstance().getSettings().giveFood) {
            playerManager.giveFoods();
        }
        active = true;
        if (!disclaimer) {
            ItemShuffle.getInstance().broadcast("§7§oItemShuffle Twitch by MarcusCZ");
            disclaimer = true;
        }
        ItemShuffle.getLogger().info("starring 5");
        return true;
    }

    public boolean stop() {
        if (active) {
            active = false;
            playerManager.hideTimers();
            playerManager.hideItems();
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
                playerManager.stopVotingClients();
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
        ItemShuffle.getInstance().broadcast(new LiteralText("§6Game has been paused! ").append(new LiteralText("§2§nResume").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemshuffle resume")))));
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
        if (ItemShuffle.getInstance().getSettings().showItems) {
            playerManager.showItems();
        }
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TEAM) {
            playerManager.refreshTeamData(true);
        }
    }

    public boolean skip() {
        if (!active || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
            return false;
        }
        endRound(true);
        return true;
    }

    public void endRound(boolean isSkip) {
        playerManager.hideTimers();
        playerManager.hideItems();
        playerManager.refreshTeamData(false);

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
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
                playerManager.askVotingClientsForWinners();
            }

            // Check players items
            playerManager.checkAllPlayersItem();
            if (playerManager.isEveryoneCompleted()) {
                if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                    showRunScore();
                    stop();
                } else {
                    endRound(false);
                }
            }

            // <= 10 seconds remain
            if (currentTime <= 200) {
                if (!timesUp) {
                    timesUp = true;
                    playerManager.updateTimers(MathHelper.packRgb(170, 50, 50));
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
