package com.liminalreverie.client;

import com.liminalreverie.Reverie;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.entity.NpcKind;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/** Renders all characters with the standard humanoid model and one generated skin per character. */
public class NpcRenderer extends HumanoidMobRenderer<LiminalNpc, HumanoidModel<LiminalNpc>> {
    private static final Map<NpcKind, ResourceLocation> TEXTURES = new EnumMap<>(NpcKind.class);

    public NpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(LiminalNpc npc) {
        return TEXTURES.computeIfAbsent(npc.kind(), k -> Reverie.id("textures/entity/" + k.id + ".png"));
    }

    @Override
    protected void scale(LiminalNpc npc, PoseStack pose, float partialTick) {
        if (npc.kind() == NpcKind.CHILD) pose.scale(0.62F, 0.62F, 0.62F);
    }
}
