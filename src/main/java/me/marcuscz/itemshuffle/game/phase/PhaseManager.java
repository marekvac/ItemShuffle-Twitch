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
                .addLike("oak")
                .addLike("birch")
                .addLike("spruce")
                .addLike("acacia")
                .addLike("concrete")
                .addLike("wool")
                .addLike("andesite")
                .addLike("granite")
                .addLike("diorite")
                .addLike("glass")
                .addLike("banner")
                .addLike("sandstone")
                .addLike("carpet")
                .addLike("terracotta")
                .not("brown")
                .not("petrified")
                .addItems(
                        Items.CRAFTING_TABLE,
                        Items.FURNACE,
                        Items.CHEST,
                        Items.STONE_BRICKS,
                        Items.STONE_BUTTON,
                        Items.HAY_BLOCK,
                        Items.ICE,
                        Items.LANTERN,
                        Items.BRICKS,
                        Items.BRICK_WALL,
                        Items.LOOM,
                        Items.COMPOSTER,
                        Items.BARREL,
                        Items.SMOKER,
                        Items.BLAST_FURNACE,
                        Items.CARTOGRAPHY_TABLE,
                        Items.FLETCHING_TABLE,
                        Items.GRINDSTONE,
                        Items.SMITHING_TABLE,
                        Items.STONECUTTER,
                        Items.PUFFERFISH,
                        Items.COD,
                        Items.COOKED_COD,
                        Items.CHICKEN,
                        Items.COOKED_MUTTON
                )
                .getItems();
        phases.add(new ItemPhase(1).setItems(phase1));

        List<Item> phase2 = new ItemPhaseBuilder()
                .addLike("pumpkin")
                .addLike("melon")
                .addLike("jungle")
                .addLike("red_sand")
                .addLike("brown")
                .addItems(
                        Items.ANVIL,
                        Items.IRON_BLOCK,
                        Items.GOLD_BLOCK,
                        Items.BELL,
                        Items.EMERALD,
                        Items.JUKEBOX,
                        Items.PISTON,
                        Items.STICKY_PISTON,
                        Items.SLIME_BALL,
                        Items.REDSTONE_BLOCK,
                        Items.REPEATER,
                        Items.TNT,
                        Items.MINECART,
                        Items.CAMPFIRE,
                        Items.MAP,
                        Items.CLOCK,
                        Items.BOOK,
                        Items.CAKE,
                        Items.COOKIE,
                        Items.BEETROOT,
                        Items.MUSHROOM_STEW,
                        Items.BROWN_MUSHROOM,
                        Items.RED_MUSHROOM
                )
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase2));

        // Nether phase
        List<Item> phase3 = new ItemPhaseBuilder()
                .addLike("basalt")
                .addLike("crimson")
                .addLike("warped")
                .addItems(
                        Items.NETHERRACK,
                        Items.GLOWSTONE,
                        Items.REDSTONE_LAMP,
                        Items.QUARTZ,
                        Items.QUARTZ_BLOCK,
                        Items.SMOOTH_QUARTZ,
                        Items.NETHER_BRICK,
                        Items.BLAZE_POWDER,
                        Items.BREWING_STAND,
                        Items.SOUL_SAND,
                        Items.MAGMA_BLOCK,
                        Items.MOSSY_COBBLESTONE,
                        Items.MOSSY_COBBLESTONE_SLAB,
                        Items.MOSSY_COBBLESTONE_WALL,
                        Items.MOSSY_STONE_BRICK_WALL,
                        Items.OBSIDIAN
                )
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase3));

        currentPhase = 0;
        availableItems.addAll(phases.get(currentPhase).getItems());

        ItemShuffle.getLogger().info("Phases initialized!");
        ItemShuffle.getLogger().info("Phases: " + phases);

        roundsLasted = 0;
    }

    public List<Item> getVotingItems(int size) {
        List<Item> items = new ArrayList<>();
        Random random = new Random();
        ItemPhase cp;
        if (currentPhase == 0) {
            cp = phases.get(currentPhase);
            while (items.size() < size) {
                Item currentItem = cp.getItems().get(random.nextInt(cp.getItems().size()));
                if (!items.contains(currentItem)) {
                    items.add(currentItem);
                }
            }
        } else if (currentPhase == 1) {
            cp = phases.get(0);
            while (items.size() < size-1) {
                Item currentItem = cp.getItems().get(random.nextInt(cp.getItems().size()));
                if (!items.contains(currentItem)) {
                    items.add(currentItem);
                }
            }
            cp = phases.get(1);
            items.add(cp.getItems().get(random.nextInt(cp.getItems().size())));
        } else {
            cp = phases.get(0);
            while (items.size() < size-2) {
                Item currentItem = cp.getItems().get(random.nextInt(cp.getItems().size()));
                if (!items.contains(currentItem)) {
                    items.add(currentItem);
                }
            }
            if (random.nextBoolean()) {
                cp = phases.get(1);
            }
            items.add(cp.getItems().get(random.nextInt(cp.getItems().size())));
            cp = phases.get(0);
            if (random.nextBoolean()) {
                cp = phases.get(2);
            }
            items.add(cp.getItems().get(random.nextInt(cp.getItems().size())));
        }
        return items;
    }

    public Item getItem() {
        Random random = new Random();
        if (currentPhase > 0 && currentPhase < phases.size()) {
            if (random.nextInt(4) < 3) {
                ItemPhase cp = phases.get(currentPhase);
                return cp.getItems().get(random.nextInt(cp.getItems().size()));
            }
        }
        return availableItems.get(random.nextInt(availableItems.size()));
    }

    public void increaseRounds() {
        roundsLasted++;
        if (roundsLasted > phases.get(currentPhase).getRoundDuration() && currentPhase < phases.size()-1) {
            currentPhase++;
            if (currentPhase < phases.size()) {
                ItemShuffle.getLogger().info("New Phase");
                roundsLasted = 1;
                availableItems.addAll(phases.get(currentPhase).getItems());
            }
        }
    }
}
