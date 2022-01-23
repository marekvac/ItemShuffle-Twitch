package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPhaseBuilder {

    private final List<Item> items;
    private List<Item> allItems;

    public ItemPhaseBuilder() {
        this.items = new ArrayList<>();
        List<Field> fields = List.of(Items.class.getDeclaredFields());
        allItems = fields.stream().map(this::transform).collect(Collectors.toList());
    }

    private Item transform(Field field) {
        try {
            return (Item) field.get(Item.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // If fails, which should not happen, return AIR
        return Items.AIR;
    }

    public ItemPhaseBuilder addItem(Item item) {
        items.add(item);
        return this;
    }

    public ItemPhaseBuilder addItems(Item ... items) {
        this.items.addAll(Arrays.stream(items).toList());
        return this;
    }

    public ItemPhaseBuilder addLike(String q) {
        List<Item> query = allItems.stream().filter(item -> item.getTranslationKey().toLowerCase().contains(q.toLowerCase())).collect(Collectors.toList());
        this.items.addAll(query);
        return this;
    }

    public ItemPhaseBuilder not(String q) {
        allItems = allItems.stream().filter(item -> !item.getTranslationKey().toLowerCase().contains(q.toLowerCase())).collect(Collectors.toList());
        return this;
    }

    public List<Item> getItems() {
        // Remove duplicates, if are present
        return new ArrayList<>(new LinkedHashSet<>(this.items));
    }

}
