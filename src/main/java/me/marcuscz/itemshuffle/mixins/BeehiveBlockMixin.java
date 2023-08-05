package me.marcuscz.itemshuffle.mixins;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin {

    @Shadow @Final public static IntProperty HONEY_LEVEL;
    @Shadow @Final public static DirectionProperty FACING;

    private static final BooleanProperty HONEY_TAKEN = BooleanProperty.of("honey_taken");

    @Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
    private Comparable injectedHoneyLevel(BlockState instance, Property property) {
        if (property.equals(HONEY_LEVEL) && instance.get(HONEY_TAKEN)) {
            return 5;
        }

        ItemShuffle.getLogger().info("Returning asked default");
        return instance.get(property);

    }

    @Inject(method = "takeHoney(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "TAIL"))
    private void onHoneycombDrop(World world, BlockState state, BlockPos pos, CallbackInfo ci) {
        world.setBlockState(pos, state.with(HONEY_TAKEN, false), 3);
    }

    /**
     * @author MarcusCZ
     * @reason
     */
    @Overwrite
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HONEY_LEVEL, FACING, HONEY_TAKEN);
    }
}
