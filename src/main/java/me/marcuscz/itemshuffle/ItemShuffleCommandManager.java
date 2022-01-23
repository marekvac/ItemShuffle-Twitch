package me.marcuscz.itemshuffle;

import com.mojang.brigadier.context.CommandContext;
import me.marcuscz.itemshuffle.game.GameManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

public class ItemShuffleCommandManager {

    public ItemShuffleCommandManager() {

    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(literal("itemshuffle")
                .then(literal("start").executes(this::start))
                .then(literal("stop").executes(this::stop))
                .then(literal("pause").executes(this::pause))
                .then(literal("resume").executes(this::resume))
                .then(literal("skip").executes(this::skip))
        ));
        ItemShuffle.getLogger().info("Registered commands!");
    }

    private int start(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.start()) {
            ctx.getSource().sendError(new LiteralText("§cGame already running"));
        }
        return 1;
    }

    private int stop(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.stop()) {
            ctx.getSource().sendError(new LiteralText("§cGame is not running"));
        }
        return 1;
    }

    private int pause(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.pause()) {
            ctx.getSource().sendError(new LiteralText("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int resume(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.resume()) {
            ctx.getSource().sendError(new LiteralText("§cGame is not paused or is not running"));
        }
        return 1;
    }

    private int skip(CommandContext<ServerCommandSource> ctx) {
        GameManager gameManager = GameManager.getInstance();
        if (!gameManager.skip()) {
            ctx.getSource().sendError(new LiteralText("§cGame is not running"));
        }
        return 1;
    }

}
