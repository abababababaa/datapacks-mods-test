package dev.datapacksmods.obsidian.client;

import dev.datapacksmods.obsidian.ObsidianItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.item.ItemConvertible;

public final class ObsidianClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ArmorRenderer.register(new ObsidianArmorRenderer(), ObsidianItems.ARMOR.toArray(ItemConvertible[]::new));
    }
}
