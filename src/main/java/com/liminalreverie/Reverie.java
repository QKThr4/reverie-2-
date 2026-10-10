package com.liminalreverie;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Shared constants for the custom dimension. */
public final class Reverie {
    public static final ResourceKey<Level> KEY =
            ResourceKey.create(Registries.DIMENSION, id("the_reverie"));

    private Reverie() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(LiminalReverie.MODID, path);
    }

    public static ServerLevel level(MinecraftServer server) {
        return server.getLevel(KEY);
    }

    public static boolean in(Level level) {
        return level.dimension() == KEY;
    }
}
