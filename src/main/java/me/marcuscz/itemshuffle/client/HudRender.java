package me.marcuscz.itemshuffle.client;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import me.marcuscz.itemshuffle.client.voting.VotingClient;
import me.marcuscz.itemshuffle.client.voting.VotingItem;
import me.marcuscz.itemshuffle.game.GameType;
import me.marcuscz.itemshuffle.game.ItemGenType;
import me.marcuscz.itemshuffle.game.ItemShowType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HudRender {

    public static Identifier background = Identifier.parse("advancements/task_frame_unobtained");
    public static Identifier slot = Identifier.parse("container/slot");
    private boolean showTimer;
    private boolean showItem;
    private int time;
    private int currentTime;
    private int color;
    private ItemStack itemStack;
    private HashMap<String, Tuple<Item,Integer>> items;
    private boolean isItemCompleted;
    private final Minecraft minecraftClient = Minecraft.getInstance();
    private boolean showVotes = false;
    private final ArrayList<TeamData> teamData = new ArrayList<>();
    private boolean showTeamData = false;
    private boolean showOtherItems = false;
    public BoardRender boardRender;

    public void initBoard(FriendlyByteBuf buf) {
        int size = buf.readInt();
        boardRender = new BoardRender(size);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < size*size; i++) {
            items.add(BuiltInRegistries.ITEM.getValue(buf.readIdentifier()));
//            items.add(buf.readItemStack().getItem());
        }
        boardRender.setItems(items);
    }

    public void hideBoard() {
        boardRender = null;
    }

    public void setBoardStatus(FriendlyByteBuf buf) {
        List<Character> status = new ArrayList<>();
        for (int i = 0; i < boardRender.size*boardRender.size; i++) {
            Character c = buf.readChar();
            if (c == '\0') c = null;
            status.add(c);
        }
        boardRender.status = status;
    }

    public void renderBoard(GuiGraphics context) {
        if (boardRender == null) return;
        boardRender.draw(context);
    }

    public void renderItemShuffleHUD(GuiGraphics context, DeltaTracker renderTickCounter) {
        renderBoard(context);
        renderVoting(context);
        if (showItem) {
            if (ItemShuffle.getInstance().getSettings().gameType != GameType.TEAM && showOtherItems && minecraftClient.options.keyPlayerList.isDown()) {
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

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int width1 = Mth.floor(getProgress() * width);
        context.fill(0, 0, width, 10, ARGB.color(80,0,0, 0));
        context.fill(0, 0, width1, 10, color);

    }

    private void renderVoting(GuiGraphics context) {
        if (!showVotes || !ItemShuffleClient.getInstance().votingEnabled || ItemShuffleClient.getInstance().getVotingClient().isPaused() || ItemShuffleClient.getInstance().getVotingClient().getItems().isEmpty()) {
            return;
        }

        context.drawString(minecraftClient.font, Component.literal("Total votes: " + VotingClient.getTotalVotes()), 10, 20, ARGB.color(255,255,255,255));

        List<VotingItem> items = new ArrayList<>(ItemShuffleClient.getInstance().getVotingClient().getItems());

        int i = 0;
        int altOffset = ItemShuffleClient.getInstance().getVotingClient().getVoteID() % 2 == 0 ? 4 : 0;
        for (VotingItem item : items) {
            double ratio = item.getRatio();

            context.fill(10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, ARGB.color(150,155,22,217));
            context.fill(10, 31 + (i * 18), 10 + Mth.floor((195 + 45) * ratio), (35 + (i * 18) + 10), ARGB.color(150,100,65,165));
            context.drawString(minecraftClient.font, Component.literal((1 + i + altOffset) + ": ").append(item.getName()), 15, 34 + (i * 18), ARGB.color(255,255,255,255));

            Component percentage = Component.literal(Mth.floor(ratio * 100) + " %");
            context.drawString(minecraftClient.font, percentage, 195 + 10 + 42 - minecraftClient.font.width(percentage), 34 + (i * 18), ARGB.color(255,255,255,255));

            i++;
        }

    }

    private void renderTeams(GuiGraphics context) {
        if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.NONE) return;

        context.drawString(minecraftClient.font, Component.literal("Team Score: §7§o(Mode " + ItemShuffle.getInstance().getSettings().itemType.name() + ")"), 10, 20, ARGB.color(255,255,255,255));

        for (int i = 0; i < teamData.size(); i++) {
            context.fill(10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, ARGB.color(70,0,0));
            TeamData data;
            try {
                data = teamData.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            String score = ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN || ItemShuffle.getInstance().getSettings().itemType == ItemGenType.ALL_SAME_VS ? "§b§l" + data.runScore : "§c§l" + data.fails;
            String item = "";
            if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.FULL) {
                String name = ClientLanguage.getInstance().getOrDefault(data.item.getDescriptionId());
                item = data.completed ? "§a " + name + " §r§2✔" : "§f " + name;
            }

            context.drawString(minecraftClient.font, Component.literal("§" + data.color + data.name + "§r §7[" + data.players + "]§r: " + score + item), 15, 34 + (i * 18), ARGB.color(255,255,255,255));

        }
    }

    private void renderItem(GuiGraphics context) {
        if (itemStack == null) {
            return;
        }

        ItemShowType mode = ItemShuffle.getInstance().getSettings().showItemsMode;
        if (mode == ItemShowType.TEXT_ONLY || mode == ItemShowType.FULL) {
            String name = ClientLanguage.getInstance().getOrDefault(itemStack.getItem().getDescriptionId());
            Component text;
            String i = ItemShuffle.getInstance().getSettings().blockMode ? "block" : "item";
            if (isItemCompleted) {
                text = Component.literal("§7Your " + i + ": §a§l" + name + " §r§2✔");
            } else {
                text = Component.literal("§7Your " + i + ": §f§l" + name);
            }
            int x = mode == ItemShowType.FULL ? 29 : 10;
            context.drawString(minecraftClient.font, text, x, 108, ARGB.color(255,255,255,255));
        }

        if (mode == ItemShowType.ICON_ONLY || mode == ItemShowType.FULL) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, background, 2,  100, 26, 26);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, slot, 6,  104, 18, 18);
            context.renderItem(itemStack, 7, 105);
            if (isItemCompleted) {
                context.drawString(minecraftClient.font, Component.literal("§2✔"), 17, 115, ARGB.color(255,255,255,255));
            }
        }
    }

    private void renderOtherItems(GuiGraphics context) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String i = ItemShuffle.getInstance().getSettings().blockMode ? "blocks" : "items";
        context.fill(10, 103, 195 + 10 + 45, 106 + (10* items.size()) +10, ARGB.color(70,0,0,0));
        context.drawString(minecraftClient.font, Component.literal("§7§oOther player " + i + ":"), 15, 106, ARGB.color(255,255,255,255));
        AtomicInteger y = new AtomicInteger(116);
        items.forEach((player, pair) -> {
            String name = ClientLanguage.getInstance().getOrDefault(pair.getA().getDescriptionId());
            Component text;
            if (pair.getA() != Items.AIR) {
                if (pair.getB() > 0) {
                    text = Component.literal("§7" + player + ": §a" + name + " §r§2✔");
                } else {
                    text = Component.literal("§7" + player + ": §f" + name);
                }
            } else {
                text = Component.literal("§7" + player + ": §b" + pair.getB());
            }
            context.drawString(minecraftClient.font, text, 15, y.getAndAdd(10), ARGB.color(255,255,255,255));
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

    public void setItem(ItemStack item) {
        this.itemStack = item;
    }

    public void setItemCompleted(boolean itemCompleted) {
        isItemCompleted = itemCompleted;
    }

    public void setTeamData(FriendlyByteBuf buf) {
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

    public void setOtherItems(FriendlyByteBuf buf) {
        showOtherItems = false;
        int size = buf.readInt();
        if (size == 0) {
            items = null;
            return;
        }
        items = new HashMap<>();
        for (int i = 0; i < size; i++) {
            items.put(buf.readUtf(), new Tuple<>(BuiltInRegistries.ITEM.getValue(buf.readIdentifier()), buf.readInt()));
        }
        showOtherItems = true;
    }

    public void setShowTeamData(boolean showTeamData) {
        this.showTeamData = showTeamData;
    }

    public void tick(Minecraft minecraftClient) {

        if (!showTimer) {
            return;
        }

        currentTime--;
    }
}
