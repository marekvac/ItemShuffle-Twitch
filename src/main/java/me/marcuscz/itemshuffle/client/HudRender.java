package me.marcuscz.itemshuffle.client;

import me.marcuscz.itemshuffle.ItemShuffle;
import me.marcuscz.itemshuffle.TeamData;
import me.marcuscz.itemshuffle.client.voting.VotingClient;
import me.marcuscz.itemshuffle.client.voting.VotingItem;
import me.marcuscz.itemshuffle.game.GameType;
import me.marcuscz.itemshuffle.game.ItemGenType;
import me.marcuscz.itemshuffle.game.ItemShuffleTeam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class HudRender {

    private boolean showTimer;
    private boolean showItem;
    private int time;
    private int currentTime;
    private int color;
    private Item item;
    private boolean isItemCompleted;
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private boolean showVotes = false;
    private ArrayList<TeamData> teamData = new ArrayList<>();
    private boolean showTeamData = false;

    public void renderTimer(MatrixStack matrixStack, float tickDelta) {
        renderVoting(matrixStack);
        if (showItem) {
            renderItem(matrixStack);
        }
        if (showTeamData) {
            renderTeams(matrixStack);
        }
        if (!showTimer) {
            return;
        }

        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int width1 = MathHelper.floor(getProgress() * width);
        DrawableHelper.fill(matrixStack, 0, 0, width, 10, 150 << 24);
        DrawableHelper.fill(matrixStack, 0, 0, width1, 10, color + (255 << 24));

    }

    private void renderVoting(MatrixStack matrixStack) {
        if (!showVotes || !ItemShuffleClient.getInstance().votingEnabled || ItemShuffleClient.getInstance().getVotingClient().isPaused() || ItemShuffleClient.getInstance().getVotingClient().getItems().size() == 0) {
            return;
        }

        DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, new LiteralText("Total votes: " + VotingClient.getTotalVotes()), 10, 20, MathHelper.packRgb(255, 255, 255));

        List<VotingItem> items = new ArrayList<>(ItemShuffleClient.getInstance().getVotingClient().getItems());

        int i = 0;
        int altOffset = ItemShuffleClient.getInstance().getVotingClient().getVoteID() % 2 == 0 ? 4 : 0;
        for (VotingItem item : items) {
            double ratio = item.getRatio();

            DrawableHelper.fill(matrixStack, 10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, MathHelper.packRgb(155, 22, 217) + 150 << 24);
            DrawableHelper.fill(matrixStack, 10, 31 + (i * 18), 10 + MathHelper.floor((195 + 45) * ratio), (35 + (i * 18) + 10), MathHelper.packRgb(100,65,165) + (150 << 24));
            DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, new LiteralText((1 + i + altOffset) + ": ").append(item.getName()), 15, 34 + (i * 18), MathHelper.packRgb(255, 255, 255));

            Text percentage = new LiteralText(MathHelper.floor(ratio * 100) + " %");
            DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, percentage, 195 + 10 + 42 - minecraftClient.textRenderer.getWidth(percentage), 34 + (i * 18), MathHelper.packRgb(255, 255, 255));

            i++;
        }

    }

    private void renderTeams(MatrixStack matrixStack) {
        if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.NONE) return;

        DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, new LiteralText("Team Score: §7§o(Mode " + ItemShuffle.getInstance().getSettings().itemType.name() + ")"), 10, 20, MathHelper.packRgb(255, 255, 255));

        for (int i = 0; i < teamData.size(); i++) {
            DrawableHelper.fill(matrixStack, 10, 31 + (i * 18), 195 + 10 + 45, 35 + (i * 18) + 10, MathHelper.packRgb(155, 22, 217) + 150 << 24);
            TeamData data;
            try {
                data = teamData.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            String score = ItemShuffle.getInstance().getSettings().itemType == ItemGenType.RUN ? "§b§l" + data.runScore : "§c§l" + data.fails;
            String item = "";
            if (ItemShuffle.getInstance().getSettings().teamShowType == TeamData.Show.FULL) {
                String name = TranslationStorage.getInstance().get(data.item.getTranslationKey());
                item = data.completed ? "§a " + name + " §r§2✔" : "§f " + name;
            }

            DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, new LiteralText("§" + data.color + data.name + "§r §7[" + data.players + "]§r: " + score + item), 15, 34 + (i * 18), MathHelper.packRgb(255, 255, 255));

        }
    }

    private void renderItem(MatrixStack matrixStack) {
        if (item == null) {
            return;
        }
        String name = TranslationStorage.getInstance().get(item.getTranslationKey());
        Text text;
        if (isItemCompleted) {
            text = new LiteralText("§7Your item: §a§l" + name + " §r§2✔");
        } else {
            text = new LiteralText("§7Your item: §f§l" + name);
        }
        DrawableHelper.drawTextWithShadow(matrixStack, minecraftClient.textRenderer, text, 10, 106, MathHelper.packRgb(255,255,255));
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
