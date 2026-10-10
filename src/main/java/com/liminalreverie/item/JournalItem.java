package com.liminalreverie.item;

import com.liminalreverie.story.Fx;
import com.liminalreverie.story.StoryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Right click asks the server for the journal data; the server answers with a packet that opens the screen. */
public class JournalItem extends Item {
    public JournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (StoryManager.canOpenJournal(sp)) StoryManager.sendJournal(sp, true);
            else Fx.hint(sp, Component.translatable("lr.msg.journal_locked"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
