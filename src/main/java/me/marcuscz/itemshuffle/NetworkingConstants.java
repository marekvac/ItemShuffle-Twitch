package me.marcuscz.itemshuffle;

import me.marcuscz.itemshuffle.networking.GenericPayload;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class NetworkingConstants {

    public static final Identifier ITEM_MESSAGES = Identifier.of("itemshuffle", "item_message");
    public static final Identifier TIMER_SHOW = Identifier.of("itemshuffle", "show_timer");
    public static final Identifier TIMER_HIDE = Identifier.of("itemshuffle", "hide_timer");
    public static final Identifier SETTING_SYNC = Identifier.of("itemshuffle", "settings_sync");
    public static final Identifier VOTING_INIT = Identifier.of("itemshuffle", "voting_init");
    public static final Identifier VOTING_CONFIRMATION = Identifier.of("itemshuffle", "voting_confirmation");
    public static final Identifier VOTING_GET_WINNER = Identifier.of("itemshuffle", "voting_get_winner");
    public static final Identifier VOTING_SEND_WINNER = Identifier.of("itemshuffle", "voting_send_winner");
    public static final Identifier GAME_PAUSED = Identifier.of("itemshuffle", "game_paused");
    public static final Identifier GAME_RESUMED = Identifier.of("itemshuffle", "game_resumed");
    public static final Identifier NEXT_ROUND = Identifier.of("itemshuffle", "game_next_round");
    public static final Identifier GAME_STOP = Identifier.of("itemshuffle", "game_stopped");
    public static final Identifier SHOW_ITEM = Identifier.of("itemshuffle", "show_item");
    public static final Identifier HIDE_ITEM = Identifier.of("itemshuffle", "hide_item");
    public static final Identifier COMPLETE_ITEM = Identifier.of("itemshuffle", "complete_item");
    public static final Identifier TEAM_DATA = Identifier.of("itemshuffle", "team_data");
    public static final Identifier OTHER_ITEMS = Identifier.of("itemshuffle", "other_items");
    public static final Identifier INIT_BOARD = Identifier.of("itemshuffle", "init_board");
    public static final Identifier HIDE_BOARD = Identifier.of("itemshuffle", "hide_board");
    public static final Identifier BOARD_SET_STATUS = Identifier.of("itemshuffle", "board_set_status");

    public static final CustomPayload.Id<GenericPayload> ITEM_MESSAGES_ID = new CustomPayload.Id<>(ITEM_MESSAGES);
    public static final CustomPayload.Id<GenericPayload> TIMER_SHOW_ID = new CustomPayload.Id<>(TIMER_SHOW);
    public static final CustomPayload.Id<GenericPayload> TIMER_HIDE_ID = new CustomPayload.Id<>(TIMER_HIDE);
    public static final CustomPayload.Id<GenericPayload> SETTING_SYNC_ID = new CustomPayload.Id<>(SETTING_SYNC);
    public static final CustomPayload.Id<GenericPayload> VOTING_INIT_ID = new CustomPayload.Id<>(VOTING_INIT);
    public static final CustomPayload.Id<GenericPayload> VOTING_CONFIRMATION_ID = new CustomPayload.Id<>(VOTING_CONFIRMATION);
    public static final CustomPayload.Id<GenericPayload> VOTING_GET_WINNER_ID = new CustomPayload.Id<>(VOTING_GET_WINNER);
    public static final CustomPayload.Id<GenericPayload> VOTING_SEND_WINNER_ID = new CustomPayload.Id<>(VOTING_SEND_WINNER);
    public static final CustomPayload.Id<GenericPayload> GAME_PAUSED_ID = new CustomPayload.Id<>(GAME_PAUSED);
    public static final CustomPayload.Id<GenericPayload> GAME_RESUMED_ID = new CustomPayload.Id<>(GAME_RESUMED);
    public static final CustomPayload.Id<GenericPayload> NEXT_ROUND_ID = new CustomPayload.Id<>(NEXT_ROUND);
    public static final CustomPayload.Id<GenericPayload> GAME_STOP_ID = new CustomPayload.Id<>(GAME_STOP);
    public static final CustomPayload.Id<GenericPayload> SHOW_ITEM_ID = new CustomPayload.Id<>(SHOW_ITEM);
    public static final CustomPayload.Id<GenericPayload> HIDE_ITEM_ID = new CustomPayload.Id<>(HIDE_ITEM);
    public static final CustomPayload.Id<GenericPayload> COMPLETE_ITEM_ID = new CustomPayload.Id<>(COMPLETE_ITEM);
    public static final CustomPayload.Id<GenericPayload> TEAM_DATA_ID = new CustomPayload.Id<>(TEAM_DATA);
    public static final CustomPayload.Id<GenericPayload> OTHER_ITEMS_ID = new CustomPayload.Id<>(OTHER_ITEMS);
    public static final CustomPayload.Id<GenericPayload> INIT_BOARD_ID = new CustomPayload.Id<>(INIT_BOARD);
    public static final CustomPayload.Id<GenericPayload> HIDE_BOARD_ID = new CustomPayload.Id<>(HIDE_BOARD);
    public static final CustomPayload.Id<GenericPayload> BOARD_SET_STATUS_ID = new CustomPayload.Id<>(BOARD_SET_STATUS);
}
