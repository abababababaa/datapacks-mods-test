package dev.datapacksmods.obsidian.test;

import dev.datapacksmods.obsidian.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.*;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.minecraft.world.GameMode;
import java.util.*;

public final class ObsidianGameTests implements FabricGameTest {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static void check(TestContext context, boolean condition, String message) {
        context.assertTrue(condition, message);
    }
    private static RegistryEntry<Enchantment> enchantment(TestContext context, net.minecraft.registry.RegistryKey<Enchantment> key) {
        return context.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(key);
    }
    private static void equipFull(LivingEntity entity) {
        for (int i = 0; i < ARMOR_SLOTS.length; i++) entity.equipStack(ARMOR_SLOTS[i], new ItemStack(ObsidianItems.ARMOR.get(i)));
    }
    private static int drops(TestContext context, Item item) {
        return context.getEntities(EntityType.ITEM).stream().map(ItemEntity::getStack)
            .filter(stack -> stack.isOf(item)).mapToInt(ItemStack::getCount).sum();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void allNineRecipesAndTags(TestContext context) {
        Map<Item, String[]> patterns = Map.of(
            ObsidianItems.SWORD, new String[]{"O", "O", "S"},
            ObsidianItems.PICKAXE, new String[]{"OOO", " S ", " S "},
            ObsidianItems.AXE, new String[]{"OO", "OS", " S"},
            ObsidianItems.SHOVEL, new String[]{"O", "S", "S"},
            ObsidianItems.HOE, new String[]{"OO", " S", " S"},
            ObsidianItems.HELMET, new String[]{"OOO", "O O"},
            ObsidianItems.CHESTPLATE, new String[]{"O O", "OOO", "OOO"},
            ObsidianItems.LEGGINGS, new String[]{"OOO", "O O", "O O"},
            ObsidianItems.BOOTS, new String[]{"O O", "O O"});
        patterns.forEach((item, pattern) -> {
            var stacks = new ArrayList<ItemStack>();
            for (String row : pattern) for (char symbol : row.toCharArray()) {
                stacks.add(symbol == 'O' ? new ItemStack(Items.OBSIDIAN)
                    : symbol == 'S' ? new ItemStack(Items.STICK) : ItemStack.EMPTY);
            }
            var input = CraftingRecipeInput.create(pattern[0].length(), pattern.length, stacks);
            var match = context.getWorld().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, context.getWorld());
            check(context, match.isPresent(), "Loaded recipe for " + item);
            check(context, match.orElseThrow().value().craft(input, context.getWorld().getRegistryManager()).isOf(item), "Correct recipe output " + item);
        });
        check(context, new ItemStack(ObsidianItems.SWORD).isIn(ItemTags.SWORDS), "Sword tag");
        check(context, new ItemStack(ObsidianItems.PICKAXE).isIn(ItemTags.PICKAXES), "Pickaxe tag");
        check(context, Registries.BLOCK.get(Identifier.ofVanilla("obsidian")) == Blocks.OBSIDIAN, "Vanilla obsidian remains registered");
        check(context, Blocks.OBSIDIAN.getBlastResistance() == 1200, "Vanilla blast resistance");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void statsDurabilityAndRepairIngredients(TestContext context) {
        for (Item item : ObsidianItems.ALL) {
            var stack = new ItemStack(item);
            check(context, stack.isDamageable() && item.getEnchantability() == 18, "Durable and enchantable: " + item);
            check(context, item.canRepair(stack, new ItemStack(Items.OBSIDIAN)), "Repairs with obsidian: " + item);
            check(context, !item.canRepair(stack, new ItemStack(Items.CRYING_OBSIDIAN)), "Crying obsidian is not ordinary obsidian");
            check(context, stack.contains(DataComponentTypes.FIRE_RESISTANT), "Equipment survives fire");
        }
        check(context, new ItemStack(ObsidianItems.PICKAXE).getMaxDamage() == 2300, "Tool durability");
        var armor = ObsidianItems.ARMOR_MATERIAL.value();
        check(context, armor.toughness() == 3.5F && armor.knockbackResistance() == 0.12F, "Armor material stats");
        check(context, ObsidianItems.ARMOR.stream().mapToInt(item -> ((ArmorItem) item).getProtection()).sum() == 24, "Total protection 24");
        check(context, ObsidianItems.PICKAXE.getMiningSpeed(new ItemStack(ObsidianItems.PICKAXE), Blocks.OBSIDIAN.getDefaultState()) == 10,
            "Correct mining efficiency");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void enchantmentTableAllEquipmentAndNoFireAspect(TestContext context) {
        var registry = context.getWorld().getRegistryManager();
        var candidates = registry.get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        var fire = enchantment(context, Enchantments.FIRE_ASPECT);
        for (Item item : ObsidianItems.ALL) {
            for (int seed = 0; seed < 48; seed++) {
                ItemStack enchanted = EnchantmentHelper.enchant(Random.create(seed), new ItemStack(item), 30, registry, candidates);
                check(context, enchanted.hasEnchantments(), "Level-30 table enchantment: " + item);
                if (item == ObsidianItems.SWORD) check(context, EnchantmentHelper.getLevel(fire, enchanted) == 0, "No generated Fire Aspect");
            }
        }
        check(context, !ObsidianItems.SWORD.canBeEnchantedWith(new ItemStack(ObsidianItems.SWORD), fire, EnchantingContext.PRIMARY), "Table disallows Fire Aspect");
        check(context, !ObsidianItems.SWORD.canBeEnchantedWith(new ItemStack(ObsidianItems.SWORD), fire, EnchantingContext.ACCEPTABLE), "Anvil disallows Fire Aspect");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void vanillaAnvilRepairForEveryItem(TestContext context) {
        var player = context.createMockPlayer(GameMode.SURVIVAL);
        player.experienceLevel = 100;
        for (Item item : ObsidianItems.ALL) {
            var anvil = new AnvilScreenHandler(1, player.getInventory());
            var damaged = new ItemStack(item);
            damaged.setDamage(damaged.getMaxDamage() - 1);
            anvil.getSlot(0).setStack(damaged);
            anvil.getSlot(1).setStack(new ItemStack(Items.OBSIDIAN));
            anvil.updateResult();
            var result = anvil.getSlot(2).getStack();
            check(context, result.isOf(item), "Anvil output: " + item);
            check(context, result.getDamage() == damaged.getDamage() - damaged.getMaxDamage() / 4, "One block repairs 25%: " + item);
            check(context, anvil.getLevelCost() > 0 && result.getOrDefault(DataComponentTypes.REPAIR_COST, 0) > 0, "Vanilla level/prior-work cost");
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void anvilRejectsFireAspectButAllowsSharpness(TestContext context) {
        var player = context.createMockPlayer(GameMode.SURVIVAL);
        player.experienceLevel = 100;
        var anvil = new AnvilScreenHandler(1, player.getInventory());
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(enchantment(context, Enchantments.FIRE_ASPECT), 2);
        anvil.getSlot(0).setStack(new ItemStack(ObsidianItems.SWORD));
        anvil.getSlot(1).setStack(book);
        anvil.updateResult();
        check(context, anvil.getSlot(2).getStack().isEmpty(), "Fire Aspect book rejected");
        var sharp = enchantment(context, Enchantments.SHARPNESS);
        book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(sharp, 3);
        anvil.getSlot(1).setStack(book);
        anvil.updateResult();
        check(context, EnchantmentHelper.getLevel(sharp, anvil.getSlot(2).getStack()) == 3, "Sharpness book accepted");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void creativeAnvilRejectsFireAspectAndKeepsOtherBookEnchantments(TestContext context) {
        var player = context.createMockPlayer(GameMode.CREATIVE);
        var anvil = new AnvilScreenHandler(1, player.getInventory());
        var fire = enchantment(context, Enchantments.FIRE_ASPECT);
        var sharp = enchantment(context, Enchantments.SHARPNESS);
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(fire, 2);
        anvil.getSlot(0).setStack(new ItemStack(ObsidianItems.SWORD));
        anvil.getSlot(1).setStack(book);
        anvil.updateResult();
        check(context, anvil.getSlot(2).getStack().isEmpty(), "Creative Fire Aspect book also rejected");
        book = book.copy();
        book.addEnchantment(sharp, 4);
        anvil.getSlot(1).setStack(book);
        anvil.updateResult();
        var output = anvil.getSlot(2).getStack();
        check(context, output.isOf(ObsidianItems.SWORD) && EnchantmentHelper.getLevel(sharp, output) == 4
            && EnchantmentHelper.getLevel(fire, output) == 0, "Mixed book applies allowed enchantment only");
        check(context, EnchantmentHelper.getEnchantments(anvil.getSlot(1).getStack()).getLevel(fire) == 2, "Input book is not mutated");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void playerHeadKeepsProfileAndDragonDropsHead(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        attacker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ObsidianItems.SWORD));
        var victim = context.createMockPlayer(GameMode.SURVIVAL);
        victim.setPosition(context.getAbsolutePos(new BlockPos(2, 2, 2)).toCenterPos());
        victim.damage(attacker.getDamageSources().playerAttack(attacker), 1000);
        var heads = context.getEntities(EntityType.ITEM).stream().map(ItemEntity::getStack)
            .filter(stack -> stack.isOf(Items.PLAYER_HEAD)).toList();
        check(context, heads.size() == 1, "Player head dropped");
        check(context, heads.getFirst().get(DataComponentTypes.PROFILE).gameProfile().getId().equals(victim.getUuid()), "Correct player profile");
        var dragon = context.spawnEntity(EntityType.ENDER_DRAGON, 3, 2, 3);
        dragon.setHealth(1);
        dragon.damagePart(dragon.head, attacker.getDamageSources().playerAttack(attacker), 1000);
        check(context, drops(context, Items.DRAGON_HEAD) == 1, "Dragon head dropped on lethal sword hit");
        dragon.discard();
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void vanillaPortalAndObsidianRecipesStillWork(TestContext context) {
        for (int x = 1; x <= 4; x++) for (int y = 1; y <= 5; y++) {
            context.setBlockState(x, y, 2, x == 1 || x == 4 || y == 1 || y == 5 ? Blocks.OBSIDIAN : Blocks.AIR);
        }
        var interior = context.getAbsolutePos(new BlockPos(2, 2, 2));
        var portal = net.minecraft.world.dimension.NetherPortal.getNewPortal(context.getWorld(), interior,
            net.minecraft.util.math.Direction.Axis.X);
        check(context, portal.isPresent(), "Vanilla obsidian frame accepted");
        portal.orElseThrow().createPortal();
        check(context, context.getWorld().getBlockState(interior).isOf(Blocks.NETHER_PORTAL), "Portal activates normally");
        var input = CraftingRecipeInput.create(3, 3, List.of(
            ItemStack.EMPTY, new ItemStack(Items.BOOK), ItemStack.EMPTY,
            new ItemStack(Items.DIAMOND), new ItemStack(Items.OBSIDIAN), new ItemStack(Items.DIAMOND),
            new ItemStack(Items.OBSIDIAN), new ItemStack(Items.OBSIDIAN), new ItemStack(Items.OBSIDIAN)));
        var recipe = context.getWorld().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, context.getWorld());
        check(context, recipe.isPresent() && recipe.orElseThrow().value().craft(input,
            context.getWorld().getRegistryManager()).isOf(Items.ENCHANTING_TABLE), "Original enchanting-table recipe preserved");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void burnCreditExpiresAndSurvivesEntitySave(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        attacker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ObsidianItems.SWORD));
        var victim = context.spawnMob(EntityType.ZOMBIE, 2, 2, 2);
        victim.damage(attacker.getDamageSources().playerAttack(attacker), 1);
        var data = new net.minecraft.nbt.NbtCompound();
        victim.writeCustomDataToNbt(data);
        var reloaded = context.spawnMob(EntityType.ZOMBIE, 3, 2, 2);
        reloaded.readCustomDataFromNbt(data);
        check(context, ObsidianCombat.qualifies(reloaded, reloaded.getDamageSources().onFire()), "Burn kill credit survives a chunk/entity save");
        ((ObsidianBurnTracker) reloaded).obsidian$setBurnDeadline(context.getWorld().getTime());
        check(context, !ObsidianCombat.qualifies(reloaded, reloaded.getDamageSources().onFire()), "Old burn credit expires");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void fullSetAndEachMissingPiece(TestContext context) {
        var wearer = context.spawnMob(EntityType.ZOMBIE, 2, 2, 2);
        wearer.setAiDisabled(true);
        for (EquipmentSlot missing : ARMOR_SLOTS) {
            equipFull(wearer);
            ObsidianSetBonus.update(wearer);
            check(context, wearer.hasStatusEffect(StatusEffects.FIRE_RESISTANCE), "Full set grants real fire immunity");
            check(context, wearer.getStatusEffect(ObsidianSetBonus.FIRE_RESISTANCE).isInfinite(), "Infinite equipment effect");
            float health = wearer.getHealth();
            wearer.damage(wearer.getDamageSources().lava(), 4);
            check(context, wearer.getHealth() == health, "Lava cannot damage full set");
            wearer.equipStack(missing, ItemStack.EMPTY);
            check(context, !wearer.hasStatusEffect(StatusEffects.FIRE_RESISTANCE), "Immunity removed immediately for " + missing);
            ObsidianSetBonus.update(wearer);
            check(context, !wearer.hasStatusEffect(ObsidianSetBonus.FIRE_RESISTANCE), "Equipment icon removed");
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void fullSetPreservesPotionAndGrantsAdvancement(TestContext context) {
        var player = context.createMockCreativeServerPlayerInWorld();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600));
        equipFull(player);
        ObsidianSetBonus.update(player);
        var advancement = context.getWorld().getServer().getAdvancementLoader().get(ObsidianEquipment.id("full_obsidian_armor"));
        check(context, advancement != null && player.getAdvancementTracker().getProgress(advancement).isDone(), "Full-set advancement granted");
        player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        ObsidianSetBonus.update(player);
        check(context, player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() == 600, "Potion unaffected by equipment removal");
        check(context, !player.hasStatusEffect(ObsidianSetBonus.FIRE_RESISTANCE), "Only equipment effect removed");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void directSwordKillsDropAvailableHeads(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        attacker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ObsidianItems.SWORD));
        Map<EntityType<? extends LivingEntity>, Item> cases = Map.of(
            EntityType.SKELETON, Items.SKELETON_SKULL, EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL,
            EntityType.ZOMBIE, Items.ZOMBIE_HEAD, EntityType.CREEPER, Items.CREEPER_HEAD, EntityType.PIGLIN, Items.PIGLIN_HEAD);
        for (var entry : cases.entrySet()) {
            var victim = context.spawnEntity(entry.getKey(), 2, 2, 2);
            int before = drops(context, entry.getValue());
            victim.damage(attacker.getDamageSources().playerAttack(attacker), 1000);
            check(context, drops(context, entry.getValue()) == before + 1, "Exactly one head for " + entry.getKey());
        }
        var cow = context.spawnEntity(EntityType.COW, 3, 2, 3);
        check(context, ObsidianCombat.headFor(cow).isEmpty(), "No invented head for cow");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void innateFireAndBurnKillCredit(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        attacker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ObsidianItems.SWORD));
        var zombie = context.spawnMob(EntityType.ZOMBIE, 2, 2, 2);
        zombie.setAiDisabled(true);
        zombie.damage(attacker.getDamageSources().playerAttack(attacker), 1);
        check(context, zombie.isOnFire(), "Unenchanted sword ignites victim");
        check(context, zombie.getFireTicks() >= 79, "Four-second fire duration");
        attacker.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        zombie.timeUntilRegen = 0;
        zombie.setHealth(1);
        int before = drops(context, Items.ZOMBIE_HEAD);
        zombie.damage(zombie.getDamageSources().onFire(), 2);
        check(context, drops(context, Items.ZOMBIE_HEAD) == before + 1, "Sword's burn still credits a head after switching items");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void witherSkeletonDoesNotDuplicateNaturalSkulls(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        var sword = new ItemStack(ObsidianItems.SWORD);
        sword.addEnchantment(enchantment(context, Enchantments.LOOTING), 10);
        attacker.equipStack(EquipmentSlot.MAINHAND, sword);
        int before = drops(context, Items.WITHER_SKELETON_SKULL);
        for (int i = 0; i < 64; i++) {
            var victim = context.spawnEntity(EntityType.WITHER_SKELETON, 2, 2, 2);
            victim.damage(attacker.getDamageSources().playerAttack(attacker), 1000);
        }
        check(context, drops(context, Items.WITHER_SKELETON_SKULL) == before + 64, "Guaranteed skull replaces chance-based skull instead of duplicating it");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void ordinaryWeaponDoesNotGuaranteeHead(TestContext context) {
        var attacker = context.createMockPlayer(GameMode.SURVIVAL);
        attacker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        var zombie = context.spawnEntity(EntityType.ZOMBIE, 2, 2, 2);
        int before = drops(context, Items.ZOMBIE_HEAD);
        zombie.damage(attacker.getDamageSources().playerAttack(attacker), 1000);
        check(context, drops(context, Items.ZOMBIE_HEAD) == before, "Normal weapons unchanged");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void ruinedPortalAlwaysContainsOneDamagedTool(TestContext context) {
        var table = context.getWorld().getServer().getReloadableRegistries().getLootTable(LootTables.RUINED_PORTAL_CHEST);
        var parameters = new LootContextParameterSet.Builder(context.getWorld())
            .add(LootContextParameters.ORIGIN, context.getAbsolutePos(BlockPos.ORIGIN).toCenterPos()).build(LootContextTypes.CHEST);
        var seen = new HashSet<Item>();
        for (long seed = 1; seed <= 256; seed++) {
            var loot = table.generateLoot(parameters, seed);
            var tools = loot.stream().filter(stack -> ObsidianItems.TOOLS.contains(stack.getItem())).toList();
            check(context, tools.size() == 1 && tools.getFirst().getCount() == 1, "One guaranteed tool at seed " + seed);
            var tool = tools.getFirst();
            int remaining = tool.getMaxDamage() - tool.getDamage();
            check(context, remaining >= 23 && remaining <= 116, "Only 1%-5% durability remains");
            check(context, loot.size() > 1, "Existing ruined portal loot retained");
            seen.add(tool.getItem());
        }
        check(context, seen.size() == 5, "All five tools can appear");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void masterVillagersReplaceExactlyOneOffer(TestContext context) {
        for (var profession : List.of(VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER)) {
            var villager = context.spawnEntity(EntityType.VILLAGER, 2, 2, 2);
            villager.setVillagerData(villager.getVillagerData().withProfession(profession).withLevel(5));
            var offers = villager.getOffers();
            long count = offers.stream().filter(offer -> ObsidianItems.ALL.contains(offer.getSellItem().getItem())).count();
            check(context, count == 1, "One replacement for " + profession);
            check(context, offers.size() == (profession == VillagerProfession.ARMORER ? 2 : 1), "No extra default master trades");
            for (var offer : offers) if (ObsidianItems.ALL.contains(offer.getSellItem().getItem())) {
                check(context, offer.getSellItem().hasEnchantments(), "Obsidian trade is enchanted");
                check(context, EnchantmentHelper.getLevel(enchantment(context, Enchantments.FIRE_ASPECT), offer.getSellItem()) == 0,
                    "No Fire Aspect on traded sword");
            }
            check(context, villager.getOffers().size() == offers.size(), "Reading trades is idempotent");
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void existingMasterPreservesPricesUsesAndOtherTrades(TestContext context) {
        var villager = context.spawnEntity(EntityType.VILLAGER, 2, 2, 2);
        villager.setVillagerData(villager.getVillagerData().withProfession(VillagerProfession.WEAPONSMITH).withLevel(5));
        var diamond = new ItemStack(Items.DIAMOND_SWORD);
        diamond.addEnchantment(enchantment(context, Enchantments.FIRE_ASPECT), 2);
        var offer = new TradeOffer(new TradedItem(Items.EMERALD, 20), Optional.empty(), diamond, 1, 3, 30, 0.2F, 4);
        offer.setSpecialPrice(-3);
        var other = new TradeOffer(new TradedItem(Items.EMERALD, 5), new ItemStack(Items.IRON_AXE), 4, 1, 0.05F);
        var offers = new TradeOfferList();
        offers.add(offer); offers.add(other);
        villager.setOffers(offers);
        var result = villager.getOffers();
        check(context, result.getFirst() == offer && offer.getSellItem().isOf(ObsidianItems.SWORD), "Existing offer updated in place");
        check(context, offer.getUses() == 1 && offer.getDemandBonus() == 4 && offer.getSpecialPrice() == -3
            && offer.getOriginalFirstBuyItem().getCount() == 20, "Price, demand, discount and uses preserved");
        check(context, offer.getSellItem().hasEnchantments(), "Only-Fire-Aspect diamond becomes valid enchanted obsidian sword");
        check(context, result.get(1) == other && other.getSellItem().isOf(Items.IRON_AXE), "Other trades untouched");
        context.complete();
    }
}
