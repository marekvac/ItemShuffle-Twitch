package me.marcuscz.itemshuffle.mixins;

import me.marcuscz.itemshuffle.client.screens.ItemShuffleConfigurationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void insertEntropySettingsButton(CallbackInfo ci) {
        Button widget = Button.builder(
                Component.literal("ItemShuffle Settings"),
                button -> this.minecraft.setScreen(new ItemShuffleConfigurationScreen(this))
            ).bounds(
                this.width - 100,
                this.height - 20,
                100,
                20
        ).build();
        this.addRenderableWidget(widget);
    }

}
