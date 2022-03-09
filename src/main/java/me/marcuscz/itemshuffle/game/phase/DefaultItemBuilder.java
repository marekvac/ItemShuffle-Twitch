package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultItemBuilder {

    private final List<ItemPhase> phases = new ArrayList<>();

    public DefaultItemBuilder() {

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
    }

    public void saveJson() throws IOException {
        int i = 1;
        JSONArray jsonPhases = new JSONArray();
        for (ItemPhase phase : phases) {
            JSONObject jsonPhase = new JSONObject();
            jsonPhase.put("phase", i);
            jsonPhase.put("rounds", phase.getRoundDuration());
            JSONArray items = new JSONArray();
            for (PhaseItem item : phase.getItems()) {
                items.add(Registry.ITEM.getId(item.getItem()).toString());
            }
            jsonPhase.put("items", items);
            jsonPhases.add(jsonPhase);
            i++;
        }
        FileWriter file = new FileWriter("./config/itemshuffle/phases.json");
        file.write(jsonPhases.toJSONString());
        file.flush();
    }

}
