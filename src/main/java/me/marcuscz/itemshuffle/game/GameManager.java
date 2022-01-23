package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;

public class GameManager {

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
        itemManager = new ItemManager();
        nextRound();
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
            playerManager.startVotingClients();
            playerManager.createNewVotes();
        }
        if (ItemShuffle.getInstance().getSettings().giveFood) {
            playerManager.giveFoods();
        }
        active = true;
        return true;
    }

    public boolean stop() {
        if (active) {
            active = false;
            playerManager.hideTimers();
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
        itemManager.nextRound();
        itemManager.getRandomItemsForPlayers(playerManager.getPlayers().values());
        itemMsgSent = false;
        paused = pausedDueFail = false;
        timesUp = false;
        playerManager.updateTimers();
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH) {
            playerManager.createNewVotes();
        }
    }

    public boolean skip() {
        if (!active) {
            return false;
        }
        endRound(true);
        return true;
    }

    public void endRound(boolean isSkip) {
        playerManager.hideTimers();
        if (!playerManager.someoneFailed()) {
            showScore();
            if (isSkip || !ItemShuffle.getInstance().getSettings().pauseOnFail) {
                nextRound();
            } else {
                pausedDueFail = true;
                pause();
            }
        } else {
            ItemShuffle.getInstance().broadcast("§aEveryone found their item!");
            nextRound();
        }
    }

    public void showScore() {
        ItemShuffle.getInstance().broadcast("§7Score:");
        playerManager.broadcastScore(true);
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
                endRound(false);
            }

            // <= 10 seconds remain
            if (currentTime <= 200) {
                if (!timesUp) {
                    timesUp = true;
                    playerManager.updateTimers(MathHelper.packRgb(170, 50, 50));
                }
                if (currentTime == 0) {
                    endRound(false);
                    return;
                }

                int time = currentTime / 20;
                String sec = time > 1 ? "seconds" : "second";
                ItemShuffle.getInstance().broadcast("§c§l" + time + " " + sec + " remain!", true);
            }
        }
    }
}
