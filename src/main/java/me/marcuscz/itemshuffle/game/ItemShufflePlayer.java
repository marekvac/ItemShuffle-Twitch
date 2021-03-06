package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.UUID;

import static me.marcuscz.itemshuffle.NetworkingConstants.*;

public class ItemShufflePlayer {

    private ServerPlayerEntity player;
    private final UUID uuid;
    private final String name;
    private Item item;
    private boolean completed;
    private int fails;
    private boolean twitchEnabled;

    public ItemShufflePlayer(ServerPlayerEntity player) {
        this.player = player;
        uuid = player.getUuid();
        name = player.getName().asString();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
        completed = false;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public boolean isOnline() {
        return this.player != null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void checkItem() {
        if (!completed && player.getInventory().contains(new ItemStack(item))) {
            completed = true;
            if (ItemShuffle.getInstance().getSettings().removeItems) {
                player.getInventory().remove(itemStack -> itemStack.getItem() == item, 1, player.getInventory());
            }
            ItemShuffle.getInstance().broadcast("§aPlayer §a" + name + "§a has found their item!");
            sendCompletedItem();
        }
    }

    public boolean failed() {
        if (completed) {
            return false;
        }
        fails++;
        ItemShuffle.getInstance().broadcast("§4Player §c" + name + "§4 failed their item!");
        return true;
    }

    public void sendItem() {
        if (player == null) {
            return;
        }
//        String key = item.getTranslationKey();
        PacketByteBuf buf = PacketByteBufs.create();
//        buf.writeString(key);
        buf.writeItemStack(new ItemStack(item));
        ServerPlayNetworking.send(player, ITEM_MESSAGES, buf);
    }

    public void broadcastScore(boolean onlyFailed) {
        if (fails == 0 && onlyFailed) {
            return;
        }
        String score = (fails == 0 ? "§2 " : "§c ") + fails + " fails";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }

    public void giveFood() {
        if (player == null || player.getInventory().contains(new ItemStack(Items.COOKED_BEEF))) {
            return;
        }
        player.giveItemStack(new ItemStack(Items.COOKED_BEEF, 32));
    }

    public void updateTimer(int color) {
        if (player == null) {
            return;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(GameManager.getCurrentTime());
        buf.writeInt(GameManager.getTime());
        buf.writeInt(color);
        ServerPlayNetworking.send(player, TIMER_SHOW, buf);
    }

    public void hideTimer() {
        if (player == null) {
            return;
        }
        hideTimerPlayer(player);
    }

    public void showItem() {
        if (player == null) {
            return;
        }
        showItemPlayer(player);
    }

    public void hideItem() {
        if (player == null) {
            return;
        }
        hideItemPlayer(player);
    }

    public static void hideTimerPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, TIMER_HIDE, PacketByteBufs.empty());
    }

    public static void sendGameStopped(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, GAME_STOP, PacketByteBufs.empty());
    }

    public static void showItemPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, SHOW_ITEM, PacketByteBufs.empty());
    }

    public static void hideItemPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, HIDE_ITEM, PacketByteBufs.empty());
    }

    public void sendCompletedItem() {
        ServerPlayNetworking.send(player, COMPLETE_ITEM, PacketByteBufs.empty());
    }

    // TWITCH CLIENT

    public void startVotingClient() {
        ServerPlayNetworking.send(player, VOTING_INIT, PacketByteBufs.empty());
    }

    public void setTwitchEnabled(boolean twitchEnabled) {
        this.twitchEnabled = twitchEnabled;
    }

    public void createNewVoting(List<Item> itemList) {
        PacketByteBuf buf = PacketByteBufs.create();
        int[] ids = new int[itemList.size()];
        int i = 0;
        for (Item item1 : itemList) {
            ids[i] = Registry.ITEM.getRawId(item1);
            i++;
        }
        buf.writeIntArray(ids);
        ServerPlayNetworking.send(player, NEXT_ROUND, buf);
    }

    public void askClientForWinner() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, VOTING_GET_WINNER, PacketByteBufs.empty());
    }

    public void pauseVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, GAME_PAUSED, PacketByteBufs.empty());
    }

    public void resumeVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, GAME_RESUMED, PacketByteBufs.empty());
    }

    public void stopVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, GAME_STOP, PacketByteBufs.empty());
    }

    @Override
    public String toString() {
        return name;
    }
}
