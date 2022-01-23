package me.marcuscz.itemshuffle.client.voting;

import me.marcuscz.itemshuffle.game.ItemManager;
import net.minecraft.item.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VotingClient {

    private final int size = 4;
    private final List<VotingItem> items;
    private int voteID;
    private boolean enabled;
    private boolean paused;
    private final TwitchClient twitchClient;
    private static int totalVotes;
    private final ItemManager itemManager;

    public VotingClient() throws IOException {
        items = new ArrayList<>();

        // Start twitch bot
        twitchClient = new TwitchClient(this);

        voteID = 0;
        itemManager = new ItemManager();
    }

    public void processVote(String message) {
        if (enabled && !paused && message.trim().length() == 1) {
            int voteIndex = Integer.parseInt(message.trim()) + (voteID % 2 == 0 ? -4 : 0);
            if (voteIndex > 0 && voteIndex < 5 && items.get(voteIndex-1) != null) {
                items.get(voteIndex-1).increaseVotes();
                totalVotes++;
            }
        }
    }

    private void refreshItems() {
        items.clear();
        List<Item> randomItems;
        randomItems = itemManager.getRandomItemList(size);
        randomItems.forEach(item -> items.add(new VotingItem(item)));
    }

    public void nextVote() {
        paused = true;
        refreshItems();
        totalVotes = 0;
        voteID++;
        paused = false;
    }

    public static int getTotalVotes() {
        return totalVotes;
    }

    public void resumeVoting() {
        if (enabled) {
            paused = false;
        }
    }

    public void pauseVoting() {
        if (enabled) {
            paused = true;
        }
    }

    public VotingItem getWinner() {
        if (items.size() == 0) {
            return null;
        }

        int votes = items.get(0).getVotes();
        VotingItem winner = items.get(0);

        for (VotingItem item : items) {
            if (item.getVotes() > votes) {
                winner = item;
                votes = item.getVotes();
            }
        }

        return winner;
    }

    public void stop() {
        twitchClient.stop();
        enabled = false;
    }

    public List<VotingItem> getItems() {
        return items;
    }

    public int getVoteID() {
        return voteID;
    }

    public void enableVoting() {
        enabled = true;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}
