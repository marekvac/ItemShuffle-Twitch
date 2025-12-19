package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.networking.GenericPayload;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

import static me.marcuscz.itemshuffle.NetworkingConstants.*;

public class ItemShufflePlayer {

    private ServerPlayer player;
    private final UUID uuid;
    private final String name;
    private Item item;
    private boolean completed;
    private int fails;
    private boolean twitchEnabled;
    private Queue<Item> itemQueue;
    private int runPoints;

    public ItemShufflePlayer(ServerPlayer player) {
        this.player = player;
        uuid = player.getUUID();
        name = player.getName().getString();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
        completed = false;
    }

    public void setTeamName(char color) {
        player.setCustomName(Component.literal("§" + color + player.getName()));
        player.setCustomNameVisible(true);
    }

    public void setItemQueue(Queue<Item> itemQueue) {
        this.itemQueue = new LinkedList<>(itemQueue);
        this.item = this.itemQueue.poll();
        completed = false;
    }

    public void resetScore() {
        runPoints = 0;
    }

    public void setPlayer(ServerPlayer player) {
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

    public int getPoints() {
        return runPoints;
    }

    public void checkItem() {
        if (completed) return;

        if (checkCompleted()) {
            if (ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD) return;

            if (ItemShuffle.getInstance().getSettings().gameType != GameType.TEAM) {
                if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN) {
                    item = itemQueue.poll();
                    sendItem();
                    runPoints++;
                    player.displayClientMessage(Component.literal("§fRun Points: §b" + runPoints), false);
                    GameManager.getInstance().getPlayerManager().refreshOtherItems(true);
                } else if (ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS) {
                    runPoints++;
                    completed = true;
                }
                else {
                    completed = true;
                    sendCompletedItem();
                    GameManager.getInstance().getPlayerManager().refreshOtherItems(true);
                }
            } else {
                sendCompletedItem();
                completed = true;
            }

            ItemShuffle.getInstance().broadcast("§aPlayer §a" + name + "§a has found their " + (ItemShuffle.getInstance().getSettings().blockMode ? "block" : "item") + "!");
        }
    }

