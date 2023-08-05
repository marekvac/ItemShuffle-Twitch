package me.marcuscz.itemshuffle.game;

public enum ItemGenType {

    RANDOM, ALL_SAME, RUN, ALL_SAME_VS;

    public static int getIndex(ItemGenType gameType) {
        int i = 0;
        for (ItemGenType gt : values()) {
            if (gt == gameType) {
                break;
            }
            i++;
        }
        return i;
    }

}
