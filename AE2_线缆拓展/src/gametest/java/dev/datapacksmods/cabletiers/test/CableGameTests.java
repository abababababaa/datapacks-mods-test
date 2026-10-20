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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeColor;
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
            checkPattern(h, CableItems.item(kind, color, 1), new int[]{0, 2, 6, 8}, kind.vanilla(color));
            checkCraft(h, java.util.Collections.nCopies(8, new ItemStack(kind.vanilla(color))), CableItems.item(kind, color, 3), 2);
        }
        checkCraft(h, java.util.Collections.nCopies(4, new ItemStack(CableKind.COVERED.vanilla(AEColor.TRANSPARENT))),
            CableKind.DENSE_COVERED.vanilla(AEColor.TRANSPARENT), 1);
        checkCraft(h, java.util.Collections.nCopies(4, new ItemStack(CableKind.SMART.vanilla(AEColor.TRANSPARENT))),
            CableKind.DENSE_SMART.vanilla(AEColor.TRANSPARENT), 1);
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void independentCraftingRoutesUseRawMaterials(GameTestHelper h) {
        for (int tier : new int[]{1, 3}) {
            var glass = CableItems.item(CableKind.GLASS, AEColor.TRANSPARENT, tier);
            var covered = CableItems.item(CableKind.COVERED, AEColor.TRANSPARENT, tier);
            var smart = CableItems.item(CableKind.SMART, AEColor.TRANSPARENT, tier);
            var denseCovered = CableItems.item(CableKind.DENSE_COVERED, AEColor.TRANSPARENT, tier);
            var denseSmart = CableItems.item(CableKind.DENSE_SMART, AEColor.TRANSPARENT, tier);
            var rawGlass = new ArrayList<ItemStack>();
            var electronics = new ArrayList<ItemStack>();
            if (tier == 1) {
                rawGlass.add(new ItemStack(MaterialItems.MICRO_QUARTZ_FIBER.get()));
                for (int i = 0; i < 4; i++) rawGlass.add(new ItemStack(MaterialItems.item("fluix_crystal_fragment")));
                for (int i = 0; i < 2; i++) electronics.add(new ItemStack(MaterialItems.item("redstone_fragment")));
                for (int i = 0; i < 2; i++) electronics.add(new ItemStack(MaterialItems.item("glowstone_fragment")));
            } else {
                for (int i = 0; i < 4; i++) rawGlass.add(new ItemStack(itemById("ae2:quartz_fiber")));
                for (int i = 0; i < 2; i++) rawGlass.add(new ItemStack(itemById("ae2:fluix_block")));
                for (int i = 0; i < 4; i++) electronics.add(new ItemStack(Items.REDSTONE));
                electronics.add(new ItemStack(Items.GLOWSTONE));
            }
            checkCraft(h, rawGlass, glass, 4);
            for (var color : DyeColor.values()) {
                String colorName = color.getName();
                var inputs = new ArrayList<ItemStack>();
                inputs.add(new ItemStack(glass));
                var wool = tier == 1 ? MaterialItems.item(colorName + "_wool_fragment") : itemById("minecraft:" + colorName + "_wool");
                for (int i = 0; i < (tier == 1 ? 2 : 4); i++) inputs.add(new ItemStack(wool));
                checkCraft(h, inputs, covered, 1);
            }
            var smartInputs = new ArrayList<ItemStack>(electronics);
            smartInputs.add(new ItemStack(covered));
            checkCraft(h, smartInputs, smart, 1);
            checkPattern(h, covered, new int[]{0, 1, 3, 4}, denseCovered);
            var denseInputs = new ArrayList<ItemStack>(electronics);
            denseInputs.add(new ItemStack(denseCovered));
            checkCraft(h, denseInputs, denseSmart, 1);
            checkPattern(h, smart, new int[]{0, 1, 3, 4}, denseSmart);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void fragmentRoundTripsKeepColorChargeAndOriginalRecipes(GameTestHelper h) {
        h.assertTrue(MaterialItems.ALL.size() == 23 && MaterialItems.SOURCES.size() == 22, "23 materials, 22 reversible sources");
        for (var entry : MaterialItems.SOURCES.entrySet()) {
            var source = itemById(entry.getValue());
            var fragment = MaterialItems.item(entry.getKey());
            checkCraft(h, List.of(new ItemStack(source)), fragment, 8);
            checkCraft(h, java.util.Collections.nCopies(8, new ItemStack(fragment)), source, 1);
        }
        for (String dust : new String[]{"ae2:certus_quartz_dust", "ae2:nether_quartz_dust"}) {
            var inputs = List.of(new ItemStack(Items.GLASS), new ItemStack(itemById(dust)), new ItemStack(Items.GLASS));
            checkInput(h, CraftingInput.of(1, 3, inputs), MaterialItems.MICRO_QUARTZ_FIBER.get(), 4);
            // Existing AE2 3x3 fiber recipe must still produce three whole fibers.
            var normal = new ArrayList<ItemStack>();
            for (int i = 0; i < 3; i++) normal.add(new ItemStack(Items.GLASS));
            for (int i = 0; i < 3; i++) normal.add(new ItemStack(itemById(dust)));
            for (int i = 0; i < 3; i++) normal.add(new ItemStack(Items.GLASS));
            checkCraft(h, normal, itemById("ae2:quartz_fiber"), 3);
        }
        checkCraft(h, List.of(new ItemStack(itemById("ae2:quartz_fiber")),
            new ItemStack(itemById("ae2:fluix_crystal")), new ItemStack(itemById("ae2:fluix_crystal"))),
            CableKind.GLASS.vanilla(AEColor.TRANSPARENT), 4);
        var mixed = new ArrayList<ItemStack>();
        for (int i = 0; i < 4; i++) mixed.add(new ItemStack(MaterialItems.item("certus_quartz_fragment")));
        for (int i = 0; i < 4; i++) mixed.add(new ItemStack(MaterialItems.item("charged_certus_quartz_fragment")));
        h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, java.util.stream.Stream.concat(mixed.stream(), java.util.stream.Stream.of(ItemStack.EMPTY)).toList()), h.getLevel()).isEmpty(),
            "Charged and uncharged fragments cannot be mixed into a charged crystal");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void allDyeAndWashRecipesPreserveCableTier(GameTestHelper h) {
        for (CableKind kind : CableKind.values()) for (int tier : new int[]{1, 3}) for (AEColor color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) continue;
            var dye = DyeItem.byColor(DyeColor.byName(color.registryPrefix, DyeColor.WHITE));
            var inputs = new ArrayList<ItemStack>(java.util.Collections.nCopies(8, new ItemStack(CableItems.item(kind, AEColor.TRANSPARENT, tier))));
            inputs.add(new ItemStack(dye));
            checkCraft(h, inputs, CableItems.item(kind, color, tier), 8);
            for (var cleaner : new net.minecraft.world.item.Item[]{Items.SNOWBALL, Items.WATER_BUCKET}) {
                checkCraft(h, List.of(new ItemStack(CableItems.item(kind, color, tier)), new ItemStack(cleaner)),
                    CableItems.item(kind, AEColor.TRANSPARENT, tier), 1);
            }
        }
        h.succeed();
    }
    private static net.minecraft.world.item.Item itemById(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }
    private static void checkPattern(GameTestHelper h, net.minecraft.world.item.Item ingredient, int[] positions,
            net.minecraft.world.item.Item expected) {
        var slots = new ArrayList<ItemStack>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        for (int slot : positions) slots.set(slot, new ItemStack(ingredient));
        checkInput(h, CraftingInput.of(3, 3, slots), expected, 1);
    }
    private static void checkCraft(GameTestHelper h, List<ItemStack> ingredients, net.minecraft.world.item.Item expected, int count) {
        var slots = new ArrayList<ItemStack>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        for (int i = 0; i < ingredients.size(); i++) slots.set(i, ingredients.get(i).copy());
        var input = CraftingInput.of(3, 3, slots);
        checkInput(h, input, expected, count);
    }
    private static void checkInput(GameTestHelper h, CraftingInput input, net.minecraft.world.item.Item expected, int count) {
        var match = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel());
        h.assertTrue(match.isPresent(), "Recipe exists");
        var output = match.orElseThrow().value().assemble(input, h.getLevel().registryAccess());
        h.assertTrue(output.is(expected) && output.getCount() == count, "Expected " + count + " x " + expected + "; got " + output + " via " + match.orElseThrow().id());
    }
}
