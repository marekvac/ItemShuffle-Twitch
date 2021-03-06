package me.marcuscz.itemshuffle.mixins;

import me.marcuscz.itemshuffle.client.screens.ItemShuffleConfigurationScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void insertEntropySettingsButton(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(this.width - 100, this.height - 20, 100, 20, new TranslatableText("ItemShuffle Settings"), button -> this.client.setScreen(new ItemShuffleConfigurationScreen(this))));
    }

}
