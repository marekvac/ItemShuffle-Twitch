package me.marcuscz.itemshuffle;

import net.minecraft.util.Identifier;

public class NetworkingConstants {

    public static final Identifier ITEM_MESSAGES = new Identifier("itemshuffle", "item_message");
    public static final Identifier TIMER_SHOW = new Identifier("itemshuffle", "show_timer");
    public static final Identifier TIMER_HIDE = new Identifier("itemshuffle", "hide_timer");
    public static final Identifier SETTING_SYNC = new Identifier("itemshuffle", "settings_sync");
    public static final Identifier VOTING_INIT = new Identifier("itemshuffle", "voting_init");
    public static final Identifier VOTING_CONFIRMATION = new Identifier("itemshuffle", "voting_confirmation");
    public static final Identifier VOTING_GET_WINNER = new Identifier("itemshuffle", "voting_get_winner");
    public static final Identifier VOTING_SEND_WINNER = new Identifier("itemshuffle", "voting_send_winner");
    public static final Identifier GAME_PAUSED = new Identifier("itemshuffle", "game_paused");
    public static final Identifier GAME_RESUMED = new Identifier("itemshuffle", "game_resumed");
    public static final Identifier NEXT_ROUND = new Identifier("itemshuffle", "game_next_round");
    public static final Identifier GAME_STOP = new Identifier("itemshuffle", "game_stopped");
    public static final Identifier SHOW_ITEM = new Identifier("itemshuffle", "show_item");
    public static final Identifier HIDE_ITEM = new Identifier("itemshuffle", "hide_item");
    public static final Identifier COMPLETE_ITEM = new Identifier("itemshuffle", "complete_item");
    public static final Identifier TEAM_DATA = new Identifier("itemshuffle", "team_data");
    public static final Identifier OTHER_ITEMS = new Identifier("itemshuffle", "other_items");
}
