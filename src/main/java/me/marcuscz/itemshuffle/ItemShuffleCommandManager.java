package me.marcuscz.itemshuffle;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.marcuscz.itemshuffle.game.GameManager;
import me.marcuscz.itemshuffle.game.ItemManager;
import me.marcuscz.itemshuffle.game.ItemShuffleTeam;
import me.marcuscz.itemshuffle.game.PlayerManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
                                            .then(argument("player", EntityArgumentType.player())
                                                    .executes(this::addPlayerToTeam)))
                            )
                            .then(literal("removeplayer")
                                    .then(argument("name", StringArgumentType.string())
                                            .then(argument("player", EntityArgumentType.player())
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
            dispatcher.register(literal("whatitem").then(argument("player", EntityArgumentType.player()).executes(this::whatItemPlayerHave)));
        });
        ItemShuffle.getLogger().info("Registered commands!");
    }

    private int start(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.start()) {
            ctx.getSource().sendError(Text.literal("§cGame already running"));
        }
        return 1;
    }

    private int stop(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.stop()) {
            ctx.getSource().sendError(Text.literal("§cGame is not running"));
        }
        return 1;
    }

    private int pause(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.pause()) {
            ctx.getSource().sendError(Text.literal("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int resume(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.resume()) {
            ctx.getSource().sendError(Text.literal("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int skip(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.skip()) {
            ctx.getSource().sendError(Text.literal("§cGame is not running"));
        }
        return 1;
    }

    private int listTeams(CommandContext<ServerCommandSource> ctx) {
        Map<String, ItemShuffleTeam> teams = GameManager.getInstance().getPlayerManager().getTeams();
        ctx.getSource().sendFeedback(Text.literal("§3Teams:"), false);
        teams.forEach((name, t) -> {
            String msg = "§l" + name;
            msg += " §7(" + t.getPlayers().values().size() + ") - " + t.getPlayers().values();
            ctx.getSource().sendFeedback(Text.literal(msg), false);
        });
        return 1;
    }

    private int createTeam(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.createTeam(ctx.getArgument("name", String.class), ctx.getArgument("color", String.class));
            ctx.getSource().sendFeedback(Text.literal("§2Team created"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int addPlayerToTeam(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayerEntity player = ctx.getArgument("player", EntitySelector.class).getPlayer(ctx.getSource());
            manager.addPlayerToTeam(ctx.getArgument("name", String.class), player);
            ctx.getSource().sendFeedback(Text.literal("§2Player added"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int removePlayerFromTeam(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayerEntity player = ctx.getArgument("player", EntitySelector.class).getPlayer(ctx.getSource());
            manager.removePlayerFromTeam(ctx.getArgument("name", String.class), player);
            ctx.getSource().sendFeedback(Text.literal("§2Player removed"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int removeTeam(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.removeTeam(ctx.getArgument("name", String.class));
            ctx.getSource().sendFeedback(Text.literal("§2Team removed"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int skipPlayerItem(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (manager.isGamePlayer(player.getUuid())) {
                manager.getPlayer(player.getUuid()).skipItem();
            } else {
                throw new Exception("");
            }
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§cYou are not game player!"));
        }
        return 1;
    }

    private int joinTeam(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.joinPlayer(ctx.getArgument("team", String.class), ctx.getSource().getPlayer());
//            ctx.getSource().sendFeedback(Text.literal("§2Joined to team"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int teamAuto(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        try {
            manager.autoTeams(ctx.getArgument("size", Integer.class));
//            ctx.getSource().sendFeedback(Text.literal("§2Joined to team"), false);
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
        }
        return 1;
    }

    private int whatItemPlayerHave(CommandContext<ServerCommandSource> ctx) {
        PlayerManager manager = GameManager.getInstance().getPlayerManager();
        if (GameManager.isActive()) {
            try {
                ServerPlayerEntity player = ctx.getArgument("player", EntitySelector.class).getPlayer(ctx.getSource());
                if (!manager.isGamePlayer(player.getUuid())) {
                    ctx.getSource().sendError(Text.literal("§cInvalid player!"));
                }
                Item item = manager.getPlayer(player.getUuid()).getItem();
                Text text;
                if (manager.getPlayer(player.getUuid()).isCompleted()) {
                    text = Text.literal("§7Item of player " + player.getName().getString() + ": §a" + item.getName().getString() + " §r§2✔");
                } else {
                    text = Text.literal("§7Item of player " + player.getName().getString() + ": §f" + item.getName().getString());
                }
                ctx.getSource().sendMessage(text);
            } catch (CommandSyntaxException e) {
                ctx.getSource().sendError(Text.literal("§4" + e.getMessage()));
            }
        }
        return 1;
    }

    private int printItemQueue(CommandContext<ServerCommandSource> ctx) {
        ItemManager itemManager = GameManager.getInstance().getItemManager();
        ctx.getSource().sendFeedback(Text.literal(itemManager.getRunItemList().toString()), false);
        return 1;
    }

}
