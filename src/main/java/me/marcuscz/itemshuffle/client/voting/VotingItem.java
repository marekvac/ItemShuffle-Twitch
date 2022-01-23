package me.marcuscz.itemshuffle.client.voting;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.floor;

public class VotingItem {

    private int votes;
    private final String name;
    private final Item item;

    public VotingItem(@NotNull Item item) {
        this.item = item;
        this.name = TranslationStorage.getInstance().get(item.getTranslationKey());
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
