package net.devtech.fixedfluids.api;


import net.devtech.fixedfluids.api.util.Transaction;

/**
 * a participant in a transaction
 *
 * @param <T> the data type (immutable)
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
		@Override
		default void onAbort(T data) {}
	}

	/**
	 * @deprecated only use for single transactions
	 */
	@Deprecated
	default long interact(Object type, long amount, boolean simulate) {
		Transaction transaction = new Transaction();
		long val = this.interact(transaction, type, amount);
		if (simulate) {
			transaction.abort();
		} else {
			transaction.commit();
		}
		return val;
	}

	/**
	 * take or add an amount of an object to the participant
	 *
	 * @param transaction the current transaction
	 * @param type the type, may be ItemStack, Fluid, Item, etc.
	 * @param amount the amount of that type to take or add (negative for take, positive for add)
	 * @return if amount is positive, return amount of type leftover. else, amount of type actually taken
	 */
	long interact(Transaction transaction, Object type, long amount);

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
	 * assumes data type is immutable
	 *
	 * @see Mut
	 */
	default T copy(T data) {
		return data;
	}
}
