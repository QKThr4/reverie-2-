package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.story.StoryData;

import java.util.function.Predicate;

/** Where and when a unique character appears inside an area (coordinates relative to the area origin). */
public record NpcPlacement(NpcKind kind, double x, double y, double z, float yaw, String name,
                           Predicate<StoryData> cond, boolean sleeping) {}
