package net.devtech.fixedfluids.api;


import net.devtech.fixedfluids.api.util.Transaction;
import org.jetbrains.annotations.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

/**
 * a participant in a transaction
 *
 * @param <T> the data type (immutable)
 * @see ItemWildcardParticipant
 * @see FluidWildcardParticipant
 */
public interface Participant<T> {
	/**
	 * @param <T> a mutable type
	 */
	interface Mut<T> extends Participant<T> {
		@Override
		T copy(T data);
	}

	/**
	 * a participant based on reversion, this means changes to the participants state are reflected in the world immediately and are rolled back when an
	 * abort is called. This means there is no use for onCommit, as the changes have already been reflected in the world.
	 */
	interface Reversion<T> extends Participant<T> {
		/**
		 * @param <T> a mutable type
		 */
		interface Mut<T> extends Reversion<T> {
			@Override
			T copy(T data);
		}

		@Override
		default void onCommit(T data) {}
	}

	/**
	 * a participant based on a persistent state, this means the container's state cannot be reverted once it is changed, so instead it stores a mutable
	 * version of it's state, and when the onCommit method is called, it reflects those changes in the world. This means there is no use for onAbort, as
	 * the
	 * mutable mirror of the participant is safely discarded.
	 */
	interface State<T> extends Participant<T> {
		/**
		 * @param <T> a mutable type
		 */
		interface Mut<T> extends State<T> {
			@Override
			T copy(T data);
		}

		@Override
		default void onAbort(T data) {}
	}


	/**
	 * take an amount of an object to the participant
	 *
	 * universal accepted types: {@link ItemStack} (amount must = 1), {@link Fluid}, {@link ItemConvertible}.
	 * however you can pass whatever you want, and implement whatever you want.
	 *
	 * @param transaction the current transaction
	 * @param type the type, may be ItemStack, Fluid, Item, etc.
	 * @param amount the amount of that type to take
	 * @return the amount actually taken
	 */
	long take(Transaction transaction, Object type, long amount);

	/**
	 * add an amount of an object to the participant
	 *
	 * default types: {@link ItemStack} (amount should be ignored in favor of amount), {@link Fluid}
	 *
	 * @param transaction the current transaction
	 * @param type the type, may be ItemStack, Fluid, Item, etc.
	 * @param amount the amount of that type to add
	 * @return the amount leftover
	 */
	long add(Transaction transaction, Object type, long amount);

	/**
	 * called when any level transaction is aborted
	 */
	void onAbort(T data);

	/**
	 * called when a top level transaction is committed.
	 *
	 * @param data the data for the participant
	 */
	void onCommit(T data);

	/**
	 * assumes data type is immutable, this is called when a child transaction's data is requested but it's only found on the parent, if you're reversion
	 * based and using a list you should just create a new empty list
	 *
	 * @see Mut
	 */
	default T copy(T data) {
		return data;
	}

	/**
	 * if the type is of Item, returns a new ItemStack of that type, if type is of ItemStack it clones it and sets the amount
	 */
	@Nullable
	static ItemStack of(Object type, long amount) {
		if (type instanceof ItemConvertible) {
			return new ItemStack(((ItemConvertible) type).asItem(), (int) Math.min(amount, Integer.MAX_VALUE));
		} else if (type instanceof ItemStack) {
			ItemStack clone = ((ItemStack) type).copy();
			clone.setCount((int) Math.min(amount, Integer.MAX_VALUE));
			return clone;
		}
		return null;
	}
}
