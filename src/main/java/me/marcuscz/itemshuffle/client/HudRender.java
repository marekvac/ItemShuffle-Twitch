package me.marcuscz.itemshuffle.client;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import me.marcuscz.itemshuffle.client.voting.VotingClient;
import me.marcuscz.itemshuffle.client.voting.VotingItem;
import me.marcuscz.itemshuffle.game.GameType;
import me.marcuscz.itemshuffle.game.ItemGenType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HudRender {

    private boolean showTimer;
    private boolean showItem;
    private int time;
    private int currentTime;
    private int color;
    private Item item;
    private HashMap<String, Pair<Item,Integer>> items;
    private boolean isItemCompleted;
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private boolean showVotes = false;
    private ArrayList<TeamData> teamData = new ArrayList<>();
    private boolean showTeamData = false;
    private boolean showOtherItems = false;

    public void renderTimer(DrawContext context, float tickDelta) {
        renderVoting(context);
        if (showItem) {
            if (ItemShuffle.getInstance().getSettings().gameType != GameType.TEAM && showOtherItems && minecraftClient.options.playerListKey.isPressed()) {
                renderOtherItems(context);
            } else {
                renderItem(context);
            }
        }
        if (showTeamData) {
            renderTeams(context);
        }
        if (!showTimer) {
            return;
        }

        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int width1 = MathHelper.floor(getProgress() * width);
        context.fill(0, 0, width, 10, ColorHelper.Argb.getArgb(80,0,0,0));
        context.fill(0, 0, width1, 10, color);

    }

    private void renderVoting(DrawContext context) {
        if (!showVotes || !ItemShuffleClient.getInstance().votingEnabled || ItemShuffleClient.getInstance().getVotingClient().isPaused() || ItemShuffleClient.getInstance().getVotingClient().getItems().size() == 0) {
            return;
        }

        context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal("Total votes: " + VotingClient.getTotalVotes()), 10, 20, ColorHelper.Argb.getArgb(255,255,255,255));

        List<VotingItem> items = new ArrayList<>(ItemShuffleClient.getInstance().getVotingClient().getItems());

        int i = 0;
        int altOffset = ItemShuffleClient.getInstance().getVotingClient().getVoteID() % 2 == 0 ? 4 : 0;
        for (VotingItem item : items) {
            double ratio = item.getRatio();

            context.fill(10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, ColorHelper.Argb.getArgb(150,155,22,217));
            context.fill(10, 31 + (i * 18), 10 + MathHelper.floor((195 + 45) * ratio), (35 + (i * 18) + 10), ColorHelper.Argb.getArgb(150,100,65,165));
            context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal((1 + i + altOffset) + ": ").append(item.getName()), 15, 34 + (i * 18), ColorHelper.Argb.getArgb(255,255,255,255));

            Text percentage = Text.literal(MathHelper.floor(ratio * 100) + " %");
            context.drawTextWithShadow(minecraftClient.textRenderer, percentage, 195 + 10 + 42 - minecraftClient.textRenderer.getWidth(percentage), 34 + (i * 18), ColorHelper.Argb.getArgb(255,255,255,255));

            i++;
        }

    }

    private void renderTeams(DrawContext context) {
        if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.NONE) return;

        context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal("Team Score: §7§o(Mode " + ItemShuffle.getInstance().getSettings().itemType.name() + ")"), 10, 20, ColorHelper.Argb.getArgb(255,255,255,255));

        for (int i = 0; i < teamData.size(); i++) {
            context.fill(10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, ColorHelper.Argb.getArgb(70,0,0,0));
            TeamData data;
            try {
                data = teamData.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            String score = ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS ? "§b§l" + data.runScore : "§c§l" + data.fails;
            String item = "";
            if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.FULL) {
                String name = TranslationStorage.getInstance().get(data.item.getTranslationKey());
                item = data.completed ? "§a " + name + " §r§2✔" : "§f " + name;
            }

            context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal("§" + data.color + data.name + "§r §7[" + data.players + "]§r: " + score + item), 15, 34 + (i * 18), ColorHelper.Argb.getArgb(255,255,255,255));

        }
    }

    private void renderItem(DrawContext context) {
        if (item == null) {
            return;
        }
        String name = TranslationStorage.getInstance().get(item.getTranslationKey());
        Text text;
        String i = ItemShuffle.getInstance().getSettings().blockMode ? "block" : "item";
        if (isItemCompleted) {
            text = Text.literal("§7Your " + i + ": §a§l" + name + " §r§2✔");
        } else {
            text = Text.literal("§7Your " + i + ": §f§l" + name);
        }
        context.drawTextWithShadow(minecraftClient.textRenderer, text, 10, 106, ColorHelper.Argb.getArgb(255,255,255,255));
    }

    private void renderOtherItems(DrawContext context) {
        if (items == null || items.size() == 0) {
            return;
        }
        String i = ItemShuffle.getInstance().getSettings().blockMode ? "blocks" : "items";
        context.fill(10, 103, 195 + 10 + 45, 106 + (10* items.size()) +10, ColorHelper.Argb.getArgb(70,0,0,0));
        context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal("§7§oOther player " + i + ":"), 15, 106, ColorHelper.Argb.getArgb(255,255,255,255));
        AtomicInteger y = new AtomicInteger(116);
        items.forEach((player, pair) -> {
            String name = TranslationStorage.getInstance().get(pair.getLeft().getTranslationKey());
            Text text;
            if (pair.getLeft() != Items.AIR) {
                if (pair.getRight() > 0) {
                    text = Text.literal("§7" + player + ": §a" + name + " §r§2✔");
                } else {
                    text = Text.literal("§7" + player + ": §f" + name);
                }
            } else {
                text = Text.literal("§7" + player + ": §b" + pair.getRight());
            }
            context.drawTextWithShadow(minecraftClient.textRenderer, text, 15, y.getAndAdd(10), ColorHelper.Argb.getArgb(255,255,255,255));
        });
    }

    private double getProgress() {
        double dTime = time;
        double timeElapsed = time - currentTime;
        return (timeElapsed / dTime);
    }

    public void showTimer(boolean showTimer) {
        this.showTimer = showTimer;
    }

    public void showItem(boolean showItem) {
        this.showItem = showItem;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void showVotes(boolean showVotes) {
        this.showVotes = showVotes;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setItemCompleted(boolean itemCompleted) {
        isItemCompleted = itemCompleted;
    }

    public void setTeamData(PacketByteBuf buf) {
        int size = buf.readInt();
        if (size == 0) {
            showTeamData = false;
            return;
        }
        teamData.clear();
        for (int i = 0; i < size; i++) {
            teamData.add(TeamData.fromPacket(buf));
        }
        showTeamData = true;
    }

    public void setOtherItems(PacketByteBuf buf) {
        showOtherItems = false;
        int size = buf.readInt();
        if (size == 0) {
            items = null;
            return;
        }
        items = new HashMap<>();
        for (int i = 0; i < size; i++) {
            items.put(buf.readString(), new Pair<>(buf.readItemStack().getItem(), buf.readInt()));
        }
        showOtherItems = true;
    }

    public void setShowTeamData(boolean showTeamData) {
        this.showTeamData = showTeamData;
    }

    public void tick(MinecraftClient minecraftClient) {

        if (!showTimer) {
            return;
        }

        currentTime--;
    }
}
