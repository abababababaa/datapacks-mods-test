package dev.datapacksmods.portablecrafting.mixin;

import dev.datapacksmods.portablecrafting.CraftingLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique private static final Identifier PORTABLE_BACKGROUND =
        Identifier.of("portable_crafting_3x3", "textures/gui/inventory.png");
    @Shadow private float mouseX;
    @Shadow private float mouseY;

    protected InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void portablecrafting$widen(PlayerEntity player, CallbackInfo ci) {
        backgroundWidth = CraftingLayout.PANEL_WIDTH;
        titleX = CraftingLayout.GRID_X;
    }

    @ModifyConstant(method = {"init", "method_19891"}, constant = @Constant(intValue = 104))
    private int portablecrafting$bookButtonX(int original) { return 178; }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 379))
    private int portablecrafting$narrowThreshold(int original) { return 407; }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void portablecrafting$drawBackground(DrawContext context, float delta,
                                                int mouseX, int mouseY, CallbackInfo ci) {
        context.drawTexture(PORTABLE_BACKGROUND, x, y, 0, 0,
            backgroundWidth, backgroundHeight, 256, 256);
        InventoryScreen.drawEntity(context, x + 26, y + 8, x + 75, y + 78,
            30, 0.0625F, this.mouseX, this.mouseY, client.player);
        ci.cancel();
    }
}
