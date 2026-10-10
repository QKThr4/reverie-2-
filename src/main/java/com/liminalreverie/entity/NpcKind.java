package com.liminalreverie.entity;

import com.liminalreverie.registry.ModEntities;
import net.minecraft.world.entity.EntityType;

/** The four unique characters. Dialogue line counts are used for the journal progress display. */
public enum NpcKind {
    CARETAKER("caretaker", 6),
    CHILD("child", 6),
    WANDERER("wanderer", 7),
    SLEEPER("sleeper", 4);

    public final String id;
    public final int lines;

    NpcKind(String id, int lines) {
        this.id = id;
        this.lines = lines;
    }

    /** Registry-backed entity type. Never resolved before registries are populated. */
    public EntityType<LiminalNpc> type() {
        return switch (this) {
            case CARETAKER -> ModEntities.CARETAKER.get();
            case CHILD -> ModEntities.CHILD.get();
            case WANDERER -> ModEntities.WANDERER.get();
            case SLEEPER -> ModEntities.SLEEPER.get();
        };
    }

    public static NpcKind byId(String id) {
        for (NpcKind k : values()) {
            if (k.id.equals(id)) return k;
        }
        return null;
    }
}
