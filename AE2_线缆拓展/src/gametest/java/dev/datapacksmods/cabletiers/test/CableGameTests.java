package dev.datapacksmods.cabletiers.test;

import appeng.api.networking.*;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.items.parts.ColoredPartItem;
import appeng.me.pathfinding.IPathItem;
import appeng.me.service.PathingService;
import appeng.parts.networking.CablePart;
import dev.datapacksmods.cabletiers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(CableTiers.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CableGameTests {
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void everyVariantHasCorrectPartCapacityAndRarity(GameTestHelper helper) {
        helper.assertTrue(CableItems.VARIANTS.size() == 170, "170 new items");
        var parts = new ArrayList<CablePart>();
        int index = 0;
        for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) for (int tier = 1; tier <= 3; tier++) {
            var item = CableItems.item(kind, color, tier);
            helper.assertTrue(new ItemStack(item).getRarity() == switch (tier) {
                case 1 -> Rarity.COMMON;
                case 2 -> Rarity.RARE;
                default -> Rarity.EPIC;
            }, "White/blue/purple rarity for every color and type");
            var pos = new BlockPos(index % 17 * 2 + 1, 2, index / 17 * 2 + 1);
            parts.add(PartHelper.setPart(helper.getLevel(), helper.absolutePos(pos), null, null, item));
            index++;
        }
        helper.runAfterDelay(25, () -> {
            int current = 0;
            for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) for (int tier = 1; tier <= 3; tier++) {
                var part = parts.get(current++);
                helper.assertTrue(part != null && kind.partClass().isInstance(part), "Vanilla AE2 cable implementation");
                helper.assertTrue(part.getCableColor() == color, "Cable keeps its dye color");
                helper.assertTrue(((IPathItem) part.getGridNode()).getMaxChannels() == CableConfig.baseCapacity(kind, tier), "Real node capacity");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void glassTierOneActuallyStopsAtFour(GameTestHelper h) { checkRoute(h, CableKind.GLASS, 1, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void glassTierTwoStillStopsAtEight(GameTestHelper h) { checkRoute(h, CableKind.GLASS, 2, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void glassTierThreeReallyCarriesSixteen(GameTestHelper h) { checkRoute(h, CableKind.GLASS, 3, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void denseTierOneActuallyStopsAtSixteen(GameTestHelper h) { checkRoute(h, CableKind.DENSE_SMART, 1, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void denseTierTwoStillStopsAtThirtyTwo(GameTestHelper h) { checkRoute(h, CableKind.DENSE_SMART, 2, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void denseTierThreeReallyCarriesSixtyFour(GameTestHelper h) { checkRoute(h, CableKind.DENSE_SMART, 3, null); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void mixedBasicRouteKeepsLowTierBottleneck(GameTestHelper h) { checkRoute(h, CableKind.GLASS, 3, 1); }
    @GameTest(template = "empty", timeoutTicks = 240)
    public static void mixedDenseRouteKeepsVanillaBottleneck(GameTestHelper h) { checkRoute(h, CableKind.DENSE_SMART, 3, 2); }

    private static void checkRoute(GameTestHelper h, CableKind kind, int tier, Integer bottleneckTier) {
        h.setBlock(new BlockPos(2, 3, 2), AEBlocks.CONTROLLER.block());
        h.setBlock(new BlockPos(2, 2, 2), AEBlocks.CREATIVE_ENERGY_CELL.block());
        var root = PartHelper.setPart(h.getLevel(), h.absolutePos(new BlockPos(3, 3, 2)), null, null,
            CableItems.item(kind, AEColor.TRANSPARENT, bottleneckTier == null ? tier : bottleneckTier));
        var end = PartHelper.setPart(h.getLevel(), h.absolutePos(new BlockPos(4, 3, 2)), null, null,
            CableItems.item(kind, AEColor.TRANSPARENT, tier));
        int expected = CableConfig.baseCapacity(kind, bottleneckTier == null ? tier : bottleneckTier);
        var consumers = new ArrayList<IManagedGridNode>();
        h.runAfterDelay(10, () -> {
            h.assertTrue(root.getGridNode() != null && end.getGridNode() != null, "Both physical cable nodes initialized");
            // These are real AE2 network nodes that consume one channel each, not mocked capacity calculations.
            for (int i = 0; i < expected + 2; i++) {
                var node = GridHelper.createManagedNode(new Object(), (owner, changed) -> {})
                    .setFlags(GridFlags.REQUIRE_CHANNEL).setInWorldNode(false).setIdlePowerUsage(0);
                node.create(h.getLevel(), h.absolutePos(new BlockPos(4, 3, 2)));
                GridHelper.createConnection(end.getGridNode(), node.getNode());
                consumers.add(node);
            }
        });
        h.runAfterDelay(120, () -> {
            try {
                h.assertTrue(!end.getGridNode().getGrid().getPathingService().isNetworkBooting(), "Routing completed");
                h.assertTrue(root.getGridNode().getUsedChannels() == expected, "Actual root channel allocation: expected " + expected);
                h.assertTrue(end.getGridNode().getUsedChannels() == expected, "Actual end channel allocation");
                long active = consumers.stream().filter(node -> node.getNode().isActive()).count();
                h.assertTrue(active == expected, "Exactly " + expected + " devices work; excess consumers are offline, got " + active);
            } finally {
                consumers.forEach(IManagedGridNode::destroy);
            }
            h.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void recoloringDoesNotChangeTierAndColorsStayIsolated(GameTestHelper h) {
        var red = PartHelper.setPart(h.getLevel(), h.absolutePos(new BlockPos(2, 3, 2)), null, null,
            CableItems.item(CableKind.SMART, AEColor.RED, 3));
        var blue = PartHelper.setPart(h.getLevel(), h.absolutePos(new BlockPos(3, 3, 2)), null, null,
            CableItems.item(CableKind.SMART, AEColor.BLUE, 1));
        h.runAfterDelay(15, () -> {
            h.assertTrue(red.getGridNode().getGrid() != blue.getGridNode().getGrid(), "Different dye colors remain isolated");
            h.assertTrue(red.changeColor(AEColor.BLUE, null), "AE2 recoloring succeeds");
            h.assertTrue(red.getPartItem().asItem() == CableItems.item(CableKind.SMART, AEColor.BLUE, 3), "Recolored part remains tier III");
        });
        h.runAfterDelay(50, () -> {
            h.assertTrue(red.getGridNode().getGrid() == blue.getGridNode().getGrid(), "Matching colors connect normally");
            h.assertTrue(((IPathItem) red.getGridNode()).getMaxChannels() == 16, "Recolored tier III keeps 16 channels");
            h.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void ae2ChannelMultipliersAndInfiniteModeArePreserved(GameTestHelper h) {
        var part = PartHelper.setPart(h.getLevel(), h.absolutePos(new BlockPos(2, 3, 2)), null, null,
            CableItems.item(CableKind.DENSE_COVERED, AEColor.TRANSPARENT, 3));
        h.runAfterDelay(15, () -> {
            var service = (PathingService) part.getGridNode().getGrid().getPathingService();
            for (var mode : ChannelMode.values()) {
                service.setForcedChannelMode(mode);
                int expected = mode == ChannelMode.INFINITE ? Integer.MAX_VALUE : 64 * mode.getCableCapacityFactor();
                h.assertTrue(((IPathItem) part.getGridNode()).getMaxChannels() == expected, "Honors AE2 mode " + mode);
            }
            h.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void allTierConversionRecipesActuallyLoad(GameTestHelper h) {
        for (CableKind kind : CableKind.values()) for (AEColor color : AEColor.values()) {
            checkCraft(h, List.of(new ItemStack(kind.vanilla(color))), CableItems.item(kind, color, 1), 4);
            checkCraft(h, java.util.Collections.nCopies(4, new ItemStack(CableItems.item(kind, color, 1))), kind.vanilla(color), 1);
            checkCraft(h, java.util.Collections.nCopies(4, new ItemStack(kind.vanilla(color))), CableItems.item(kind, color, 3), 1);
        }
        h.succeed();
    }
    private static void checkCraft(GameTestHelper h, List<ItemStack> ingredients, net.minecraft.world.item.Item expected, int count) {
        var slots = new ArrayList<ItemStack>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        for (int i = 0; i < ingredients.size(); i++) slots.set(i, ingredients.get(i).copy());
        var input = CraftingInput.of(3, 3, slots);
        var match = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel());
        h.assertTrue(match.isPresent(), "Recipe exists");
        var output = match.orElseThrow().value().assemble(input, h.getLevel().registryAccess());
        h.assertTrue(output.is(expected) && output.getCount() == count, "Correct recipe output and count");
    }
}
