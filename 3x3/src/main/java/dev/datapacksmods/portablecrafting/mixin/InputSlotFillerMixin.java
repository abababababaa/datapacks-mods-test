package dev.datapacksmods.portablecrafting.mixin;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Recipe-book autofill uses contiguous ordinals; network IDs stay vanilla-compatible. */
@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {
    @Redirect(method = "*", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/screen/AbstractRecipeScreenHandler;getSlot(I)Lnet/minecraft/screen/slot/Slot;"))
    private Slot portablecrafting$resolveRecipeSlot(AbstractRecipeScreenHandler<?, ?> handler, int ordinal) {
        return handler.getSlot(handler instanceof PlayerScreenHandler
            ? CraftingLayout.recipeSlotId(ordinal) : ordinal);
    }
}
