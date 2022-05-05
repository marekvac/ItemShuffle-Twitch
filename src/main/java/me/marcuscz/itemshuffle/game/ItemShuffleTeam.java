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

    public ItemShuffleTeam(String name) {
        this.name = name;
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
            this.completed = true;
            players.values().forEach(p -> {
                if (!p.isCompleted()) {
                    p.setCompleted(true);
                    p.sendCompletedItem();
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

    public Map<UUID, ItemShufflePlayer> getPlayers() {
        return players;
    }

    public void broadcastScore(boolean onlyFailed) {
        if (fails == 0 && onlyFailed) {
            return;
        }
        String score = (fails == 0 ? "§2 " : "§c ") + fails + " fails";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }
}
