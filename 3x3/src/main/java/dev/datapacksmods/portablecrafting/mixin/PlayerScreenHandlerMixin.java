package dev.datapacksmods.portablecrafting.mixin;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {
    @Shadow @Final private RecipeInputInventory craftingInput;

    protected PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/inventory/CraftingInventory;<init>(Lnet/minecraft/screen/ScreenHandler;II)V"))
    private void portablecrafting$expandInput(Args args) {
        args.set(1, 3);
        args.set(2, 3);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void portablecrafting$addSlots(PlayerInventory inventory, boolean onServer,
                                          PlayerEntity owner, CallbackInfo ci) {
        // Existing slots 1..4 still reference input indices 0..3.
        for (int inputIndex = 4; inputIndex < CraftingLayout.INPUT_COUNT; inputIndex++) {
            addSlot(new Slot(craftingInput, inputIndex,
                CraftingLayout.gridX(inputIndex), CraftingLayout.gridY(inputIndex)));
        }
        for (int inputIndex = 0; inputIndex < CraftingLayout.INPUT_COUNT; inputIndex++) {
            Slot slot = getSlot(CraftingLayout.recipeSlotId(inputIndex + 1));
            ((SlotAccessor) slot).portablecrafting$setX(CraftingLayout.gridX(inputIndex));
            ((SlotAccessor) slot).portablecrafting$setY(CraftingLayout.gridY(inputIndex));
        }
        ((SlotAccessor) getSlot(0)).portablecrafting$setX(CraftingLayout.RESULT_X);
        ((SlotAccessor) getSlot(0)).portablecrafting$setY(CraftingLayout.RESULT_Y);
    }

    @Inject(method = "getCraftingSlotCount", at = @At("HEAD"), cancellable = true)
    private void portablecrafting$slotCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(10);
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void portablecrafting$quickMoveExtraInputs(PlayerEntity player, int slotId,
                                                     CallbackInfoReturnable<ItemStack> cir) {
        if (slotId < 46 || slotId > 50) return;
        Slot slot = getSlot(slotId);
        if (!slot.hasStack()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (!insertItem(stack, 9, 45, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        slot.onTakeItem(player, stack);
        cir.setReturnValue(original);
    }
}
