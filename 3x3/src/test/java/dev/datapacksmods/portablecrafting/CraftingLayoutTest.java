package dev.datapacksmods.portablecrafting;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class CraftingLayoutTest {
    @Test void inputsHaveUniqueIdsAndDoNotReplaceEquipmentOrInventory() {
        var ids = new HashSet<Integer>();
        for (int ordinal = 1; ordinal <= 9; ordinal++) {
            int id = CraftingLayout.recipeSlotId(ordinal);
            assertTrue(ids.add(id));
            assertTrue(CraftingLayout.isInputSlot(id));
            assertEquals(ordinal, CraftingLayout.recipeOrdinal(id));
            assertFalse(id >= 5 && id <= 45, "Must preserve every vanilla equipment/inventory ID");
        }
        assertEquals(9, ids.size());
        assertEquals(0, CraftingLayout.recipeSlotId(0));
        for (int id = 5; id <= 45; id++) {
            assertFalse(CraftingLayout.isInputSlot(id));
            assertTrue(CraftingLayout.recipeOrdinal(id) >= 10);
        }
    }

    @Test void nineInputsFitWithoutOverlappingOtherControls() {
        for (int input = 0; input < 9; input++) {
            int x = CraftingLayout.gridX(input), y = CraftingLayout.gridY(input);
            assertTrue(x >= 94, "Keep clear of the offhand and player preview");
            assertTrue(x + 16 < CraftingLayout.RESULT_X);
            assertTrue(y >= 18 && y + 16 < 83, "Keep clear of title and main inventory");
            for (int other = 0; other < input; other++) {
                assertFalse(overlap(x, y, 16, 16,
                    CraftingLayout.gridX(other), CraftingLayout.gridY(other), 16, 16));
            }
            assertFalse(overlap(x, y, 16, 16, 178, 61, 20, 18)); // recipe-book button
        }
        assertTrue(CraftingLayout.RESULT_X + 16 < CraftingLayout.PANEL_WIDTH);
        assertFalse(overlap(178, 36, 16, 16, 178, 61, 20, 18));
    }

    private static boolean overlap(int x, int y, int w, int h, int a, int b, int c, int d) {
        return x < a + c && x + w > a && y < b + d && y + h > b;
    }
}
