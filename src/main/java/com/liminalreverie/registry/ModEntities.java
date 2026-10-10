package com.liminalreverie.registry;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.entity.NpcKind;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All NPC entity types are registered here through DeferredRegister. Nothing is ever spawned by numeric id. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, LiminalReverie.MODID);

    private ModEntities() {}

    private static DeferredHolder<EntityType<?>, EntityType<LiminalNpc>> npc(NpcKind kind, float w, float h) {
        return ENTITIES.register(kind.id, () -> EntityType.Builder
                .<LiminalNpc>of((type, level) -> new LiminalNpc(type, level, kind), MobCategory.MISC)
                .sized(w, h).clientTrackingRange(10).build(kind.id));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<LiminalNpc>> CARETAKER = npc(NpcKind.CARETAKER, 0.6F, 1.95F);
    public static final DeferredHolder<EntityType<?>, EntityType<LiminalNpc>> CHILD = npc(NpcKind.CHILD, 0.4F, 1.2F);
    public static final DeferredHolder<EntityType<?>, EntityType<LiminalNpc>> WANDERER = npc(NpcKind.WANDERER, 0.6F, 1.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<LiminalNpc>> SLEEPER = npc(NpcKind.SLEEPER, 0.6F, 1.8F);
}
