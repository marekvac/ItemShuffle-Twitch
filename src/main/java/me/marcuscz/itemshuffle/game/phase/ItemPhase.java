package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.world.item.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemPhase {

    private final int roundDuration;
    private final List<PhaseItem> items;
    private final int phaseIndex;
    private static final Random random = new Random();

    public ItemPhase(int roundDuration, int phaseIndex) {
        this.phaseIndex = phaseIndex;
        this.roundDuration = roundDuration;
        this.items = new ArrayList<>();
    }

    public ItemPhase addItems(List<Item> items, boolean uncraftable, int baseChance, int phaseIndex) {
        items.forEach(item -> {
            PhaseItem phaseItem = new PhaseItem(item, uncraftable, baseChance, phaseIndex);
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

    private static PhaseItem getRandomItem(List<PhaseItem> items, int currentPhase) {
        // 1. Nejprve si vyfiltrujeme kandidáty do listu.
        // Děláme to jen jednou, abychom nemuseli streamovat dvakrát (tvůj původní kód to dělal 2x).
        List<PhaseItem> candidates = new ArrayList<>(items.stream()
                .filter(ItemPhase::filterItem) // Předpokládám, že toto je tvůj filtr dostupnosti
                .toList());

        Collections.shuffle(candidates, random);

        if (candidates.isEmpty()) {
            return null;
        }

        // 2. Spočítáme celkovou váhu (Total Weight)
        double totalWeight = 0;
        for (PhaseItem item : candidates) {
            totalWeight += item.getEffectiveChance(currentPhase);
        }

        // 3. Hodíme kostkou
        double rand = random.nextDouble() * totalWeight;

        // 4. Najdeme vítěze
        for (PhaseItem item : candidates) {
            double chance = item.getEffectiveChance(currentPhase);
            rand -= chance;
            if (rand < 0) {
                return item;
            }
        }

        // Fallback pro float imprecisions (velmi vzácné, ale jistota je jistota)
        var item = candidates.get(candidates.size() - 1);
        item.chosen();
        return item;
    }

    private static boolean filterItem(PhaseItem item) {
        if (ItemShuffle.getInstance().getSettings().blockMode) {
            return item.isBlock();
        } else {
            return !item.isUncraftable();
        }
    }

    public static Item getItem(List<PhaseItem> items, int currentPhase) {
        PhaseItem phaseItem = null;
        int i = 0;
        while (phaseItem == null && i < 10) {
            phaseItem = getRandomItem(items, currentPhase);
            i++;
        }
        if (phaseItem != null) {
//            phaseItem.increaseChance(10);
            return phaseItem.getItem();
        }
        return null;
    }

    public static List<Item> getItems(List<PhaseItem> items, int size, int currentPhase) {
        List<Item> items1 = new ArrayList<>();
        int i = 0;
        while (items1.size() < size && i < 20) {
            PhaseItem phaseItem = getRandomItem(items, currentPhase);
            if (phaseItem != null && !items1.contains(phaseItem.getItem())) {
                items1.add(phaseItem.getItem());
//                phaseItem.increaseChance(10);
            }
            i++;
        }
        return items1;
    }

    public List<PhaseItem> getItems() {
        return items;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public int getPhaseIndex() {
        return phaseIndex;
    }

    @Override
    public String toString() {
        return "ItemPhase{" +
                "roundDuration=" + roundDuration +
                ", items=" + items +
                '}';
    }
}
