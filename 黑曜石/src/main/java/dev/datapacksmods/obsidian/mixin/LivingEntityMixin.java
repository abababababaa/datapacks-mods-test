package dev.datapacksmods.obsidian.mixin;

import dev.datapacksmods.obsidian.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ObsidianBurnTracker {
    @Unique private long obsidian$burnDeadline = Long.MIN_VALUE;
    @Unique private boolean obsidian$headDropped;
    @Unique private StatusEffectInstance obsidian$virtualFireResistance;
    @Override public long obsidian$getBurnDeadline() { return obsidian$burnDeadline; }
    @Override public void obsidian$setBurnDeadline(long tick) { obsidian$burnDeadline = tick; }
    @Override public boolean obsidian$claimHeadDrop() {
        if (obsidian$headDropped) return false;
        obsidian$headDropped = true;
        return true;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void obsidian$ignite(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.getWorld().isClient && amount > 0 && ObsidianCombat.isSwordAttack(source)) {
            obsidian$burnDeadline = self.getWorld().getTime() + 80;
            self.setOnFireFor(4);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void obsidian$updateSetBonus(CallbackInfo ci) {
        ObsidianSetBonus.update((LivingEntity) (Object) this);
    }

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void obsidian$fireResistance(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.equals(StatusEffects.FIRE_RESISTANCE)
            && ObsidianSetBonus.hasFullSet((LivingEntity) (Object) this)) cir.setReturnValue(true);
    }

    @Inject(method = "getStatusEffect", at = @At("RETURN"), cancellable = true)
    private void obsidian$fireResistanceInstance(RegistryEntry<StatusEffect> effect,
                                                CallbackInfoReturnable<StatusEffectInstance> cir) {
        // Keep hasStatusEffect/getStatusEffect consistent for vanilla and other consumers.
        // A real potion instance takes priority and is never overwritten or extended.
        if (cir.getReturnValue() == null && effect.equals(StatusEffects.FIRE_RESISTANCE)
            && ObsidianSetBonus.hasFullSet((LivingEntity) (Object) this)) {
            if (obsidian$virtualFireResistance == null) {
                obsidian$virtualFireResistance = new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,
                    StatusEffectInstance.INFINITE, 0, true, false, false);
            }
            cir.setReturnValue(obsidian$virtualFireResistance);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void obsidian$saveBurnCredit(NbtCompound nbt, CallbackInfo ci) {
        if (obsidian$burnDeadline > ((LivingEntity) (Object) this).getWorld().getTime()) {
            nbt.putLong("obsidian_equipment:burn_deadline", obsidian$burnDeadline);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void obsidian$loadBurnCredit(NbtCompound nbt, CallbackInfo ci) {
        obsidian$burnDeadline = nbt.contains("obsidian_equipment:burn_deadline")
            ? nbt.getLong("obsidian_equipment:burn_deadline") : Long.MIN_VALUE;
    }

    @Redirect(method = "dropLoot", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"))
    private void obsidian$avoidDuplicateHeads(LootTable table, LootContextParameterSet parameters, long seed,
                                             Consumer<ItemStack> consumer, DamageSource source, boolean causedByPlayer) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack guaranteed = ObsidianCombat.qualifies(self, source) ? ObsidianCombat.headFor(self) : ItemStack.EMPTY;
        table.generateLoot(parameters, seed, stack -> {
            if (guaranteed.isEmpty() || !stack.isOf(guaranteed.getItem())) consumer.accept(stack);
        });
    }
}
