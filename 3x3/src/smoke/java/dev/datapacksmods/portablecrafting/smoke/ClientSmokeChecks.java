package dev.datapacksmods.portablecrafting.smoke;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

final class ClientSmokeChecks {
    static void run() throws ReflectiveOperationException {
        var handler = new CraftingSmokeChecks.TestHandler();
        var book = new RecipeBookWidget();
        var handlerField = RecipeBookWidget.class.getDeclaredField("craftingScreenHandler");
        handlerField.setAccessible(true);
        handlerField.set(book, handler);
        var ghostField = RecipeBookWidget.class.getDeclaredField("ghostSlots");
        ghostField.setAccessible(true);
        var ghosts = (RecipeBookGhostSlots) ghostField.get(book);
        for (int input = 0; input < 9; input++) {
            book.acceptAlignedInput(Ingredient.ofItems(Items.OAK_PLANKS), input + 1, 1, input % 3, input / 3);
            var ghost = ghosts.getSlot(input);
            CraftingSmokeChecks.check(ghost.getX() == CraftingLayout.gridX(input)
                && ghost.getY() == CraftingLayout.gridY(input), "Ghost input position " + input);
        }
        book.slotClicked(handler.getSlot(5));
        CraftingSmokeChecks.check(ghosts.getSlotCount() == 9, "Armor clicks preserve recipe ghost");
        book.slotClicked(handler.getSlot(50));
        CraftingSmokeChecks.check(ghosts.getSlotCount() == 0, "Ninth input click clears recipe ghost");
        System.out.println("PORTABLE_CRAFTING_CLIENT_GHOST_CHECKS_PASSED");
    }
}
