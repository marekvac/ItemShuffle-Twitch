package me.marcuscz.itemshuffle.client.voting;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VotingClient {

    private final List<VotingItem> items;
    private int voteID;
    private boolean enabled;
    private boolean paused;
    private final TwitchClient twitchClient;
    private static int totalVotes;

    public VotingClient() throws IOException, ParseException {
        items = new ArrayList<>();

        // Start twitch bot
        twitchClient = new TwitchClient(this);

        voteID = 0;
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

    private void setItems(int[] ids) {
        items.clear();
        for (int id : ids) {
            Item item = Registry.ITEM.get(id);
            items.add(new VotingItem(item));
        }
    }

    public void nextVote(int[] ids) {
        paused = true;
        this.setItems(ids);
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

    public boolean isPaused() {
        return paused;
    }
}
