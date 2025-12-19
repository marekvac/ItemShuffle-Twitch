package me.marcuscz.itemshuffle.game.phase;

import java.util.Random;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.game.GameSettings;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class PhaseItem {

    private final Item item;
    private int chance;
    private final boolean isBlock;
    private final boolean uncraftable;
    private final int phaseIndex;
    private int timesChosen = 0;

    public PhaseItem(Item item, boolean uncraftable, int baseChance, int phaseIndex) {
        this.item = item;
        this.phaseIndex = phaseIndex;
//        int baseChance = 50;
        Random random = new Random();
        chance = baseChance + random.nextInt(40);
        isBlock = Block.byItem(item) != Blocks.AIR;
        this.uncraftable = uncraftable;
    }

    public void increaseChance(int by)
    {
        chance += by;
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

    public int getPhaseIndex() {
        return phaseIndex;
    }

    public int getTimesChosen() {
        return timesChosen;
    }

    public void chosen() {
        this.timesChosen++;
    }

    @Override
    public String toString() {
        return "PhaseItem{" +
                "item=" + item +
                ", chance=" + chance +
                '}';
    }

    public double getEffectiveChance(int currentPhase) {
        return getEffectiveChance(this, currentPhase);
    }

    private static double getEffectiveChance(PhaseItem item, int currentPhase) {
        double chance = item.getChance();
        if (item.getPhaseIndex() < currentPhase) {
            int difficulty = ItemShuffle.getInstance().getSettings().itemDifficulty;
            double penaltyFactor = GameSettings.DIFFICULTY_RATES[difficulty]; // 5 % původní šance
            chance *= penaltyFactor;
            if (chance < 1.0) {
                chance = 1.0;
            }
        }

        chance = chance / (1 + item.getTimesChosen());

        return chance;
    }
}
