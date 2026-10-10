package com.liminalreverie.block;

import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.StoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** A chalk-like symbol set into a wall. The block state stores which symbol (0..11) it shows. */
public class SymbolMarkBlock extends Block {
    public static final IntegerProperty SYMBOL = IntegerProperty.create("symbol", 0, 11);

    public SymbolMarkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SYMBOL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SYMBOL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (StoryManager.collectSymbol(sp, state.getValue(SYMBOL))) {
                level.playSound(null, pos, ModSounds.CHIME.get(), SoundSource.PLAYERS, 0.8F, 0.8F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
