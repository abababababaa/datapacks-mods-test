package dev.datapacksmods.obsidian;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.entry.RegistryEntry;

public final class ObsidianSwordItem extends SwordItem {
    public ObsidianSwordItem(Settings settings) { super(ObsidianMaterial.INSTANCE, settings); }

    @Override public boolean canBeEnchantedWith(ItemStack stack, RegistryEntry<Enchantment> enchantment,
                                               EnchantingContext context) {
        if (enchantment.matchesKey(Enchantments.FIRE_ASPECT)) return false;
        return context == EnchantingContext.PRIMARY
            ? enchantment.value().isPrimaryItem(stack) : enchantment.value().isAcceptableItem(stack);
    }
}
