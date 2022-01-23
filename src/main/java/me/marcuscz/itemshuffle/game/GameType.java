package me.marcuscz.itemshuffle.game;

public enum GameType {
    CLASSIC, ALL_SAME, TWITCH;

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
