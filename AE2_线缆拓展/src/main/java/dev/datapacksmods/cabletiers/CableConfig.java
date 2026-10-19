package dev.datapacksmods.cabletiers;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CableConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue BASIC_I;
    private static final ModConfigSpec.IntValue BASIC_III;
    private static final ModConfigSpec.IntValue DENSE_I;
    private static final ModConfigSpec.IntValue DENSE_III;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.comment("Capacity in AE2 DEFAULT mode. AE2 x2/x3/x4 and infinite modes remain supported.",
            "Tier II is AE2's original cable and is not overridden. Restart after editing.").push("channels");
        BASIC_I = builder.worldRestart().defineInRange("basicTier1", 4, 1, 8192);
        BASIC_III = builder.worldRestart().defineInRange("basicTier3", 16, 1, 8192);
        DENSE_I = builder.worldRestart().defineInRange("denseTier1", 16, 1, 8192);
        DENSE_III = builder.worldRestart().defineInRange("denseTier3", 64, 1, 8192);
        builder.pop();
        SPEC = builder.build();
    }
    public static int baseCapacity(CableKind kind, int tier) {
        if (tier == 2) return kind.dense ? 32 : 8;
        var value = kind.dense ? (tier == 1 ? DENSE_I : DENSE_III) : (tier == 1 ? BASIC_I : BASIC_III);
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
    private CableConfig() {}
}
