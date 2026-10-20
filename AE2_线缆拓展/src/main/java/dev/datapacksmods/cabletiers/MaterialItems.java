package dev.datapacksmods.cabletiers;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import java.util.LinkedHashMap;
import java.util.Map;

/** Small crafting ingredients. Never tagged as whole crystals, dusts or wool. */
public final class MaterialItems {
    public static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();
    public static final Map<String, String> SOURCES = new LinkedHashMap<>();
    public static final DeferredItem<Item> MICRO_QUARTZ_FIBER = register("micro_quartz_fiber");
    static {
        fragment("fluix_crystal_fragment", "ae2:fluix_crystal");
        fragment("certus_quartz_fragment", "ae2:certus_quartz_crystal");
        fragment("nether_quartz_fragment", "minecraft:quartz");
        fragment("charged_certus_quartz_fragment", "ae2:charged_certus_quartz_crystal");
        fragment("redstone_fragment", "minecraft:redstone");
        fragment("glowstone_fragment", "minecraft:glowstone_dust");
        for (String color : new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}) {
            fragment(color + "_wool_fragment", "minecraft:" + color + "_wool");
        }
    }
    private static DeferredItem<Item> register(String name) {
        var item = CableItems.ITEMS.registerSimpleItem(name);
        ALL.put(name, item);
        return item;
    }
    private static void fragment(String name, String source) {
        register(name);
        SOURCES.put(name, source);
    }
    public static Item item(String name) { return ALL.get(name).get(); }
    public static void initialize() { /* Registers material suppliers before the item registry event. */ }
    private MaterialItems() {}
}
