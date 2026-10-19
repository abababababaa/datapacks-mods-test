package dev.datapacksmods.cabletiers.mixin;
import appeng.api.util.AEColor;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.parts.networking.CablePart;
import dev.datapacksmods.cabletiers.CableItems;
import dev.datapacksmods.cabletiers.TieredCableItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CablePart.class, remap = false)
public abstract class CableRecolorMixin {
    @Redirect(method = "changeColor", at = @At(value = "INVOKE",
        target = "Lappeng/core/definitions/ColoredItemDefinition;item(Lappeng/api/util/AEColor;)Lnet/minecraft/world/item/Item;"))
    private Item cabletiers$preserveTier(ColoredItemDefinition<?> definition, AEColor color) {
        var self = (CablePart) (Object) this;
        if (self.getPartItem().asItem() instanceof TieredCableItem variant) {
            return CableItems.item(variant.kind, color, variant.tier);
        }
        return definition.item(color);
    }
}
