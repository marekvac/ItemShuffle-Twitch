package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhaseManager {

    private int roundsLasted;
    private int currentPhase;
    private final List<ItemPhase> phases;
    private final List<Item> availableItems;

    public PhaseManager() {
        phases = new ArrayList<>();
        availableItems = new ArrayList<>();

        List<Item> phase1 = new ItemPhaseBuilder()
                .addLike("acacia")
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase1));

        List<Item> phase2 = new ItemPhaseBuilder()
                .addLike("spruce")
                .getItems();
        phases.add(new ItemPhase(1).setItems(phase2));

        List<Item> phase3 = new ItemPhaseBuilder()
                .addLike("birch")
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase3));

        List<Item> phase4 = new ItemPhaseBuilder()
                .addLike("nether")
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase4));

        currentPhase = 0;
        availableItems.addAll(phases.get(currentPhase).getItems());

        ItemShuffle.getLogger().info("Phases initialized!");
        ItemShuffle.getLogger().info("Phases: " + phases);

        roundsLasted = 0;
    }

    public Item getItem() {
        Random random = new Random();
        return availableItems.get(random.nextInt(availableItems.size()));
    }

    public void increaseRounds() {
        roundsLasted++;
        if (roundsLasted > phases.get(currentPhase).getRoundDuration()) {
            currentPhase++;
            if (currentPhase < phases.size()) {
                ItemShuffle.getLogger().info("New Phase");
                roundsLasted = 1;
                availableItems.addAll(phases.get(currentPhase).getItems());
            }
        }
    }
}
