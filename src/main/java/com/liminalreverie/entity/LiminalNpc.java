package com.liminalreverie.entity;

import com.liminalreverie.story.Dialogue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * One class for all four characters. They are invulnerable, never despawn and never attack;
 * dialogue is deterministic and driven by saved story progress. The placement key + epoch let the
 * server discard duplicates (for example after /reverie reset).
 */
public class LiminalNpc extends PathfinderMob {
    private final NpcKind kind;
    private String placementKey = "";
    private int epoch = 0;

    public LiminalNpc(EntityType<? extends LiminalNpc> type, Level level, NpcKind kind) {
        super(type, level);
        this.kind = kind;
        this.setInvulnerable(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public NpcKind kind() {
        return kind;
    }

    public String placementKey() {
        return placementKey;
    }

    public int epoch() {
        return epoch;
    }

    public void setPlacement(String key, int epoch) {
        this.placementKey = key;
        this.epoch = epoch;
    }

    @Override
    protected void registerGoals() {
        if (kind != NpcKind.SLEEPER) {
            this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide && player instanceof ServerPlayer sp) {
            Dialogue.talk(sp, kind);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PlacementKey", placementKey);
        tag.putInt("StoryEpoch", epoch);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        placementKey = tag.getString("PlacementKey");
        epoch = tag.getInt("StoryEpoch");
    }
}
