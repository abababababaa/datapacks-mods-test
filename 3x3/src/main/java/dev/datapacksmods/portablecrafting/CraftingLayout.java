package dev.datapacksmods.portablecrafting;

/** Keep all 46 vanilla slot IDs, including armor and offhand, unchanged. */
public final class CraftingLayout {
    public static final int VANILLA_SLOT_COUNT = 46;
    public static final int INPUT_COUNT = 9;
    public static final int PANEL_WIDTH = 204;
    public static final int GRID_X = 106;
    public static final int GRID_Y = 18;
    public static final int RESULT_X = 178;
    public static final int RESULT_Y = 36;

    private CraftingLayout() {}

    /** Recipe-book ordinal (0=result, 1..9=input) to actual network slot ID. */
    public static int recipeSlotId(int ordinal) {
        if (ordinal < 0 || ordinal > INPUT_COUNT) {
            throw new IllegalArgumentException("Not a crafting ordinal: " + ordinal);
        }
        return ordinal <= 4 ? ordinal : VANILLA_SLOT_COUNT + ordinal - 5;
    }

    public static boolean isInputSlot(int slotId) {
        return (slotId >= 1 && slotId <= 4) || (slotId >= 46 && slotId <= 50);
    }

    public static int recipeOrdinal(int slotId) {
        if (slotId >= 0 && slotId <= 4) return slotId;
        if (slotId >= 46 && slotId <= 50) return slotId - 41;
        return 10; // Outside the recipe book's input range.
    }

    public static int gridX(int inputIndex) { return GRID_X + inputIndex % 3 * 18; }
    public static int gridY(int inputIndex) { return GRID_Y + inputIndex / 3 * 18; }
}
