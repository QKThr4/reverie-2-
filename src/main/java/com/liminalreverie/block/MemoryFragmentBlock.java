package com.liminalreverie.block;

import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.StoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A glowing memory. The block state stores which memory (0..31) it contains. Right click to remember it. */
public class MemoryFragmentBlock extends Block {
    public static final IntegerProperty MEMORY = IntegerProperty.create("memory", 0, 31);
    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public MemoryFragmentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MEMORY, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MEMORY);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            int id = state.getValue(MEMORY) + 1;
            if (StoryManager.collectMemory(sp, id)) {
                sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.25, 0.25, 0.25, 0.02);
                level.playSound(null, pos, ModSounds.CHIME.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
                level.removeBlock(pos, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
