package com.liminalreverie.world;

import net.minecraft.core.BlockPos;

import java.util.List;

/** Registry of all areas. Position -> area lookup is a pure function of the X coordinate. */
public final class Areas {
    public static final List<AreaDef> ALL = List.of(
            LobbyArea.DEF, SchoolArea.DEF, PoolArea.DEF, HotelArea.DEF, MallArea.DEF, FieldsArea.DEF,
            LoungeArea.DEF, RooftopArea.DEF, TrainArea.DEF, BedroomArea.DEF, LastRoomArea.DEF);

    private Areas() {}

    public static AreaDef byId(String id) {
        for (AreaDef a : ALL) if (a.id.equals(id)) return a;
        return null;
    }

    public static AreaDef at(BlockPos pos) {
        int idx = Math.floorDiv(pos.getX() + 256, 512);
        for (AreaDef a : ALL) if (a.index == idx) return a;
        return null;
    }
}
