package me.marcuscz.itemshuffle.mixins;

import me.marcuscz.itemshuffle.ItemShuffle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
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

    @Shadow @Final public static IntegerProperty HONEY_LEVEL;
    @Shadow @Final public static EnumProperty<Direction> FACING;

    private static final BooleanProperty HONEY_TAKEN = BooleanProperty.create("honey_taken");

    @Redirect(method = "useItemOn", at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockState.getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable injectedHoneyLevel(BlockState instance, Property property) {
        if (property.equals(HONEY_LEVEL) && instance.getValue(HONEY_TAKEN)) {
            return 5;
        }

        ItemShuffle.getLogger().info("Returning asked default");
        return instance.getValue(property);

    }

    @Inject(method = "resetHoneyLevel(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "TAIL"))
    private void onHoneycombDrop(Level world, BlockState state, BlockPos pos, CallbackInfo ci) {
        world.setBlock(pos, state.setValue(HONEY_TAKEN, false), 3);
    }

    /**
     * @author MarcusCZ
     * @reason
     */
    @Overwrite
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HONEY_LEVEL, FACING, HONEY_TAKEN);
    }
}
