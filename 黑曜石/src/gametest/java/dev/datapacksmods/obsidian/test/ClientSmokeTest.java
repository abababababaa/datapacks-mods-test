package dev.datapacksmods.obsidian.test;

import dev.datapacksmods.obsidian.*;
import dev.datapacksmods.obsidian.client.*;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.registry.Registries;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ClientSmokeTest implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        if (!Boolean.getBoolean("obsidian-equipment.client-smoke")) return;
        try {
            SharedConstants.createGameVersion();
            Bootstrap.initialize();
            new ObsidianEquipment().onInitialize();
            new ObsidianClient().onInitializeClient();
            for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                var model = ObsidianArmorRenderer.createModel(slot);
                if (model.head == null || model.body == null) throw new AssertionError("Armor model bones missing");
            }
            for (var item : ObsidianItems.ALL) {
                String name = Registries.ITEM.getId(item).getPath();
                var stream = getClass().getClassLoader().getResourceAsStream("assets/obsidian_equipment/models/item/" + name + ".json");
                if (stream == null) throw new AssertionError("Missing item model: " + name);
                try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    var model = JsonUnbakedModel.deserialize(reader);
                    if (model.getElements().isEmpty()) throw new AssertionError("No model geometry: " + name);
                }
            }
            System.out.println("OBSIDIAN_CLIENT_MODELS_AND_REGISTRATION_PASSED");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
