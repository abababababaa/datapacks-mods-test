package dev.datapacksmods.cabletiers;

import appeng.api.util.AEColor;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.CablePart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class TieredCableItem extends ColoredPartItem<CablePart> {
    public final CableKind kind;
    public final int tier;
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TieredCableItem(CableKind kind, AEColor color, int tier) {
        // Reuse AE2's actual cable classes, geometry, buses and serialization.
        super(new Item.Properties().rarity(tier == 1 ? Rarity.COMMON : Rarity.EPIC),
            (Class) kind.partClass(), item -> kind.create(item), color);
        this.kind = kind;
        this.tier = tier;
    }
}
