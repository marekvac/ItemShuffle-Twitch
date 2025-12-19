package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.world.item.Item;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPhaseBuilder {

    private final List<Item> items;

    public ItemPhaseBuilder() {
        ItemShuffle.logDebug("ItemPhaseBuilder");
        this.items = new ArrayList<>();
    }

    public ItemPhaseBuilder addItem(Item item) {
        items.add(item);
        return this;
    }

    public ItemPhaseBuilder addItems(Item ... items) {
        this.items.addAll(Arrays.stream(items).toList());
        return this;
    }

    public List<Item> getItems() {
        // Remove duplicates, if are present
        return new ArrayList<>(new LinkedHashSet<>(this.items));
    }

}
