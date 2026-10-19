package dev.datapacksmods.cabletiers;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEParts;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.networking.*;

public enum CableKind {
    GLASS("glass", "glass_cable", false),
    COVERED("covered", "covered_cable", false),
    SMART("smart", "smart_cable", false),
    DENSE_COVERED("dense_covered", "covered_dense_cable", true),
    DENSE_SMART("dense_smart", "smart_dense_cable", true);

    public final String id;
    public final String aeSuffix;
    public final boolean dense;
    CableKind(String id, String aeSuffix, boolean dense) { this.id = id; this.aeSuffix = aeSuffix; this.dense = dense; }

    public ColoredPartItem<? extends CablePart> vanilla(AEColor color) {
        return switch (this) {
            case GLASS -> AEParts.GLASS_CABLE.item(color);
            case COVERED -> AEParts.COVERED_CABLE.item(color);
            case SMART -> AEParts.SMART_CABLE.item(color);
            case DENSE_COVERED -> AEParts.COVERED_DENSE_CABLE.item(color);
            case DENSE_SMART -> AEParts.SMART_DENSE_CABLE.item(color);
        };
    }
    public Class<? extends CablePart> partClass() {
        return switch (this) {
            case GLASS -> GlassCablePart.class;
            case COVERED -> CoveredCablePart.class;
            case SMART -> SmartCablePart.class;
            case DENSE_COVERED -> CoveredDenseCablePart.class;
            case DENSE_SMART -> SmartDenseCablePart.class;
        };
    }
    public CablePart create(ColoredPartItem<?> item) {
        return switch (this) {
            case GLASS -> new GlassCablePart(item);
            case COVERED -> new CoveredCablePart(item);
            case SMART -> new SmartCablePart(item);
            case DENSE_COVERED -> new CoveredDenseCablePart(item);
            case DENSE_SMART -> new SmartDenseCablePart(item);
        };
    }
}
