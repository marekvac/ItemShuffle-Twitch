package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;

import java.util.Random;

public class PhaseItem {

    private final Item item;
    private int chance;
    private final boolean isBlock;
    private final boolean uncraftable;

    public PhaseItem(Item item, boolean uncraftable, int baseChance) {
        this.item = item;
//        int baseChance = 50;
        Random random = new Random();
        chance = baseChance + random.nextInt(40);
        isBlock = Block.getBlockFromItem(item) != Blocks.AIR;
        this.uncraftable = uncraftable;
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

    public boolean isBlock() {
        return isBlock;
    }

    public boolean isUncraftable() {
        return uncraftable;
    }

    @Override
    public String toString() {
        return "PhaseItem{" +
                "item=" + item +
                ", chance=" + chance +
                '}';
    }
}
