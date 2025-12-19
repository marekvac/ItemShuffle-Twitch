package me.marcuscz.itemshuffle.client.voting;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.floor;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.world.item.Item;

public class VotingItem {

    private int votes;
    private final String name;
    private final Item item;

    public VotingItem(@NotNull Item item) {
        this.item = item;
        this.name = ClientLanguage.getInstance().getOrDefault(item.getDescriptionId());
    }

    public int getVotes() {
        return votes;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item;
    }

    public void increaseVotes() {
        votes++;
    }

    public double getRatio() {
        if (VotingClient.getTotalVotes() == 0) {
            return 0;
        }
        return (double) votes / VotingClient.getTotalVotes();
    }
}
