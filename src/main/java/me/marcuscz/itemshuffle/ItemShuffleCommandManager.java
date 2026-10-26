package me.marcuscz.itemshuffle;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.marcuscz.itemshuffle.game.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemShuffleCommandManager {

    public ItemShuffleCommandManager() {

    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("itemshuffle")
                    .then(literal("start").executes(this::start))
                    .then(literal("stop").executes(this::stop))
                    .then(literal("pause").executes(this::pause))
                    .then(literal("resume").executes(this::resume))
                    .then(literal("skip").executes(this::skip))
                    .then(literal("reroll").executes(this::reroll))
                    .then(literal("showrunitems").executes(this::printItemQueue))
                    .then(literal("teams")
                            .then(literal("list").executes(this::listTeams))
                            .then(literal("create")
                                    .then(argument("name", StringArgumentType.string())
                                            .then(argument("color", StringArgumentType.string()))
                                                    .executes(this::createTeam))
                            )
                            .then(literal("addplayer")
                                    .then(argument("name", StringArgumentType.string())
                                            .then(argument("player", EntityArgument.player())
                                                    .executes(this::addPlayerToTeam)))
                            )
                            .then(literal("removeplayer")
                                    .then(argument("name", StringArgumentType.string())
                                            .then(argument("player", EntityArgument.player())
                                                    .executes(this::removePlayerFromTeam)))
                            )
                            .then(literal("remove")
                                    .then(argument("name", StringArgumentType.string())
                                            .executes(this::removeTeam))
                            )
                            .then(literal("join")
                                    .then(argument("team", StringArgumentType.string())
                                            .executes(this::joinTeam))
                            )
                            .then(literal("auto")
                                    .then(argument("size", IntegerArgumentType.integer())
                                            .executes(this::teamAuto))
                            )
                    )
            );
            dispatcher.register(literal("skip").executes(this::skipPlayerItem));
            dispatcher.register(literal("whatitem")/*.then(argument("player", EntityArgumentType.player()).executes(this::whatItemPlayerHave))*/.then(argument("n", IntegerArgumentType.integer()).executes(this::whatBoardItem)));
        });
        ItemShuffle.getLogger().info("Registered commands!");
    }

    private int reroll(CommandContext<CommandSourceStack> ctx) {
        if (ItemShuffle.getInstance().getSettings().gameType != GameType.BOARD) return 1;
        if (!GameManager.isActive()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not running"));
            return 1;
        }
        if (!GameManager.getInstance().reRollBoard(false)) {
            ctx.getSource().sendFailure(Component.literal("§cInvalid game type"));
            return 1;
        }
        ctx.getSource().sendSystemMessage(Component.literal("§7Board items re-rolled."));
        return 1;
    }

    private int whatBoardItem(CommandContext<CommandSourceStack> ctx) {
        if (!ItemShuffle.getInstance().getSettings().gameType.isBoardType()) return 1;
        if (!GameManager.isActive()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not running"));
            return 1;
        }
        if (ItemShuffle.getInstance().getSettings().gameType == GameType.TIC_TAC_TOE) {
            if (!ctx.getSource().isPlayer()) return 1;
            var team = GameManager.getInstance().getPlayerManager().getPlayersTeam(ctx.getSource().getPlayer().getUUID());
            if (team == null) return 1;
            if (team != GameManager.getInstance().getPlayerManager().getActiveTeam()) {
                ctx.getSource().sendFailure(Component.literal("§cIt's not your turn!"));
                return 1;
            }
        }
        int n = ctx.getArgument("n", Integer.class);
        if (n > ItemShuffle.getInstance().getSettings().boardSize*ItemShuffle.getInstance().getSettings().boardSize || n < 1) {
            ctx.getSource().sendFailure(Component.literal("§cInvalid number"));
            return 1;
        }
        Item i = GameManager.getInstance().getItemManager().boardItems.get(n-1);
        ctx.getSource().sendSystemMessage(Component.literal("§7Item at " + n + ": §f" + i.getName().getString()));
        return 1;
    }

    private int start(CommandContext<CommandSourceStack> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.start()) {
            ctx.getSource().sendFailure(Component.literal("§cGame already running"));
        }
        return 1;
    }

    private int stop(CommandContext<CommandSourceStack> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.stop()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not running"));
        }
        return 1;
    }

    private int pause(CommandContext<CommandSourceStack> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.pause()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int resume(CommandContext<CommandSourceStack> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.resume()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int skip(CommandContext<CommandSourceStack> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.skip()) {
            ctx.getSource().sendFailure(Component.literal("§cGame is not running"));
        }
        return 1;
    }

    private int listTeams(CommandContext<CommandSourceStack> ctx) {
        Map<String, ItemShuffleTeam> teams = GameManager.getInstance().getPlayerManager().getTeams();
        ctx.getSource().sendSuccess(() -> Component.literal("§3Teams:"), false);
        teams.forEach((name, t) -> {
            String msg = "§l" + name;
            msg += " §7(" + t.getPlayers().values().size() + ") - " + t.getPlayers().values();
            String finalMsg = msg;
            ctx.getSource().sendSuccess(() -> Component.literal(finalMsg), false);
        });
        return 1;
    }

    private int createTeam(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.createTeam(ctx.getArgument("name", String.class), ctx.getArgument("color", String.class));
            ctx.getSource().sendSuccess(() -> Component.literal("§2Team created"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int addPlayerToTeam(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayer player = ctx.getArgument("player", EntitySelector.class).findSinglePlayer(ctx.getSource());
            manager.addPlayerToTeam(ctx.getArgument("name", String.class), player);
            ctx.getSource().sendSuccess(() -> Component.literal("§2Player added"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int removePlayerFromTeam(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayer player = ctx.getArgument("player", EntitySelector.class).findSinglePlayer(ctx.getSource());
            manager.removePlayerFromTeam(ctx.getArgument("name", String.class), player);
            ctx.getSource().sendSuccess(() -> Component.literal("§2Player removed"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int removeTeam(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.removeTeam(ctx.getArgument("name", String.class));
            ctx.getSource().sendSuccess(() -> Component.literal("§2Team removed"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int skipPlayerItem(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (manager.isGamePlayer(player.getUUID())) {
                manager.getPlayer(player.getUUID()).skipItem();
            } else {
                throw new Exception("");
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cYou are not game player!"));
        }
        return 1;
    }

    private int joinTeam(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.joinPlayer(ctx.getArgument("team", String.class), ctx.getSource().getPlayer());
//            ctx.getSource().sendFeedback(Text.literal("§2Joined to team"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int teamAuto(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.autoTeams(ctx.getArgument("size", Integer.class));
//            ctx.getSource().sendFeedback(Text.literal("§2Joined to team"), false);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int whatItemPlayerHave(CommandContext<CommandSourceStack> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        if (GameManager.isActive()) {
            try {
                ServerPlayer player = ctx.getArgument("player", EntitySelector.class).findSinglePlayer(ctx.getSource());
                if (!manager.isGamePlayer(player.getUUID())) {
                    ctx.getSource().sendFailure(Component.literal("§cInvalid player!"));
                }
                Item item = manager.getPlayer(player.getUUID()).getItem();
                Component text;
                if (manager.getPlayer(player.getUUID()).isCompleted()) {
                    text = Component.literal("§7Item of player " + player.getName().getString() + ": §a" + item.getName().getString() + " §r§2✔");
                } else {
                    text = Component.literal("§7Item of player " + player.getName().getString() + ": §f" + item.getName().getString());
                }
                ctx.getSource().sendSystemMessage(text);
            } catch (CommandSyntaxException e) {
                ctx.getSource().sendFailure(Component.literal("§4" + e.getMessage()));
            }
        }
        return 1;
    }

    private int printItemQueue(CommandContext<CommandSourceStack> ctx) {
        ItemManager itemManager = GameManager.getInstance().getItemManager();
        ctx.getSource().sendSuccess(() -> Component.literal(itemManager.getRunItemList().toString()), false);
        return 1;
    }

}
