package dev.datapacksmods.portablecrafting.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Creative mode keeps its own UI; hide the extra crafting slots just like vanilla inputs. */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<ScreenHandler> {
    @Shadow protected abstract boolean isInventoryTabSelected();

    protected CreativeInventoryScreenMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void portablecrafting$hideExtraInputs(ItemGroup group, CallbackInfo ci) {
        if (!isInventoryTabSelected()) return;
        for (int slotId = 46; slotId <= 50 && slotId < handler.slots.size(); slotId++) {
            ((SlotAccessor) handler.slots.get(slotId)).portablecrafting$setX(-2000);
            ((SlotAccessor) handler.slots.get(slotId)).portablecrafting$setY(-2000);
        }
    }
}
