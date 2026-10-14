package dev.datapacksmods.obsidian;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;

public final class ObsidianEquipment implements ModInitializer {
    public static final String MOD_ID = "obsidian_equipment";
    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }
    @Override public void onInitialize() {
        ObsidianItems.initialize();
        ObsidianSetBonus.initialize();
        ObsidianLoot.initialize();
        ServerLivingEntityEvents.AFTER_DEATH.register(ObsidianCombat::onDeath);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ObsidianItems.SWORD);
            ObsidianItems.ARMOR.forEach(entries::add);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries ->
            ObsidianItems.TOOLS.stream().filter(item -> item != ObsidianItems.SWORD).forEach(entries::add));
    }
}
