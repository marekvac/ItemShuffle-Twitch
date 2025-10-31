package me.marcuscz.itemshuffle.game;

public enum ItemGenDimension {
    OFF, NETHER;

    public static int getIndex(ItemGenDimension dimType) {
        int i = 0;
        for (ItemGenDimension gt : values()) {
            if (gt == dimType) {
                break;
            }
            i++;
        }
        return i;
    }

}
