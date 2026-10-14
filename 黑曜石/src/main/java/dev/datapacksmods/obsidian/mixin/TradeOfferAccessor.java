package dev.datapacksmods.obsidian.mixin;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TradeOffer.class)
public interface TradeOfferAccessor {
    @Mutable @Accessor("sellItem") void obsidian$setSellItem(ItemStack stack);
}
