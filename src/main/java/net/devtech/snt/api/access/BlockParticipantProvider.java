package net.devtech.snt.api.access;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.util.participants.MojangFluidBlockParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

/**
 * This must be implemented on the Block, not the BlockEntity.
 * already implemented on {@link InventoryProvider}
 * @see MojangFluidBlockParticipant
 */
public interface BlockParticipantProvider<T extends Participant<?>> {
	/**
	 * get the participant for the given side of the block
	 * @param entity should only be null if state#getBlock#hasBlockEntity is false
	 * @param direction the side to access it from, if null assume input inventory
	 */
	@NotNull
	T getParticipant(@Nullable BlockEntity entity, BlockState state, BlockView world, BlockPos pos, @Nullable Direction direction);
}
