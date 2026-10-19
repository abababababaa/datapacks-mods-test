package dev.datapacksmods.cabletiers;

import appeng.api.util.AEColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CableTiers.MOD_ID)
public final class CableTiers {
    public static final String MOD_ID = "ae2_cable_tiers";
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MOD_ID);
    static {
        TABS.register("cables", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ae2_cable_tiers"))
            .icon(() -> new ItemStack(CableItems.item(CableKind.SMART, AEColor.TRANSPARENT, 3)))
            .displayItems((parameters, output) -> {
                for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) for (int tier = 1; tier <= 3; tier++) {
                    output.accept(CableItems.item(kind, color, tier));
                }
            }).build());
    }
    public CableTiers(IEventBus bus, ModContainer container) {
        CableItems.ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(CableTiers::setTierTwoRarity);
        container.registerConfig(ModConfig.Type.SERVER, CableConfig.SPEC);
    }
    private static void setTierTwoRarity(ModifyDefaultComponentsEvent event) {
        for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) {
            event.modify(kind.vanilla(color), builder -> builder.set(DataComponents.RARITY, Rarity.RARE));
        }
    }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
