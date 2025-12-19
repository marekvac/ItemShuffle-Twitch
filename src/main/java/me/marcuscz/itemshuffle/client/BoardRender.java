package me.marcuscz.itemshuffle.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BoardRender {

    private final Minecraft minecraftClient = Minecraft.getInstance();
    public static final HashMap<Character, Integer> colors = new HashMap<>()
    {{
        put('9', ARGB.color(100, 0,0,255));
        put('2', ARGB.color(100,0,255,0));
        put('c', ARGB.color(100,255,0,0));
        put('e', ARGB.color(100,255,255,0));
    }};

    public int size;
    public int posX = 4;
    public int posY = 4;
    public List<Item> items;
    public List<Character> status;

    private final RenderPipeline pipeline;

    public BoardRender(int size) {
        this.size = size;
        this.pipeline = RenderPipelines.GUI_TEXTURED;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void draw(GuiGraphics context) {

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                context.blitSprite(pipeline, HudRender.background, posX + 18 * j, posY + 18 * i, 26, 26);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                context.blitSprite(pipeline, HudRender.slot, posX + 4 + 18 * j, posY + 4 + 18 * i, 18, 18);
            }
        }

        drawStatus(context);

        if (minecraftClient.options.keyPlayerList.isDown()) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    context.drawCenteredString(minecraftClient.font, Component.literal(String.valueOf(i*size+j+1)), posX + 4 + 18 * j + 9, posY + 4 + 18 * i + 4, ARGB.color(255,255,255,255));
                }
            }
            context.drawString(minecraftClient.font, Component.literal("Use §b/whatitem <n> §rto identify item"), posX, posY + 18 * size + 10, ARGB.color(255,255,255,255));
        } else {
            drawItems(context);
        }

    }

    private void drawStatus(GuiGraphics context) {
        if (status == null) return;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (status.get(i*size+j) == null) continue;
                int color = colors.getOrDefault(status.get(i*size+j), -1);
                if (color == -1) continue;
                context.fill(posX + 4 + 18 * j + 1, posY + 4 + 18 * i + 1, posX + 4 + 18 * j + 17, posY + 4 + 18 * i + 17, color);
            }
        }
    }

    private void drawItems(GuiGraphics context) {
        if (items == null) return;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (items.get(i*size+j) == null) continue;
                context.renderItem(new ItemStack(items.get(i*size+j)), posX + 4 + 18 * j + 1, posY + 4 + 18 * i + 1);
            }
        }

    }
}
