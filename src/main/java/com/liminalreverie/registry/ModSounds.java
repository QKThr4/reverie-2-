package com.liminalreverie.registry;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.Reverie;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom sound events. Each one is mapped in assets/liminalreverie/sounds.json to a (quiet, pitched)
 * alias of a vanilla sound event, so the mod ships no audio files of its own.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, LiminalReverie.MODID);

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Reverie.id(name)));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_LOBBY = reg("ambient_lobby");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_SCHOOL = reg("ambient_school");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_POOL = reg("ambient_pool");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_MALL = reg("ambient_mall");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_FIELDS = reg("ambient_fields");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_HOTEL = reg("ambient_hotel");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMB_SAFE = reg("ambient_safe");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHIME = reg("chime");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRONG = reg("wrong");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLVE = reg("solve");
    public static final DeferredHolder<SoundEvent, SoundEvent> BELL = reg("bell");
    public static final DeferredHolder<SoundEvent, SoundEvent> DING = reg("ding");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP = reg("step");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUGH = reg("laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = reg("wind");
}
