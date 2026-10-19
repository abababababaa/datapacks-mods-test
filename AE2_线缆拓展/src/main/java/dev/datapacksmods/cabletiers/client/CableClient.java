package dev.datapacksmods.cabletiers.client;

import appeng.client.render.StaticItemColor;
import dev.datapacksmods.cabletiers.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CableTiers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CableClient {
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) {
        CableItems.VARIANTS.values().forEach(holder -> {
            var color = new StaticItemColor(holder.get().getColor());
            event.register((stack, index) -> color.getColor(stack, index) | 0xff000000, holder.get());
        });
    }
}
