package dev.datapacksmods.portablecrafting.mixin;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
    @Shadow protected AbstractRecipeScreenHandler<?, ?> craftingScreenHandler;

    @Redirect(method = "acceptAlignedInput", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;"))
    private Object portablecrafting$ghostSlot(DefaultedList<?> slots, int ordinal) {
        if (craftingScreenHandler instanceof PlayerScreenHandler
                && slots == craftingScreenHandler.slots) {
            return slots.get(CraftingLayout.recipeSlotId(ordinal));
        }
        return slots.get(ordinal);
    }

    @Redirect(method = "slotClicked", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
        target = "Lnet/minecraft/screen/slot/Slot;id:I"))
    private int portablecrafting$identifyInput(Slot slot) {
        return craftingScreenHandler instanceof PlayerScreenHandler
            ? CraftingLayout.recipeOrdinal(slot.id) : slot.id;
    }
}
