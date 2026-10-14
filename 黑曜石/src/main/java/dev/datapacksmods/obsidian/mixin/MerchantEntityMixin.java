package dev.datapacksmods.obsidian.mixin;
import dev.datapacksmods.obsidian.ObsidianTrades;
import dev.datapacksmods.obsidian.TradeGenerationState;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin {
    @Inject(method = "getOffers", at = @At("RETURN"))
    private void obsidian$ensureMasterOffer(CallbackInfoReturnable<TradeOfferList> cir) {
        if ((Object) this instanceof VillagerEntity villager
            && !((TradeGenerationState) villager).obsidian$isGeneratingTrades()) {
            ObsidianTrades.ensureMasterOffer(villager, cir.getReturnValue());
        }
    }
}
