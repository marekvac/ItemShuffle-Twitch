package me.marcuscz.itemshuffle.game.phase;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
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

        if (!f.getParentFile().getParentFile().exists()) {
            f.getParentFile().getParentFile().mkdir();
        }

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdir();
        }

        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("itemshuffle-twitch");
        if (container.isEmpty()) {
            throw new FileNotFoundException("failed to get mod container");
        }

        if (!f.exists()) {
            copyModFile(container);
        }

        File modFile = container.get().getPath("phases-" + ItemShuffle.MC_VERSION + ".json").toFile();

        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(f);
        FileReader readerMod = new FileReader(modFile);

        JSONObject obj = (JSONObject) jsonParser.parse(reader);
        if (!obj.containsKey("version")) {
            reader.close();
            copyModFile(container);
            reader = new FileReader(f);
            obj = (JSONObject) jsonParser.parse(reader);
            ItemShuffle.getLogger().info("Updated phases.json file!");
        } else {
            ItemShuffle.getLogger().info("File is in new format");
            JSONObject objMod = (JSONObject) jsonParser.parse(readerMod);
            JSONObject version = (JSONObject) obj.get("version");
            JSONObject modVersion = (JSONObject) objMod.get("version");
            if (!((boolean) version.get("custom")) && ((double) version.get("file")) < ((double) modVersion.get("file"))) {
                reader.close();
                copyModFile(container);
                reader = new FileReader(f);
                obj = (JSONObject) jsonParser.parse(reader);
                ItemShuffle.getLogger().info("Updated phases.json file!");
            }
        }

        JSONArray phasesArray = (JSONArray) obj.get("phases");
        phasesArray.forEach(ph -> this.parsePhase((JSONObject) ph));

        currentPhase = 0;
        availableItems.addAll(phases.get(currentPhase).getItems());

        ItemShuffle.getLogger().info("Phases initialized!");

        roundsLasted = 0;
    }

    private void copyModFile(Optional<ModContainer> container) throws IOException {

        File f = ItemShuffle.getPhasesFile();
        OutputStream os = new FileOutputStream(f);
        Files.copy(container.get().getPath("phases-" + ItemShuffle.MC_VERSION + ".json"), os);
        os.close();
    }

    private void parsePhase(JSONObject phase) throws ClassCastException {
        Long rounds = (Long) phase.get("rounds");
        JSONArray items = (JSONArray) phase.get("items");
        ItemPhaseBuilder builder = new ItemPhaseBuilder();
        items.forEach(i -> {
            Identifier id = new Identifier((String) i);
            Item item = Registries.ITEM.get(id);
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
