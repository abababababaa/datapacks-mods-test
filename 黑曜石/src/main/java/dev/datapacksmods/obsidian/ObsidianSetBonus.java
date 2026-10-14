package dev.datapacksmods.obsidian;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ObsidianSetBonus {
    public static final RegistryEntry<StatusEffect> FIRE_RESISTANCE = Registry.registerReference(
        Registries.STATUS_EFFECT, ObsidianEquipment.id("obsidian_fire_resistance"),
        new SetEffect());

    private static final class SetEffect extends StatusEffect {
        private SetEffect() { super(StatusEffectCategory.BENEFICIAL, 0xE49A3A); }
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return entity.getEquippedStack(EquipmentSlot.HEAD).isOf(ObsidianItems.HELMET)
            && entity.getEquippedStack(EquipmentSlot.CHEST).isOf(ObsidianItems.CHESTPLATE)
            && entity.getEquippedStack(EquipmentSlot.LEGS).isOf(ObsidianItems.LEGGINGS)
            && entity.getEquippedStack(EquipmentSlot.FEET).isOf(ObsidianItems.BOOTS);
    }

    public static void update(LivingEntity entity) {
        if (entity.getWorld().isClient) return;
        if (hasFullSet(entity)) {
            if (!entity.hasStatusEffect(FIRE_RESISTANCE)) {
                entity.addStatusEffect(new StatusEffectInstance(FIRE_RESISTANCE,
                    StatusEffectInstance.INFINITE, 0, true, false, true));
            }
            if (entity instanceof ServerPlayerEntity player) {
                var advancement = player.getServer().getAdvancementLoader().get(ObsidianEquipment.id("full_obsidian_armor"));
                if (advancement != null && !player.getAdvancementTracker().getProgress(advancement).isDone()) {
                    player.getAdvancementTracker().grantCriterion(advancement, "wear_full_set");
                }
            }
        } else {
            entity.removeStatusEffect(FIRE_RESISTANCE);
        }
    }
    public static void initialize() {}
    private ObsidianSetBonus() {}
}
