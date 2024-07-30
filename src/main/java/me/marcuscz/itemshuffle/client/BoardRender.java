package me.marcuscz.itemshuffle.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BoardRender {

    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    public static final HashMap<Character, Integer> colors = new HashMap<>()
    {{
        put('9', ColorHelper.Argb.getArgb(100, 0,0,255));
        put('2', ColorHelper.Argb.getArgb(100,0,255,0));
        put('c', ColorHelper.Argb.getArgb(100,255,0,0));
        put('e', ColorHelper.Argb.getArgb(100,255,255,0));
    }};

    public int size;
    public static Identifier background = new Identifier("advancements/task_frame_unobtained");
    public static Identifier slot = new Identifier("container/slot");
    public int posX = 4;
    public int posY = 4;
    public List<Item> items;
    public List<Character> status;

    public BoardRender(int size) {
        this.size = size;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void draw(DrawContext context) {

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                context.drawGuiTexture(background, posX + 18 * j, posY + 18 * i, 26, 26);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                context.drawGuiTexture(slot, posX + 4 + 18 * j, posY + 4 + 18 * i, 18, 18);
            }
        }

        drawStatus(context);

        if (minecraftClient.options.playerListKey.isPressed()) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    context.drawCenteredTextWithShadow(minecraftClient.textRenderer, Text.literal(String.valueOf(i*size+j+1)), posX + 4 + 18 * j + 9, posY + 4 + 18 * i + 4, ColorHelper.Argb.getArgb(255,255,255,255));
                }
            }
            context.drawTextWithShadow(minecraftClient.textRenderer, Text.literal("Use §b/whatitem <n> §rto identify item"), posX, posY + 18 * size + 10, ColorHelper.Argb.getArgb(255,255,255,255));
        } else {
            drawItems(context);
        }

    }

    private void drawStatus(DrawContext context) {
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

    private void drawItems(DrawContext context) {
        if (items == null) return;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (items.get(i*size+j) == null) continue;
                context.drawItem(new ItemStack(items.get(i*size+j)), posX + 4 + 18 * j + 1, posY + 4 + 18 * i + 1);
            }
        }

    }
}
