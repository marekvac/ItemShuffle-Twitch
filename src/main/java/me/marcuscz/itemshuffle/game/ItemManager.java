package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ItemManager {

    private final GameManager gameManager;
    private final Map<UUID, Item> votingQueueItems;

    public ItemManager(GameManager gameManager) {
        this.gameManager = gameManager;
        votingQueueItems = new HashMap<>();
    }

    public static Item getRandomItem() throws IllegalAccessException {
        Field[] items = Items.class.getDeclaredFields();
        List<Field> items1 = Arrays.stream(items).collect(Collectors.toList());
        Random random = new Random();
        Field field = items1.get(random.nextInt(items1.size()));
        return (Item) field.get(Item.class);
    }

    public static List<Item> getRandomItemList(int size) throws IllegalAccessException {
        List<Item> items = new ArrayList<>();
        while (items.size() < size) {
            Item item = getRandomItem();
            if (!items.contains(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public void getRandomItemsForPlayers(Collection<ItemShufflePlayer> players) throws IllegalAccessException {
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
