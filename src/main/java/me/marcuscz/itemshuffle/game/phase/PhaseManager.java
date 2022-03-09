package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PhaseManager {

    private double roundsLasted;
    private int currentPhase;
    private final List<ItemPhase> phases;
    private final List<PhaseItem> availableItems;

    public PhaseManager() throws IOException, ParseException {
        phases = new ArrayList<>();
        availableItems = new ArrayList<>();

        File f = new File("./config/itemshuffle/phases.json");
        if (!f.exists()) {
            DefaultItemBuilder builder = new DefaultItemBuilder();
            builder.saveJson();
        }

        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("./config/itemshuffle/phases.json");
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
        Long num = (Long) phase.get("phase");
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
//        List<PhaseItem> items = this.availableItems.stream().toList();
//        items.sort(Comparator.comparingInt(PhaseItem::getChance));
        ItemShuffle.getLogger().info(availableItems);
    }

    public void increaseRounds(double by) {
        roundsLasted += by;
        if (roundsLasted > phases.get(currentPhase).getRoundDuration() && currentPhase < phases.size()-1) {
            currentPhase++;
            if (currentPhase < phases.size()) {
                ItemPhase previous = phases.get(currentPhase-1);
                previous.increaseAllItems(30);
                availableItems.addAll(phases.get(currentPhase).getItems());
                roundsLasted = 1;
//                ItemShuffle.getLogger().info(availableItems);
                ItemShuffle.getLogger().info("New Phase");
                ItemShuffle.getInstance().broadcast("§bNew phase!");
            }
        }
    }
}
