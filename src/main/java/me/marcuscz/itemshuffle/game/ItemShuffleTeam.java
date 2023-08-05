package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ItemShuffleTeam {

    private final Map<UUID, ItemShufflePlayer> players;
    private int fails;
    private Item item;
    private boolean completed;
    private final String name;
    private Queue<Item> itemQueue;
    private int runPoints;
    private final char color;

    public ItemShuffleTeam(String name, char color) {
        this.name = name;
        this.color = color;
        this.players = new HashMap<>();
    }

    public void addPlayer(ItemShufflePlayer player) {
        this.players.put(player.getUuid(), player);
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
    }

    public boolean hasPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean failed() {
        if (completed) {
            return false;
        }
        fails++;
        ItemShuffle.getInstance().broadcast("§4Team §c" + name + "§4 failed their item!");
        return true;
    }

    public void checkItem() {
        if (this.completed) return;

        AtomicBoolean completed = new AtomicBoolean(false);
        players.values().forEach(p -> {
            p.checkItem();
            if (p.isCompleted()) {
                completed.set(true);
            }
        });
        if (completed.get()) {
            if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                item = itemQueue.poll();
                runPoints++;
            } else if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
                runPoints++;
                this.completed = true;
            } else {
                this.completed = true;
            }
            GameManager.getInstance().getPlayerManager().refreshTeamData(true);
            players.values().forEach(p -> {
                if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                    p.setItem(item);
                    p.sendItem();
                    p.setCompleted(false);
//                    p.getPlayer().sendMessage(new LiteralText("§fRun Points: §b" + runPoints), false);
                } else {
                    if (!p.isCompleted()) {
                        p.setCompleted(true);
                        p.sendCompletedItem();
                    }
                }
            });
            ItemShuffle.getInstance().broadcast("§2Team §2" + name + "§2 has found their item!");
        }
    }

    public void setItem(Item item) {
        this.item = item;
        completed = false;
        players.values().forEach(p -> p.setItem(item));
    }

    public void setItemQueue(Queue<Item> itemQueue) {
        this.itemQueue = new LinkedList<>(itemQueue);
        item = this.itemQueue.poll();
        completed = false;
        players.values().forEach(p -> p.setItem(item));
    }

    public void resetPoints() {
        runPoints = 0;
    }

    public void skipItem() {
        item = this.itemQueue.poll();
        completed = false;
        runPoints--;
        if (runPoints < 0) runPoints = 0;
        players.values().forEach(p -> {
            p.setItem(item);
            p.sendItem();
//            p.getPlayer().sendMessage(new LiteralText("§fRun Points: §b" + runPoints), false);
        });
        ItemShuffle.getInstance().broadcast("§7Team §f" + name + "§7 skipped their item");
        GameManager.getInstance().getPlayerManager().refreshTeamData(true);
    }

    public Map<UUID, ItemShufflePlayer> getPlayers() {
        return players;
    }

    public String getName() {
        return name;
    }

    public char getColor() {
        return color;
    }

    public int getFails() {
        return fails;
    }

    public Item getItem() {
        return item;
    }

    public int getRunPoints() {
        return runPoints;
    }

    public void broadcastScore(boolean onlyFailed) {
        if (fails == 0 && onlyFailed) {
            return;
        }
        String score = (fails == 0 ? "§2 " : "§c ") + fails + " fails";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }

    public void broadcastRunScore() {
        String score = (runPoints == 0 ? "§c " : "§b ") + runPoints + " points";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }
}
