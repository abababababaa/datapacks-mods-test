package dev.datapacksmods.cabletiers;

import appeng.api.util.AEColor;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.CablePart;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CableItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CableTiers.MOD_ID);
    public record Variant(CableKind kind, AEColor color, int tier) {}
    public static final Map<Variant, DeferredItem<TieredCableItem>> VARIANTS = new LinkedHashMap<>();
    static {
        for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) for (int tier : new int[]{1, 3}) {
            String name = color.registryPrefix + "_" + kind.aeSuffix + "_tier_" + tier;
            VARIANTS.put(new Variant(kind, color, tier), ITEMS.register(name, () -> new TieredCableItem(kind, color, tier)));
        }
    }
    public static ColoredPartItem<? extends CablePart> item(CableKind kind, AEColor color, int tier) {
        return tier == 2 ? kind.vanilla(color) : VARIANTS.get(new Variant(kind, color, tier)).get();
    }
    public static CableKind vanillaKind(Item item) {
        for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) {
            if (kind.vanilla(color) == item) return kind;
        }
        return null;
    }
    private CableItems() {}
}
