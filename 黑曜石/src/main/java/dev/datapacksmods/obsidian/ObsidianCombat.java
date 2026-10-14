package dev.datapacksmods.obsidian;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.GameRules;
import java.util.Map;

public final class ObsidianCombat {
    private static final Map<EntityType<?>, Item> HEADS = Map.of(
        EntityType.SKELETON, Items.SKELETON_SKULL,
        EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL,
        EntityType.ZOMBIE, Items.ZOMBIE_HEAD,
        EntityType.CREEPER, Items.CREEPER_HEAD,
        EntityType.PIGLIN, Items.PIGLIN_HEAD,
        EntityType.ENDER_DRAGON, Items.DRAGON_HEAD);

    public static boolean isSwordAttack(DamageSource source) {
        return source.getAttacker() instanceof LivingEntity attacker
            && source.getSource() == attacker
            && attacker.getMainHandStack().isOf(ObsidianItems.SWORD)
            && (source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.MOB_ATTACK));
    }

    public static boolean qualifies(LivingEntity victim, DamageSource source) {
        return isSwordAttack(source) || (source.isOf(DamageTypes.ON_FIRE)
            && ((ObsidianBurnTracker) victim).obsidian$getBurnDeadline() > victim.getWorld().getTime());
    }

    public static ItemStack headFor(LivingEntity victim) {
        if (victim instanceof PlayerEntity player) {
            var head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            return head;
        }
        Item head = HEADS.get(victim.getType());
        return head == null ? ItemStack.EMPTY : new ItemStack(head);
    }

    public static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim.getWorld().isClient || !qualifies(victim, source)
            || !victim.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) return;
        ItemStack head = headFor(victim);
        if (!head.isEmpty() && ((ObsidianBurnTracker) victim).obsidian$claimHeadDrop()) victim.dropStack(head);
    }
    private ObsidianCombat() {}
}
