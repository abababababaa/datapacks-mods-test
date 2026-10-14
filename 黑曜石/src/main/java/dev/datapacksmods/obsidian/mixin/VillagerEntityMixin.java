package dev.datapacksmods.obsidian.mixin;

import dev.datapacksmods.obsidian.ObsidianTrades;
import dev.datapacksmods.obsidian.TradeGenerationState;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin implements TradeGenerationState {
    @Unique private boolean obsidian$generatingTrades;
    @Override public boolean obsidian$isGeneratingTrades() { return obsidian$generatingTrades; }
    @Inject(method = "fillRecipes", at = @At("HEAD"))
    private void obsidian$beginGeneration(CallbackInfo ci) { obsidian$generatingTrades = true; }
    @Inject(method = "fillRecipes", at = @At("TAIL"))
    private void obsidian$finishGeneration(CallbackInfo ci) {
        obsidian$generatingTrades = false;
        var self = (VillagerEntity) (Object) this;
        ObsidianTrades.ensureMasterOffer(self, self.getOffers());
    }
}
