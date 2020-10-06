package net.devtech.snt.api;

import java.util.List;

import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.access.EntityParticipantProvider;
import net.devtech.snt.api.util.data.Mutable;
import net.devtech.snt.api.util.participants.EmptyParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.EntityView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * do not pass null participants anywhere {@link EmptyParticipant#INSTANCE}
 *
 * @param <T> the <b>assumed immutable</b> data type of this participant
 *
 * 		This is the top level interface of this api, and is intentionally opaque, however the more context you can provide the better.
 *
 * 		For example pipes can't know what blocks to connect to unless you implement:
 * @see Supported
 * @see WildParticipant
 */
public interface Participant<T> extends InternalParticipant {
	/**
	 * todo Cauldron
	 * todo per item participants
	 * todo per block participants
	 */

	/**
	 * @return the participant at the given location, accessed from the given face
	 */
	static Participant<?> getParticipantAt(WorldAccess world, BlockPos pos, @Nullable Direction direction) {
		Chunk chunk = world.getChunk(pos);
		BlockState state = chunk.getBlockState(pos);
		if(state.getBlock().hasBlockEntity()) {
			return getParticipantAt(chunk.getBlockEntity(pos), state, world, pos, direction);
		}
		return getParticipantAt(null, state, world, pos, direction);
	}

	/**
	 *
	 * @return the participant at the given location, accessed from the given face
	 */
	static Participant<?> getParticipantAt(@NotNull BlockState state, BlockView world, BlockPos pos, @Nullable Direction direction) {
		if(state.getBlock().hasBlockEntity()) {
			return getParticipantAt(world.getBlockEntity(pos), state, world, pos, direction);
		}
		return getParticipantAt(null, state, world, pos, direction);
	}

	/**
	 * @param entity should only be null if state#getBlock#hasBlockEntity is false
	 * @return the participant at the given location, accessed from the given face
	 */
	@NotNull
	static Participant<?> getParticipantAt(@Nullable BlockEntity entity, BlockState state, BlockView world, BlockPos pos, @Nullable Direction direction) {
		Block block = state.getBlock();
		if(block instanceof BlockParticipantProvider) {
			return ((BlockParticipantProvider)block).getParticipant(entity, state, world, pos, direction);
		}

		if(entity instanceof Participant) {
			return (Participant<?>) entity;
		}

		if(world instanceof EntityView) {
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
			List<Entity> list = ((EntityView) world).getOtherEntities(null, new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), e -> e instanceof EntityParticipantProvider);
			for (Entity e : list) {
				EntityParticipantProvider provider = (EntityParticipantProvider) e;
				return provider.getParticipant(direction);
			}
		}

		return EmptyParticipant.INSTANCE;
	}

	/**
	 * A participant that reverts it's in-world state when the transaction is aborted rather than storing it's state in the transaction itself
	 */
	interface Reversion<T> extends Participant<T> {
		@Override
		void onAbort(T obj);

		@Override
		default void onCommit(T obj) {}
	}

	/**
	 * take an amount of something from the participant.
	 *
	 * universally recognized types: {@link Fluid} and {@link Item}.
	 * Fluids are in Drops, Items are in... items.
	 * for ItemStacks: {@link WildParticipant}
	 *
	 * @see net.devtech.snt.api.util.FluidUtil
	 * @param transaction the current transaction
	 * @param type the type being taken
	 * @param amount the amount to take, must be positive.
	 * @return the amount taken, must be lower than or equal to amount
	 */
	@Range(from = 0, to = Integer.MAX_VALUE)
	int take(Transaction transaction, Object type, @Range (from = 0, to = Integer.MAX_VALUE) int amount);

	/**
	 * put an amount of something into the participant. todo immutable interface implemented on itemstack?
	 *
	 * universally recognized types: {@link Fluid} and {@link ItemStack}
	 * <b>if ItemStack, ignore {@link ItemStack#getCount()}</b>
	 * <b>DO NOT MUTATE THE ITEMSTACK</b>
	 *
	 * @param transaction the transaction
	 * @param type the object and it's amount being added
	 * @param amount the amount to add, must be positive
	 * @return the amount remaining after the addition, must be lower than or equal to amount
	 */
	@Range(from = 0, to = Integer.MAX_VALUE)
	int push(Transaction transaction, Object type, @Range(from = 0, to = Integer.MAX_VALUE) int amount);


	/**
	 * called when any level transaction is aborted.
	 */
	default void onAbort(T obj) {}

	/**
	 * called when a top level transaction is confirmed
	 */
	void onCommit(T obj);

	/**
	 * assuming T is immutable
	 */
	default T copyTransactionData(T data) {
		if (data instanceof Mutable) {
			return (T) ((Mutable) data).copy();
		}
		return data;
	}


	/**
	 * this function can be used by block participants to avoid making duplicate data for confirmation based participants
	 */
	default boolean isEqual(Participant<?> other) {
		return this.equals(other);
	}
}
