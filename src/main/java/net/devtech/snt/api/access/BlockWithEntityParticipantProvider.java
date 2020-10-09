package net.devtech.snt.api.access;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.util.participants.EmptyParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

/**
 * implement Participant directly on your BlockEntity class
 */
public interface BlockWithEntityParticipantProvider extends BlockParticipantProvider<Participant<?>> {
	@Override
	default @NotNull Participant<?> getParticipant(@Nullable BlockEntity entity, BlockState state, WorldAccess world, BlockPos pos, @Nullable Direction direction) {
		if (!this.getBlockEntityType().isInstance(entity)) {
			entity = world.getBlockEntity(pos);
		}

		return entity == null ? EmptyParticipant.INSTANCE : (Participant<?>) entity;
	}

	/**
	 * @return the class of the block entity of this class, for extra validation in case you don't trust other people
	 */
	default Class<?> getBlockEntityType() {
		return Object.class;
	}
}
