package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.game.phase.PhaseManager;
import net.minecraft.item.Item;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class ItemManager {

    private final Map<UUID, Item> votingQueueItems;
    private final PhaseManager phaseManager;

    private final List<Item> runQueue;

    public ItemManager() throws IOException, ParseException {
        votingQueueItems = new HashMap<>();
        phaseManager = new PhaseManager();
        runQueue = new ArrayList<>();
    }

    public void nextRound(double skipFactor) {
        phaseManager.increaseRounds(skipFactor);
        if (ItemShuffle.getInstance().getSettings().debug) {
            phaseManager.printItems();
        }
    }

    public Item getRandomItem() {
        return phaseManager.getItem();
    }

    public List<Item> getVotingItems(int size) {
        return phaseManager.getVotingItems(size);
    }

    public void getRandomItemsForPlayers(Collection<ItemShufflePlayer> players) {
        Item item = null;
        if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
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
                    if (ItemShuffle.getInstance().getSettings().itemType != ItemGenType.ALL_SAME && ItemShuffle.getInstance().getSettings().itemType != ItemGenType.ALL_SAME_VS) {
                        item = getRandomItem();
                    }
                }
                player.setItem(item);
            }
        }
    }

    public void getRandomItemsForTeams(Collection<ItemShuffleTeam> teams) {
        if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
            Item item = getRandomItem();
            teams.forEach(t -> t.setItem(item));
        } else {
            teams.forEach(t -> t.setItem(getRandomItem()));
        }
    }

    public void setQueueItem(UUID uuid, Item item) {
        votingQueueItems.put(uuid, item);
    }

    public Queue<Item> createRunQueue(int size) {
        Queue<Item> items = new LinkedList<>();
        runQueue.clear();
        for (int i = 0; i < size; i++) {
            boolean found = false;
            int t = 0;
            while (!found) {
                Item item = phaseManager.getItem();
                if (!items.contains(item) || t > 30) {
                    items.add(item);
                    runQueue.add(item);
                    found = true;
                }
                t++;
            }
//            phaseManager.increaseRounds(0.5);
        }
        return items;
    }

    public List<Item> getRunItemList() {
        return runQueue;
    }
}
