package dev.datapacksmods.obsidian.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import java.util.EnumMap;

/** Actual block texture, sampled on geometric armor shells; resource-pack obsidian textures also work. */
public final class ObsidianArmorRenderer implements ArmorRenderer {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/block/obsidian.png");
    private final EnumMap<EquipmentSlot, BipedEntityModel<LivingEntity>> models = new EnumMap<>(EquipmentSlot.class);

    @Override public void render(MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack,
                                 LivingEntity entity, EquipmentSlot slot, int light,
                                 BipedEntityModel<LivingEntity> contextModel) {
        BipedEntityModel<LivingEntity> model = models.computeIfAbsent(slot, ObsidianArmorRenderer::createModel);
        contextModel.copyBipedStateTo(model);
        model.setVisible(false);
        switch (slot) {
            case HEAD -> model.head.visible = true;
            case CHEST -> {
                model.body.visible = true;
                model.leftArm.visible = true;
                model.rightArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.leftLeg.visible = true;
                model.rightLeg.visible = true;
            }
            case FEET -> {
                model.leftLeg.visible = true;
                model.rightLeg.visible = true;
            }
            default -> { return; }
        }
        ArmorRenderer.renderPart(matrices, consumers, light, stack, model, TEXTURE);
    }

    public static BipedEntityModel<LivingEntity> createModel(EquipmentSlot slot) {
        var data = BipedEntityModel.getModelData(Dilation.NONE, 0);
        var root = data.getRoot();
        root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
        if (slot == EquipmentSlot.HEAD) {
            // Top, sides, back, and brow leave the player's face open without an edited texture.
            var helmet = ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4.5F, -8.5F, -4.5F, 9, 2, 9)
                .uv(0, 0).cuboid(-4.5F, -6.5F, 3.5F, 9, 6.5F, 1)
                .uv(0, 0).cuboid(-4.5F, -6.5F, -4.5F, 1, 6.5F, 8)
                .uv(0, 0).cuboid(3.5F, -6.5F, -4.5F, 1, 6.5F, 8)
                .uv(0, 0).cuboid(-3.5F, -6.5F, -4.5F, 7, 1, 1);
            root.addChild(EntityModelPartNames.HEAD, helmet, ModelTransform.NONE);
        } else if (slot == EquipmentSlot.CHEST) {
            root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4.7F, -0.7F, -2.7F, 9.4F, 12.4F, 5.4F), ModelTransform.NONE);
            root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(0, 0)
                .cuboid(-3.6F, -2.6F, -2.6F, 5.2F, 8.8F, 5.2F), ModelTransform.pivot(-5, 2, 0));
            root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 0)
                .cuboid(-1.6F, -2.6F, -2.6F, 5.2F, 8.8F, 5.2F), ModelTransform.pivot(5, 2, 0));
        } else if (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
            boolean boots = slot == EquipmentSlot.FEET;
            root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4.3F, 10, -2.3F, 8.6F, 2.5F, 4.6F), ModelTransform.NONE);
            float radius = boots ? 2.6F : 2.3F;
            float y = boots ? 8.2F : 0;
            float height = boots ? 4.3F : 9.8F;
            for (boolean left : new boolean[]{false, true}) {
                root.addChild(left ? EntityModelPartNames.LEFT_LEG : EntityModelPartNames.RIGHT_LEG,
                    ModelPartBuilder.create().uv(0, 0).cuboid(-radius, y, -radius, radius * 2, height, radius * 2),
                    ModelTransform.pivot(left ? 1.9F : -1.9F, 12, 0));
            }
        }
        return new BipedEntityModel<>(TexturedModelData.of(data, 16, 16).createModel());
    }
}
