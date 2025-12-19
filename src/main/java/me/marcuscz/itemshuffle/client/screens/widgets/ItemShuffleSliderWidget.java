package me.marcuscz.itemshuffle.client.screens.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class ItemShuffleSliderWidget extends AbstractSliderButton {

    String title;
    MessageSupplier messageSupplier;
    ValueUpdater valueUpdater;

    public ItemShuffleSliderWidget(int x, int y, int width, int height, String title, double value, MessageSupplier messageSupplier, ValueUpdater valueUpdater) {
        super(x, y, width, height, Component.literal(title), value);
        this.title = title;
        this.messageSupplier=messageSupplier;
        this.valueUpdater=valueUpdater;
        this.updateMessage();
    }

    protected void updateMessage() {
    }

    @Override
    public Component getMessage() {
        return this.messageSupplier.updateMessage(this,this.title,this.value);
    }

    @Override
    protected void applyValue() {
        valueUpdater.applyValue(this.value);
    }

    public interface MessageSupplier {
        Component updateMessage(ItemShuffleSliderWidget slider,String title, double value);
    }

    public interface ValueUpdater {
        void applyValue(double value);
    }
}
