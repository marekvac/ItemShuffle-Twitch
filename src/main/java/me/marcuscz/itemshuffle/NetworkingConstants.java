package me.marcuscz.itemshuffle;

import me.marcuscz.itemshuffle.networking.GenericPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class NetworkingConstants {

    public static final Identifier ITEM_MESSAGES = Identifier.fromNamespaceAndPath("itemshuffle", "item_message");
    public static final Identifier TIMER_SHOW = Identifier.fromNamespaceAndPath("itemshuffle", "show_timer");
    public static final Identifier TIMER_HIDE = Identifier.fromNamespaceAndPath("itemshuffle", "hide_timer");
    public static final Identifier SETTING_SYNC = Identifier.fromNamespaceAndPath("itemshuffle", "settings_sync");
    public static final Identifier VOTING_INIT = Identifier.fromNamespaceAndPath("itemshuffle", "voting_init");
    public static final Identifier VOTING_CONFIRMATION = Identifier.fromNamespaceAndPath("itemshuffle", "voting_confirmation");
    public static final Identifier VOTING_GET_WINNER = Identifier.fromNamespaceAndPath("itemshuffle", "voting_get_winner");
    public static final Identifier VOTING_SEND_WINNER = Identifier.fromNamespaceAndPath("itemshuffle", "voting_send_winner");
    public static final Identifier GAME_PAUSED = Identifier.fromNamespaceAndPath("itemshuffle", "game_paused");
    public static final Identifier GAME_RESUMED = Identifier.fromNamespaceAndPath("itemshuffle", "game_resumed");
    public static final Identifier NEXT_ROUND = Identifier.fromNamespaceAndPath("itemshuffle", "game_next_round");
    public static final Identifier GAME_STOP = Identifier.fromNamespaceAndPath("itemshuffle", "game_stopped");
    public static final Identifier SHOW_ITEM = Identifier.fromNamespaceAndPath("itemshuffle", "show_item");
    public static final Identifier HIDE_ITEM = Identifier.fromNamespaceAndPath("itemshuffle", "hide_item");
    public static final Identifier COMPLETE_ITEM = Identifier.fromNamespaceAndPath("itemshuffle", "complete_item");
    public static final Identifier TEAM_DATA = Identifier.fromNamespaceAndPath("itemshuffle", "team_data");
    public static final Identifier OTHER_ITEMS = Identifier.fromNamespaceAndPath("itemshuffle", "other_items");
    public static final Identifier INIT_BOARD = Identifier.fromNamespaceAndPath("itemshuffle", "init_board");
    public static final Identifier HIDE_BOARD = Identifier.fromNamespaceAndPath("itemshuffle", "hide_board");
    public static final Identifier BOARD_SET_STATUS = Identifier.fromNamespaceAndPath("itemshuffle", "board_set_status");

    public static final CustomPacketPayload.Type<GenericPayload> ITEM_MESSAGES_ID = new CustomPacketPayload.Type<>(ITEM_MESSAGES);
    public static final CustomPacketPayload.Type<GenericPayload> TIMER_SHOW_ID = new CustomPacketPayload.Type<>(TIMER_SHOW);
    public static final CustomPacketPayload.Type<GenericPayload> TIMER_HIDE_ID = new CustomPacketPayload.Type<>(TIMER_HIDE);
    public static final CustomPacketPayload.Type<GenericPayload> SETTING_SYNC_ID = new CustomPacketPayload.Type<>(SETTING_SYNC);
    public static final CustomPacketPayload.Type<GenericPayload> VOTING_INIT_ID = new CustomPacketPayload.Type<>(VOTING_INIT);
    public static final CustomPacketPayload.Type<GenericPayload> VOTING_CONFIRMATION_ID = new CustomPacketPayload.Type<>(VOTING_CONFIRMATION);
    public static final CustomPacketPayload.Type<GenericPayload> VOTING_GET_WINNER_ID = new CustomPacketPayload.Type<>(VOTING_GET_WINNER);
    public static final CustomPacketPayload.Type<GenericPayload> VOTING_SEND_WINNER_ID = new CustomPacketPayload.Type<>(VOTING_SEND_WINNER);
    public static final CustomPacketPayload.Type<GenericPayload> GAME_PAUSED_ID = new CustomPacketPayload.Type<>(GAME_PAUSED);
    public static final CustomPacketPayload.Type<GenericPayload> GAME_RESUMED_ID = new CustomPacketPayload.Type<>(GAME_RESUMED);
    public static final CustomPacketPayload.Type<GenericPayload> NEXT_ROUND_ID = new CustomPacketPayload.Type<>(NEXT_ROUND);
    public static final CustomPacketPayload.Type<GenericPayload> GAME_STOP_ID = new CustomPacketPayload.Type<>(GAME_STOP);
    public static final CustomPacketPayload.Type<GenericPayload> SHOW_ITEM_ID = new CustomPacketPayload.Type<>(SHOW_ITEM);
    public static final CustomPacketPayload.Type<GenericPayload> HIDE_ITEM_ID = new CustomPacketPayload.Type<>(HIDE_ITEM);
    public static final CustomPacketPayload.Type<GenericPayload> COMPLETE_ITEM_ID = new CustomPacketPayload.Type<>(COMPLETE_ITEM);
    public static final CustomPacketPayload.Type<GenericPayload> TEAM_DATA_ID = new CustomPacketPayload.Type<>(TEAM_DATA);
    public static final CustomPacketPayload.Type<GenericPayload> OTHER_ITEMS_ID = new CustomPacketPayload.Type<>(OTHER_ITEMS);
    public static final CustomPacketPayload.Type<GenericPayload> INIT_BOARD_ID = new CustomPacketPayload.Type<>(INIT_BOARD);
    public static final CustomPacketPayload.Type<GenericPayload> HIDE_BOARD_ID = new CustomPacketPayload.Type<>(HIDE_BOARD);
    public static final CustomPacketPayload.Type<GenericPayload> BOARD_SET_STATUS_ID = new CustomPacketPayload.Type<>(BOARD_SET_STATUS);
}
