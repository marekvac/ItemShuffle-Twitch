package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhaseManager {

    private double roundsLasted;
    private int currentPhase;
    private final List<ItemPhase> phases;
    private final List<PhaseItem> availableItems;

    public PhaseManager() throws IOException, ParseException {
        phases = new ArrayList<>();
        availableItems = new ArrayList<>();

        File f = ItemShuffle.getPhasesFile();

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdir();
        }

        if (!f.exists()) {
            Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("itemshuffle-twitch");
            if (container.isEmpty()) {
                throw new FileNotFoundException("failed to get mod container");
            }
            OutputStream os = new FileOutputStream(f);
            Files.copy(container.get().getPath("phases.json"), os);
            os.close();
        }

        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(f);
        Object obj = jsonParser.parse(reader);
        JSONArray phasesArray = (JSONArray) obj;
        phasesArray.forEach(ph -> this.parsePhase((JSONObject) ph));


        currentPhase = 0;
        availableItems.addAll(phases.get(currentPhase).getItems());

        ItemShuffle.getLogger().info("Phases initialized!");
//        ItemShuffle.getLogger().info("Phases: " + phases);

        roundsLasted = 0;
    }

    private void parsePhase(JSONObject phase) throws ClassCastException {
        Long rounds = (Long) phase.get("rounds");
        JSONArray items = (JSONArray) phase.get("items");
        ItemPhaseBuilder builder = new ItemPhaseBuilder();
        items.forEach(i -> {
            Identifier id = new Identifier((String) i);
            Item item = Registry.ITEM.get(id);
            builder.addItem(item);
        });
        ItemPhase itemPhase = new ItemPhase(rounds.intValue());
        itemPhase.setItems(builder.getItems());
        this.phases.add(itemPhase);
    }

    public List<Item> getVotingItems(int size) {
        return ItemPhase.getItems(availableItems, size);
    }

    public Item getItem() {
        return ItemPhase.getItem(availableItems);
    }

    public void printItems() {
        ItemShuffle.getLogger().info(availableItems);
    }

    public void increaseRounds(double by) {
        roundsLasted += by;
        if (roundsLasted > phases.get(currentPhase).getRoundDuration() && currentPhase < phases.size()-1) {
            currentPhase++;
            if (currentPhase < phases.size()) {
                ItemPhase previous = phases.get(currentPhase-1);
                previous.increaseAllItems(40);
                availableItems.addAll(phases.get(currentPhase).getItems());
                roundsLasted = 1;
                ItemShuffle.getLogger().info("New Phase");
            }
        }
    }
}
