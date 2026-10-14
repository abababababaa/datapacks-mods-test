package dev.datapacksmods.obsidian;

import dev.datapacksmods.obsidian.mixin.TradeOfferAccessor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import java.util.ArrayList;

public final class ObsidianTrades {
    public static void ensureMasterOffer(VillagerEntity villager, TradeOfferList offers) {
        if (villager.getWorld().isClient || villager.getVillagerData().getLevel() != 5) return;
        var profession = villager.getVillagerData().getProfession();
        if (profession != VillagerProfession.TOOLSMITH && profession != VillagerProfession.WEAPONSMITH
            && profession != VillagerProfession.ARMORER) return;
        if (offers.stream().anyMatch(offer -> ObsidianItems.ALL.contains(offer.getSellItem().getItem()))) return;

        var candidates = new ArrayList<TradeOffer>();
        for (TradeOffer offer : offers) {
            if (ObsidianItems.DIAMOND_UPGRADES.containsKey(offer.getSellItem().getItem())
                && offer.getMerchantExperience() >= 30) candidates.add(offer);
        }
        if (candidates.isEmpty()) {
            for (TradeOffer offer : offers) {
                if (ObsidianItems.DIAMOND_UPGRADES.containsKey(offer.getSellItem().getItem())) candidates.add(offer);
            }
        }
        if (!candidates.isEmpty()) {
            TradeOffer offer = candidates.get(villager.getRandom().nextInt(candidates.size()));
            ItemStack old = offer.getSellItem();
            ItemStack upgraded = new ItemStack(ObsidianItems.DIAMOND_UPGRADES.get(old.getItem()), old.getCount());
            upgraded.applyChanges(old.getComponentChanges());
            finishEnchantments(villager, upgraded);
            // Preserve uses, demand, discounts, input costs, and experience exactly.
            ((TradeOfferAccessor) offer).obsidian$setSellItem(upgraded);
        } else {
            // Experimental trade rebalance and pre-existing/custom master villagers may have no diamond offer.
            Item fallback = profession == VillagerProfession.ARMORER ? ObsidianItems.CHESTPLATE
                : profession == VillagerProfession.WEAPONSMITH ? ObsidianItems.SWORD : ObsidianItems.PICKAXE;
            var stack = new ItemStack(fallback);
            finishEnchantments(villager, stack);
            offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 32), stack, 3, 30, 0.2F));
        }
    }

    private static void finishEnchantments(VillagerEntity villager, ItemStack stack) {
        if (stack.isOf(ObsidianItems.SWORD)) {
            EnchantmentHelper.apply(stack, builder -> builder.remove(entry -> entry.matchesKey(Enchantments.FIRE_ASPECT)));
        }
        if (!stack.hasEnchantments()) {
            stack.addEnchantment(villager.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.UNBREAKING), 1);
        }
    }
    private ObsidianTrades() {}
}