    private boolean checkCompleted() {
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.BOARD) {
            for (Item i : GameManager.getInstance().getItemManager().getCompletableBoardItems()) {
                if (checkItem(i)) {
                    GameManager.getInstance().completeBoardItem(i, this);
                    return true;
                }
            }
            return false;
        } else {
            return checkItem(this.item);
        }

    }

    private boolean checkItem(Item item) {
        boolean c;
        if (ItemShuffle.getInstance().getSettings().blockMode) {
            c = player.getBlockStateOn().getBlock().asItem() == item;
        } else {
            c = player.getInventory().contains(new ItemStack(item));
            if (c && ItemShuffle.getInstance().getSettings().removeItems) {
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() == item, 1, player.getInventory());
            }
        }
        return c;
    }

    public void skipItem() {
        if (ItemShuffle.getInstance().getSettings().itemType != ItemGenType.RUN) return;
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TEAM) {
            ItemShuffleTeam t = GameManager.getInstance().getPlayerManager().getPlayersTeam(uuid);
            if (t == null) {
                player.displayClientMessage(Component.literal("§4You are not in team!"), false);
                return;
            }
            t.skipItem();
        } else {
            item = itemQueue.poll();
            sendItem();
            runPoints--;
            if (runPoints < 0) runPoints = 0;
            player.displayClientMessage(Component.literal("§fRun Points: §b" + runPoints), false);
            ItemShuffle.getInstance().broadcast("§7Player §f" + name + "§7 skipped their " + (ItemShuffle.getInstance().getSettings().blockMode ? "block" : "item"));
        }
    }

    public boolean failed() {
        if (completed) {
            return false;
        }
        fails++;
        ItemShuffle.getInstance().broadcast("§4Player §c" + name + "§4 failed their " + (ItemShuffle.getInstance().getSettings().blockMode ? "block" : "item") + "!");
        return true;
    }

    public void sendItem() {
        if (player == null) {
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
        ServerPlayNetworking.send(player, new GenericPayload(ITEM_MESSAGES_ID, buf));
    }

    public void sendPayload(GenericPayload payload) {
        if (player == null) return;
        ServerPlayNetworking.send(player, payload);
    }

    public void broadcastScore(boolean onlyFailed) {
        if (fails == 0 && onlyFailed) {
            return;
        }
        String score = (fails == 0 ? "§2 " : "§c ") + fails + " fails";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }

    public void broadcastRunScore() {
        String score = (runPoints == 0 ? "§c " : "§b ") + runPoints + " points";
        ItemShuffle.getInstance().broadcast("§f" + name + "§7: " + score);
    }

    public void giveFood() {
        if (player == null || player.getInventory().contains(new ItemStack(Items.COOKED_BEEF))) {
            return;
        }
        player.addItem(new ItemStack(Items.COOKED_BEEF, 32));
    }

    public void updateTimer(int color) {
        if (player == null) {
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(GameManager.getCurrentTime());
        buf.writeInt(GameManager.getTime());
        buf.writeInt(color);
        ServerPlayNetworking.send(player, new GenericPayload(TIMER_SHOW_ID, buf));
//        ServerPlayNetworking.send(player, TIMER_SHOW, buf);
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

    public static void hideTimerPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new GenericPayload(TIMER_HIDE_ID, PacketByteBufs.empty()));
    }

    public static void sendGameStopped(ServerPlayer player) {
        ServerPlayNetworking.send(player, new GenericPayload(GAME_STOP_ID, PacketByteBufs.empty()));
    }

    public static void showItemPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new GenericPayload(SHOW_ITEM_ID, PacketByteBufs.empty()));
    }

    public static void hideItemPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new GenericPayload(HIDE_ITEM_ID, PacketByteBufs.empty()));
    }

    public void sendCompletedItem() {
        ServerPlayNetworking.send(player, new GenericPayload(COMPLETE_ITEM_ID, PacketByteBufs.empty()));
    }

    // TWITCH CLIENT

    public void startVotingClient() {
        ServerPlayNetworking.send(player, new GenericPayload(VOTING_INIT_ID, PacketByteBufs.empty()));
    }

    public void setTwitchEnabled(boolean twitchEnabled) {
        this.twitchEnabled = twitchEnabled;
    }

    public void createNewVoting(List<Item> itemList) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        int[] ids = new int[itemList.size()];
        int i = 0;
        for (Item item1 : itemList) {
            ids[i] = BuiltInRegistries.ITEM.getId(item1);
            i++;
        }
        buf.writeVarIntArray(ids);
        ServerPlayNetworking.send(player, new GenericPayload(NEXT_ROUND_ID, buf));
    }

    public void askClientForWinner() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, new GenericPayload(VOTING_GET_WINNER_ID, PacketByteBufs.empty()));
    }

    public void pauseVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, new GenericPayload(GAME_PAUSED_ID, PacketByteBufs.empty()));
    }

    public void resumeVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, new GenericPayload(GAME_RESUMED_ID, PacketByteBufs.empty()));
    }

    public void stopVotingClient() {
        if (!twitchEnabled) {
            return;
        }
        ServerPlayNetworking.send(player, new GenericPayload(GAME_STOP_ID, PacketByteBufs.empty()));
    }

    public void sendBoard(int size, List<Item> board) {
        ItemShuffle.getLogger().info("pre send board");
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(size);
        for (Item item : board) {
//            buf.writeItemStack(new ItemStack(item));
            buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
        }
        ServerPlayNetworking.send(player, new GenericPayload(INIT_BOARD_ID, buf));
        ItemShuffle.getLogger().info("send board");
    }

    public void sendBoardStatus(List<Character> status) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        for (Character character : status) {
            if (character == null) buf.writeChar('\0');
            else buf.writeChar(character);
        }
        ServerPlayNetworking.send(player, new GenericPayload(BOARD_SET_STATUS_ID, buf));
    }

    public void hideBoard() {
        ServerPlayNetworking.send(player, new GenericPayload(HIDE_BOARD_ID, PacketByteBufs.empty()));
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return name;
    }
}
