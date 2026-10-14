package dev.datapacksmods.obsidian.mixin;

import dev.datapacksmods.obsidian.ObsidianItems;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Creative mode bypasses vanilla applicability checks; remove only this forbidden entry. */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    protected AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory inventory,
                                      ScreenHandlerContext context) {
        super(type, syncId, inventory, context);
    }
    @Redirect(method = "updateResult", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantments(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/component/type/ItemEnchantmentsComponent;"))
    private ItemEnchantmentsComponent obsidian$excludeFireAspect(ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (!input.getStack(0).isOf(ObsidianItems.SWORD)) return enchantments;
        var filtered = new ItemEnchantmentsComponent.Builder(enchantments);
        filtered.remove(entry -> entry.matchesKey(Enchantments.FIRE_ASPECT));
        return filtered.build();
    }
}
