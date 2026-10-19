package dev.datapacksmods.cabletiers.mixin;
import appeng.me.GridNode;
import dev.datapacksmods.cabletiers.CableCapacity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridNode.class, remap = false)
public abstract class GridNodeMixin {
    @Inject(method = "getMaxChannels", at = @At("RETURN"), cancellable = true)
    private void cabletiers$capacity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(CableCapacity.nodeCapacity((GridNode) (Object) this, cir.getReturnValue()));
    }
}
