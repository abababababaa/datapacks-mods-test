package dev.datapacksmods.cabletiers.client;

import dev.datapacksmods.cabletiers.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CableTiers.MOD_ID, value = Dist.CLIENT)
public final class CableTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        var item = event.getItemStack().getItem();
        var kind = item instanceof TieredCableItem tiered ? tiered.kind : CableItems.vanillaKind(item);
        if (kind == null) return;
        int tier = item instanceof TieredCableItem tiered ? tiered.tier : 2;
        event.getToolTip().add(Component.translatable("tooltip.ae2_cable_tiers.tier", tier,
            CableConfig.baseCapacity(kind, tier)).withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable("tooltip.ae2_cable_tiers.mode").withStyle(ChatFormatting.DARK_GRAY));
    }
}
