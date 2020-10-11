package net.devtech.snt.api;

import java.util.List;

import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.access.EntityParticipantProvider;
import net.devtech.snt.api.access.ItemParticipantProvider;
import net.devtech.snt.api.concrete.Supported;
import net.devtech.snt.api.concrete.WildParticipant;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * do not pass null participants anywhere {@link EmptyParticipant#INSTANCE}
 *
 * @param <T> the <b>assumed immutable</b> data type of this participant
 *
 * 		This is the top level interface of this api, and is intentionally opaque, however the more context you can
 * 		provide
 * 		the better.
 *
 * 		For example pipes can't know what blocks to connect to unless you implement:
 * @see Supported
 * @see WildParticipant
 */
public interface Participant<T> {
	/**
	 * todo Cauldron
	 * todo per item participants
	 * todo per block participants
	 */



	/**
	 * A participant that reverts it's in-world state when the transaction is aborted rather than storing it's state in
	 * the transaction itself
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
	 * universally recognized types: {@link Fluid} and {@link Item}. Fluids are in Drops, Items are in... items. for
	 * ItemStacks: {@link WildParticipant}
	 *
	 * @param transaction the current transaction
	 * @param type the type being taken
	 * @param amount the amount to take, must be positive.
	 * @return the amount taken, must be lower than or equal to amount
	 * @see net.devtech.snt.api.util.FluidUtil
	 */
	@Range (from = 0, to = Integer.MAX_VALUE) int take(Transaction transaction,
			Object type,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount);

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
	@Range (from = 0, to = Integer.MAX_VALUE) int push(Transaction transaction,
			Object type,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount);


	/**
	 * called when any level transaction is aborted.
	 */
	default void onAbort(T obj) {}

	/**
	 * called when a top level transaction is confirmed
	 *
	 * @see #postCommit(T)
	 */
	void onCommit(T obj);

	/**
	 * this method is reserved for BlockState-implemented containers. in the onCommit function, change the
	 * blockstate of
	 * your container, <b>without</b> block updates, and then in this function propagate the updates to neighbors. The
	 * reason is because a change in one block's blockstate can influence another's, for example a cauldron changing
	 * states could power a BUDed piston, and cause it to push/pull a second cauldron out of the way, so when that
	 * second cauldron recieves it's onCommit function, it doesn't know where it's cauldron is. This is to prevent
	 * duplication glitches.
	 */
	default void postCommit(T obj) {}

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
	 * this function can be used by block participants to avoid making duplicate data for confirmation based
	 * participants
	 */
	default boolean isEqual(Participant<?> other) {
		return this.equals(other);
	}
}
