package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.item.Item;

import java.util.Random;

public class PhaseItem {

    private final Item item;
    private int chance;

    public PhaseItem(Item item) {
        this.item = item;
        int baseChance = 50;
        Random random = new Random();
        chance = baseChance + random.nextInt(40);
    }

    public void increaseChance(int by)
    {
        chance -= by;
    }

    public int getChance() {
        return chance;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "PhaseItem{" +
                "item=" + item +
                ", chance=" + chance +
                '}';
    }
}
