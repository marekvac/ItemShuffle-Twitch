package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.TeamData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class GameSettings {

    public static double[] DIFFICULTY_RATES = {0.7, 0.4, 0.05, 0.01};
    public static String[] DIFFICULTY_NAMES = {"Easy", "Medium", "Hard", "Extreme"};
    public static int MAX_DIFFICULTY = DIFFICULTY_RATES.length - 1;

    public boolean removeItems = false;
    public GameType gameType = GameType.SOLO;
    public ItemGenType itemType = ItemGenType.RANDOM;
    public int time = 6000;
    public boolean twitchEnabled = false;
    public boolean showTimers = true;
    public boolean pauseOnFail = true;
    public boolean giveFood = true;
    public ItemShowType showItemsMode = ItemShowType.FULL;
    public boolean debug = false;
    public TeamData.Show teamShowType = TeamData.Show.FULL;
    public boolean blockMode = false;
    public int boardSize = 3;
    public int boardTarget = 3;
    public ItemGenDimension itemGenDimension = ItemGenDimension.OFF;
    public int itemDifficulty = 0;

    public GameSettings() {

    }

    public GameSettings(FriendlyByteBuf buf) {
        removeItems = buf.readBoolean();
        gameType = buf.readEnum(GameType.class);
        itemType = buf.readEnum(ItemGenType.class);
        time = buf.readInt();
        twitchEnabled = buf.readBoolean();
        showTimers = buf.readBoolean();
        pauseOnFail = buf.readBoolean();
        giveFood = buf.readBoolean();
        showItemsMode = buf.readEnum(ItemShowType.class);
        debug = buf.readBoolean();
        teamShowType = buf.readEnum(TeamData.Show.class);
        blockMode = buf.readBoolean();
        boardSize = buf.readInt();
        boardTarget = buf.readInt();
        itemGenDimension = buf.readEnum(ItemGenDimension.class);
        itemDifficulty = buf.readInt();
    }

    @Override
    public String toString() {
        return "GameSettings{" +
                "removeItems=" + removeItems +
                ", gameType=" + gameType +
                ", itemType=" + itemType +
                ", time=" + time +
                ", twitchEnabled=" + twitchEnabled +
                ", showTimers=" + showTimers +
                ", pauseOnFail=" + pauseOnFail +
                ", giveFood=" + giveFood +
                ", showItems=" + showItemsMode +
                ", debug=" + debug +
                ", teamShowType=" + teamShowType +
                ", blockMode=" + blockMode +
                ", boardSize=" + boardSize +
                ", boardTarget=" + boardTarget +
                ", itemGenDimension=" + itemGenDimension +
                ", itemDifficulty=" + itemDifficulty +
                '}';
    }

    public void printSettings() {
        System.out.println(this);
    }

    public FriendlyByteBuf toPacket() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(removeItems);
        buf.writeEnum(gameType);
        buf.writeEnum(itemType);
        buf.writeInt(time);
        buf.writeBoolean(twitchEnabled);
        buf.writeBoolean(showTimers);
        buf.writeBoolean(pauseOnFail);
        buf.writeBoolean(giveFood);
        buf.writeEnum(showItemsMode);
        buf.writeBoolean(debug);
        buf.writeEnum(teamShowType);
        buf.writeBoolean(blockMode);
        buf.writeInt(boardSize);
        buf.writeInt(boardTarget);
        buf.writeEnum(itemGenDimension);
        buf.writeInt(itemDifficulty);
        return buf;
    }
}
