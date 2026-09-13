package net.identidade.tanuki_fix.mixin;

import net.identidade.tanuki_fix.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tanukidecor.block.seat.ISeatProvider;

@Mixin(ISeatProvider.class)
public interface ISeatProviderMixin {

    @Inject(method = "startSitting", at = @At("HEAD"), cancellable = true)
    default void startSittingFix(BlockState state, Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) {
            SeatEntity chair = new SeatEntity(pos, 0.6D, level);

            level.addFreshEntity(chair);
            player.startRiding(chair);

            level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());

            callbackInfo.setReturnValue(true);
            return;
        }
        callbackInfo.setReturnValue(false);
    }
}
