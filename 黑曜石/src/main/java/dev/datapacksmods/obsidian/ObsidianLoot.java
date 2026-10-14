package dev.datapacksmods.obsidian;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

public final class ObsidianLoot {
    public static LootPool.Builder ruinedPortalPool() {
        var pool = LootPool.builder().rolls(ConstantLootNumberProvider.create(1))
            .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.01F, 0.05F)));
        ObsidianItems.TOOLS.forEach(item -> pool.with(ItemEntry.builder(item)));
        return pool;
    }
    public static void initialize() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (key.equals(LootTables.RUINED_PORTAL_CHEST)) builder.pool(ruinedPortalPool());
        });
    }
    private ObsidianLoot() {}
}
