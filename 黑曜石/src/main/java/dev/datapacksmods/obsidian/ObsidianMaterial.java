package dev.datapacksmods.obsidian;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum ObsidianMaterial implements ToolMaterial {
    INSTANCE;
    @Override public int getDurability() { return 2300; }
    @Override public float getMiningSpeedMultiplier() { return 10.0F; }
    @Override public float getAttackDamage() { return 5.0F; }
    @Override public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantability() { return 18; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.OBSIDIAN); }
}
