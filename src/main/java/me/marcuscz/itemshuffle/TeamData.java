package me.marcuscz.itemshuffle;

import me.marcuscz.itemshuffle.game.ItemShuffleTeam;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public class TeamData {

    public String name;
    public char color;
    public Item item;
    public int runScore;
    public int fails;
    public int players;
    public boolean completed;

    private TeamData() {

    }

    public FriendlyByteBuf toPacket() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(name);
        buf.writeChar(color);
        buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
        buf.writeInt(runScore);
        buf.writeInt(fails);
        buf.writeInt(players);
        buf.writeBoolean(completed);
        return buf;
    }

    public static TeamData fromTeam(ItemShuffleTeam team) {
        TeamData data = new TeamData();
        data.name = team.getName();
        data.color = team.getColor();
        data.item = team.getItem();
        data.runScore = team.getRunPoints();
        data.fails = team.getFails();
        data.players = team.getPlayers().size();
        data.completed = team.isCompleted();
        return data;
    }

    public static TeamData fromPacket(FriendlyByteBuf buf) {
        TeamData data = new TeamData();
        data.name = buf.readUtf();
        data.color = buf.readChar();
        data.item = BuiltInRegistries.ITEM.getValue(buf.readIdentifier());
        data.runScore = buf.readInt();
        data.fails = buf.readInt();
        data.players = buf.readInt();
        data.completed = buf.readBoolean();
        return data;
    }

    public enum Show {
        NONE,FULL,HIDE_ITEM;

        public static int getIndex(TeamData.Show teamShowType) {
            int i = 0;
            for (TeamData.Show gt : values()) {
                if (gt == teamShowType) {
                    break;
                }
                i++;
            }
            return i;
        }
    }

}
