package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.item.Item;

import java.util.List;

public class ItemPhase {

    private final int roundDuration;
    private List<Item> items;

    public ItemPhase(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public ItemPhase setItems(List<Item> items) {
        this.items = items;
        return this;
    }

    public List<Item> getItems() {
        return items;
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
