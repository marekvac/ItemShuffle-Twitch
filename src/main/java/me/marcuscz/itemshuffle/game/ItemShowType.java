package me.marcuscz.itemshuffle.game;

public enum ItemShowType {
    OFF, TEXT_ONLY, ICON_ONLY, FULL;

    public static int getIndex(ItemShowType gameType) {
        int i = 0;
        for (ItemShowType gt : values()) {
            if (gt == gameType) {
                break;
            }
            i++;
        }
        return i;
    }
}
