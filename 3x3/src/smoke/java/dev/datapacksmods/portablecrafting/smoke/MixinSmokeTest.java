package dev.datapacksmods.portablecrafting.smoke;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

/** Verify every injection against the real Minecraft bytecode before opening a window/server. */
public final class MixinSmokeTest implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        if (!Boolean.getBoolean("portablecrafting.smoke")) return;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            verify("net.minecraft.screen.slot.Slot", loader);
            verify("net.minecraft.screen.PlayerScreenHandler", loader);
            verify("net.minecraft.recipe.InputSlotFiller", loader);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                verify("net.minecraft.client.gui.screen.ingame.InventoryScreen", loader);
                verify("net.minecraft.client.gui.screen.recipebook.RecipeBookWidget", loader);
                verify("net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen", loader);
            }
            SharedConstants.createGameVersion();
            Bootstrap.initialize();
            CraftingSmokeChecks.run();
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientSmokeChecks.run();
            }
            System.out.println("PORTABLE_CRAFTING_MIXIN_SMOKE_PASSED "
                + FabricLoader.getInstance().getEnvironmentType());
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void verify(String name, ClassLoader loader) throws Exception {
        Class<?> target = Class.forName(name, false, loader);
        target.getDeclaredMethods(); // Resolve method descriptors and finish verification.
        System.out.println("Verified transformed class: " + name);
    }
}
