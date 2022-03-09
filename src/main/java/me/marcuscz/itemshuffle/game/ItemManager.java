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

    public ItemManager() throws IOException, ParseException {
        votingQueueItems = new HashMap<>();
        phaseManager = new PhaseManager();
    }

    public void nextRound(double skipFactor) {
        phaseManager.increaseRounds(skipFactor);
        phaseManager.printItems();
    }

    public Item getRandomItem() {
        return phaseManager.getItem();
    }

    public List<Item> getVotingItems(int size) {
        return phaseManager.getVotingItems(size);
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
