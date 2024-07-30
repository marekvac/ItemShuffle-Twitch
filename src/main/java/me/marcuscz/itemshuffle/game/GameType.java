package me.marcuscz.itemshuffle.game;

public enum GameType {
    TWITCH, TEAM, SOLO, BOARD;

    public static int getIndex(GameType gameType) {
        int i = 0;
        for (GameType gt : values()) {
            if (gt == gameType) {
                break;
            }
            i++;
        }
        return i;
    }
}
