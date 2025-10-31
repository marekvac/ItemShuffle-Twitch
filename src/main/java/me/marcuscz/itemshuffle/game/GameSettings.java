package me.marcuscz.itemshuffle.game;

import me.marcuscz.itemshuffle.TeamData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class GameSettings {

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

    public GameSettings() {

    }

    public GameSettings(PacketByteBuf buf) {
        removeItems = buf.readBoolean();
        gameType = buf.readEnumConstant(GameType.class);
        itemType = buf.readEnumConstant(ItemGenType.class);
        time = buf.readInt();
        twitchEnabled = buf.readBoolean();
        showTimers = buf.readBoolean();
        pauseOnFail = buf.readBoolean();
        giveFood = buf.readBoolean();
        showItemsMode = buf.readEnumConstant(ItemShowType.class);
        debug = buf.readBoolean();
        teamShowType = buf.readEnumConstant(TeamData.Show.class);
        blockMode = buf.readBoolean();
        boardSize = buf.readInt();
        boardTarget = buf.readInt();
        itemGenDimension = buf.readEnumConstant(ItemGenDimension.class);
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
                '}';
    }

    public void printSettings() {
        System.out.println(this);
    }

    public PacketByteBuf toPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(removeItems);
        buf.writeEnumConstant(gameType);
        buf.writeEnumConstant(itemType);
        buf.writeInt(time);
        buf.writeBoolean(twitchEnabled);
        buf.writeBoolean(showTimers);
        buf.writeBoolean(pauseOnFail);
        buf.writeBoolean(giveFood);
        buf.writeEnumConstant(showItemsMode);
        buf.writeBoolean(debug);
        buf.writeEnumConstant(teamShowType);
        buf.writeBoolean(blockMode);
        buf.writeInt(boardSize);
        buf.writeInt(boardTarget);
        buf.writeEnumConstant(itemGenDimension);
        return buf;
    }
}
