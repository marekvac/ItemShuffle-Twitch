package me.marcuscz.itemshuffle.game;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class GameSettings {

    public boolean removeItems = false;
    public GameType gameType = GameType.CLASSIC;
    public int time = 6000;
    public boolean twitchEnabled = false;
    public boolean showTimers = true;

    public GameSettings() {

    }

    public GameSettings(PacketByteBuf buf) {
        removeItems = buf.readBoolean();
        gameType = buf.readEnumConstant(GameType.class);
        time = buf.readInt();
        twitchEnabled = buf.readBoolean();
        showTimers = buf.readBoolean();
    }

    @Override
    public String toString() {
        return "GameSettings{" +
                "removeItems=" + removeItems +
                ", gameType=" + gameType +
                ", time=" + time +
                ", twitchEnabled=" + twitchEnabled +
                ", showTimers=" + showTimers +
                '}';
    }

    public void printSettings() {
        System.out.println(this);
    }

    public PacketByteBuf toPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(removeItems);
        buf.writeEnumConstant(gameType);
        buf.writeInt(time);
        buf.writeBoolean(twitchEnabled);
        buf.writeBoolean(showTimers);
        return buf;
    }
}
