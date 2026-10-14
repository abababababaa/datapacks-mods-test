package dev.datapacksmods.portablecrafting.smoke;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.screen.PlayerScreenHandler;
import java.util.Map;
import java.util.List;
import net.minecraft.util.Identifier;

final class CraftingSmokeChecks {
    static final class TestHandler extends PlayerScreenHandler {
        TestHandler() { super(new PlayerInventory(null), false, null); }
        // No world exists in this isolated handler fixture; exercise vanilla recipe matching directly.
        @Override public void onContentChanged(Inventory inventory) {}
    }

    static final class TestFiller extends InputSlotFiller<CraftingRecipeInput, CraftingRecipe> {
        TestFiller(TestHandler handler) {
            super(handler);
            inventory = (PlayerInventory) handler.getSlot(9).inventory;
        }
        void returnAllInputs() { returnInputs(); }
    }

    static void run() {
        TestHandler handler = new TestHandler();
        check(handler.slots.size() == 51, "51 total slots");
        check(handler.getCraftingWidth() == 3 && handler.getCraftingHeight() == 3, "3x3 input");
        check(handler.getCraftingSlotCount() == 10, "Recipe book sees 9 inputs and result");
        check(handler.getSlot(45).getIndex() == 40, "Vanilla offhand ID preserved");
        check(handler.getSlot(36).getIndex() == 0, "Vanilla hotbar IDs preserved");
        check(handler.getSlot(5).getIndex() == 39, "Vanilla armor IDs preserved");

        for (int input = 0; input < 9; input++) {
            var slot = handler.getSlot(CraftingLayout.recipeSlotId(input + 1));
            check(slot.getIndex() == input, "Grid input index " + input);
            check(slot.inventory == handler.getCraftingInput(), "Backed by real crafting input");
            check(slot.x == CraftingLayout.gridX(input) && slot.y == CraftingLayout.gridY(input),
                "Transformed slot coordinates " + input);
            slot.setStack(input == 4 ? ItemStack.EMPTY : new ItemStack(Items.OAK_PLANKS));
        }
        ShapedRecipe chest = new ShapedRecipe("", CraftingRecipeCategory.MISC,
            RawShapedRecipe.create(Map.of('#', Ingredient.ofItems(Items.OAK_PLANKS)),
                "###", "# #", "###"), new ItemStack(Items.CHEST));
        var recipeInput = handler.getCraftingInput().createRecipeInput();
        check(chest.matches(recipeInput, null), "Full 3x3 chest recipe matches");
        check(chest.craft(recipeInput, null).isOf(Items.CHEST), "Crafting produces chest");
        handler.getSlot(50).setStack(ItemStack.EMPTY);
        check(!chest.matches(handler.getCraftingInput().createRecipeInput(), null),
            "Missing ninth ingredient must fail");

        handler.getCraftingInput().clear();
        handler.getSlot(46).setStack(new ItemStack(Items.OAK_PLANKS, 7));
        var moved = handler.quickMove(null, 46);
        check(moved.getCount() == 7 && handler.getSlot(46).getStack().isEmpty(),
            "Shift-click returns additional grid slots to inventory");
        check(handler.getSlot(9).getStack().isOf(Items.OAK_PLANKS)
            && handler.getSlot(9).getStack().getCount() == 7, "Shift-click destination");

        var fillHandler = new TestHandler();
        fillHandler.getSlot(9).setStack(new ItemStack(Items.OAK_PLANKS, 8));
        var filler = new TestFiller(fillHandler);
        int plankId = RecipeMatcher.getItemId(new ItemStack(Items.OAK_PLANKS));
        filler.alignRecipeToGrid(3, 3, 0,
            new RecipeEntry<>(Identifier.of("portable_crafting_smoke", "chest"), chest),
            List.of(plankId, plankId, plankId, plankId, 0, plankId, plankId, plankId, plankId).iterator(), 1);
        check(chest.matches(fillHandler.getCraftingInput().createRecipeInput(), null),
            "Recipe-book autofill reaches all nine inputs");
        check(fillHandler.getSlot(5).getStack().isEmpty(), "Autofill never touches armor");
        check(fillHandler.getSlot(9).getStack().isEmpty(), "Autofill uses exactly eight planks");
        filler.returnAllInputs();
        check(fillHandler.getCraftingInput().isEmpty(), "Recipe-book clears all inputs");
        check(fillHandler.getSlot(36).getStack().getCount() == 8,
            "Recipe-book returns all eight ingredients to inventory");
        System.out.println("PORTABLE_CRAFTING_FUNCTIONAL_CHECKS_PASSED");
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
