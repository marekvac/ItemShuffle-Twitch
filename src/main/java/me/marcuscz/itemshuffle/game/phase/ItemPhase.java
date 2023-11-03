package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemPhase {

    private final int roundDuration;
    private final List<PhaseItem> items;

    public ItemPhase(int roundDuration) {
        this.roundDuration = roundDuration;
        this.items = new ArrayList<>();
    }

    public ItemPhase addItems(List<Item> items, boolean uncraftable, int baseChance) {
        items.forEach(item -> {
            PhaseItem phaseItem = new PhaseItem(item, uncraftable, baseChance);
            this.items.add(phaseItem);
        });
        return this;
    }

    private static PhaseItem getRandomItem(List<PhaseItem> items) {
        double sum = items.stream().filter(ItemPhase::filterItem).mapToDouble(PhaseItem::getChance).sum();
        double rand = Math.random() * sum;
        PhaseItem choice = null;
        for (PhaseItem i : items.stream().filter(ItemPhase::filterItem).toList()) {
            choice = i;
            rand -= i.getChance();
            if (rand < 0) {
                break;
            }
        }
        return choice;
    }

    private static boolean filterItem(PhaseItem item) {
        if (ItemShuffle.getInstance().getSettings().blockMode) {
            return item.isBlock();
        } else {
            return !item.isUncraftable();
        }
    }

    public static Item getItem(List<PhaseItem> items) {
        PhaseItem phaseItem = null;
        int i = 0;
        while (phaseItem == null && i < 10) {
            phaseItem = getRandomItem(items);
            i++;
        }
        if (phaseItem != null) {
            phaseItem.increaseChance(10);
            return phaseItem.getItem();
        }
        return null;
    }

    public static List<Item> getItems(List<PhaseItem> items, int size) {
        List<Item> items1 = new ArrayList<>();
        int i = 0;
        while (items1.size() < size && i < 20) {
            PhaseItem phaseItem = getRandomItem(items);
            if (phaseItem != null && !items1.contains(phaseItem.getItem())) {
                items1.add(phaseItem.getItem());
                phaseItem.increaseChance(5);
            }
            i++;
        }
        return items1;
    }

    public List<PhaseItem> getItems() {
        return items;
    }

    public void increaseAllItems(int by) {
        items.forEach(phaseItem -> phaseItem.increaseChance(by));
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    @Override
    public String toString() {
        return "ItemPhase{" +
                "roundDuration=" + roundDuration +
                ", items=" + items +
                '}';
    }
}
