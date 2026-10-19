package dev.datapacksmods.cabletiers.mixin;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import dev.datapacksmods.cabletiers.CableCapacity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridConnection.class, remap = false)
public abstract class GridConnectionMixin {
    @Shadow private GridNode sideA;
    @Shadow private GridNode sideB;
    @Inject(method = "getMaxChannels", at = @At("RETURN"), cancellable = true)
    private void cabletiers$capacity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(CableCapacity.connectionCapacity(sideA, sideB, cir.getReturnValue()));
    }
}
