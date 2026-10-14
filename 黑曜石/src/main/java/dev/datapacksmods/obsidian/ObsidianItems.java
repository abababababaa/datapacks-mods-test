package dev.datapacksmods.obsidian;

import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import java.util.List;
import java.util.Map;

public final class ObsidianItems {
    public static final RegistryEntry<ArmorMaterial> ARMOR_MATERIAL = Registry.registerReference(
        Registries.ARMOR_MATERIAL, ObsidianEquipment.id("obsidian"), new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 4, ArmorItem.Type.CHESTPLATE, 9,
                ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.BOOTS, 4, ArmorItem.Type.BODY, 12),
            18, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, () -> Ingredient.ofItems(Items.OBSIDIAN),
            List.of(new ArmorMaterial.Layer(ObsidianEquipment.id("obsidian"))), 3.5F, 0.12F));

    public static final Item SWORD = register("obsidian_sword", new ObsidianSwordItem(settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ObsidianMaterial.INSTANCE, 3, -2.4F))));
    public static final Item PICKAXE = register("obsidian_pickaxe", new PickaxeItem(ObsidianMaterial.INSTANCE, settings()
        .attributeModifiers(PickaxeItem.createAttributeModifiers(ObsidianMaterial.INSTANCE, 1, -2.8F))));
    public static final Item AXE = register("obsidian_axe", new AxeItem(ObsidianMaterial.INSTANCE, settings()
        .attributeModifiers(AxeItem.createAttributeModifiers(ObsidianMaterial.INSTANCE, 5, -3.0F))));
    public static final Item SHOVEL = register("obsidian_shovel", new ShovelItem(ObsidianMaterial.INSTANCE, settings()
        .attributeModifiers(ShovelItem.createAttributeModifiers(ObsidianMaterial.INSTANCE, 1.5F, -3.0F))));
    public static final Item HOE = register("obsidian_hoe", new HoeItem(ObsidianMaterial.INSTANCE, settings()
        .attributeModifiers(HoeItem.createAttributeModifiers(ObsidianMaterial.INSTANCE, -4, 0.0F))));
    public static final Item HELMET = armor("obsidian_helmet", ArmorItem.Type.HELMET);
    public static final Item CHESTPLATE = armor("obsidian_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final Item LEGGINGS = armor("obsidian_leggings", ArmorItem.Type.LEGGINGS);
    public static final Item BOOTS = armor("obsidian_boots", ArmorItem.Type.BOOTS);
    public static final List<Item> TOOLS = List.of(SWORD, PICKAXE, AXE, SHOVEL, HOE);
    public static final List<Item> ARMOR = List.of(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final List<Item> ALL = List.of(SWORD, PICKAXE, AXE, SHOVEL, HOE, HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final Map<Item, Item> DIAMOND_UPGRADES = Map.of(
        Items.DIAMOND_SWORD, SWORD, Items.DIAMOND_PICKAXE, PICKAXE, Items.DIAMOND_AXE, AXE,
        Items.DIAMOND_SHOVEL, SHOVEL, Items.DIAMOND_HOE, HOE, Items.DIAMOND_HELMET, HELMET,
        Items.DIAMOND_CHESTPLATE, CHESTPLATE, Items.DIAMOND_LEGGINGS, LEGGINGS, Items.DIAMOND_BOOTS, BOOTS);

    private static Item.Settings settings() { return new Item.Settings().fireproof(); }
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ObsidianEquipment.id(name), item);
    }
    private static Item armor(String name, ArmorItem.Type type) {
        return register(name, new ArmorItem(ARMOR_MATERIAL, type, settings().maxDamage(type.getMaxDamage(42))));
    }
    public static void initialize() { /* Trigger static registration during mod initialization. */ }
    private ObsidianItems() {}
}
