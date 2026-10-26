package me.marcuscz.itemshuffle.game;

public enum GameType {
    TWITCH, TEAM, SOLO, BOARD, TIC_TAC_TOE;

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

    public boolean isBoardType() {
        return this == BOARD || this == TIC_TAC_TOE;
    }
}
