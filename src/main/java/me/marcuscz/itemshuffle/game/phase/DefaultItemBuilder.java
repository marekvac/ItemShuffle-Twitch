package me.marcuscz.itemshuffle.game.phase;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DefaultItemBuilder {

    private final List<ItemPhase> phases = new ArrayList<>();

    public DefaultItemBuilder() {

        List<Item> phase1 = new ItemPhaseBuilder()
                .addLike("oak")
                .addLike("birch")
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
                .not("green")
                .not("brown")
                .not("petrified")
                .addItems(
                        Items.CRAFTING_TABLE,
                        Items.FURNACE,
                        Items.CHEST,
                        Items.STONE_BRICKS,
                        Items.STONE_BUTTON,
                        Items.HAY_BLOCK,
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
                        Items.CHICKEN,
                        Items.COOKED_MUTTON,
                        Items.STONE,
                        Items.GRASS_BLOCK,
                        Items.DIRT,
                        Items.WATER_BUCKET
                )
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase1));

        List<Item> phase2 = new ItemPhaseBuilder()
                .addLike("spruce")
                .addLike("acacia")
                .addLike("green")
                .addItems(
                        Items.ANVIL,
                        Items.IRON_BLOCK,
                        Items.GOLD_BLOCK,
                        Items.BELL,
                        Items.JUKEBOX,
                        Items.PISTON,
                        Items.REDSTONE_BLOCK,
                        Items.REPEATER,
                        Items.TNT,
                        Items.MINECART,
                        Items.CAMPFIRE,
                        Items.CLOCK,
                        Items.BOOK,
                        Items.BEETROOT,
                        Items.MUSHROOM_STEW,
                        Items.BROWN_MUSHROOM,
                        Items.RED_MUSHROOM,
                        Items.LAVA_BUCKET
                )
                .getItems();
        phases.add(new ItemPhase(2).setItems(phase2));

        // Nether phase
        List<Item> phase3 = new ItemPhaseBuilder()
                .addItems(
                        Items.CRIMSON_DOOR,
                        Items.CRIMSON_BUTTON,
                        Items.CRIMSON_FUNGUS,
                        Items.BASALT,
                        Items.SMOOTH_BASALT,
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
                items.add(Registries.ITEM.getId(item.getItem()).toString());
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
