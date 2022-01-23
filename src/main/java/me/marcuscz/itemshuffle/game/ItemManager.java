package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.game.phase.PhaseManager;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ItemManager {

    private final Map<UUID, Item> votingQueueItems;
    private final PhaseManager phaseManager;

    public ItemManager() {
        votingQueueItems = new HashMap<>();
        phaseManager = new PhaseManager();
    }

    public void nextRound() {
        phaseManager.increaseRounds();
    }

    public Item getRandomItem() {
        return phaseManager.getItem();
    }

    public List<Item> getRandomItemList(int size) {
        List<Item> items = new ArrayList<>();
        while (items.size() < size) {
            Item item = getRandomItem();
            if (!items.contains(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public void getRandomItemsForPlayers(Collection<ItemShufflePlayer> players) {
        Item item = null;
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.ALL_SAME) {
             item = getRandomItem();
        }
        for (ItemShufflePlayer player : players) {
            if (player.isOnline()) {
                // Get item from voting queue
                if (ItemShuffle.getInstance().getSettings().gameType == GameType.TWITCH && votingQueueItems.containsKey(player.getUuid())) {
                    item = votingQueueItems.get(player.getUuid());
                    votingQueueItems.remove(player.getUuid());
                } else {
                    // Else get random item
                    if (ItemShuffle.getInstance().getSettings().gameType != GameType.ALL_SAME) {
                        item = getRandomItem();
                    }
                }
                player.setItem(item);
            }
        }
    }

    public void setQueueItem(UUID uuid, Item item) {
        votingQueueItems.put(uuid, item);
    }
}
