package net.devtech.snt.v0.api.transactions.keys;

import net.devtech.snt.v0.api.transactions.Transaction;
import org.jetbrains.annotations.Nullable;

public interface Key<T> {
	/**
	 * A simple key is one you implement on your participant, this avoids the creation of a persistent object and simplifies code,
	 * at the cost of slower access times for data, and maintaining a hashmap inside each transaction object
	 * @see FastKey
	 */
	interface Simple<T> extends Key<T> {
		@Override
		default void store(Transaction transaction, T value) {
			transaction.storeSimple(this, value);
		}

		@Override
		default T get(Transaction transaction) {
			return transaction.getSimple(this);
		}

		@Override
		default void commit(Transaction transaction) {
			Transaction parent = transaction.getParent();
			if(parent == null) {
				// commit parent level transaction
				this.commit(transaction.removeSimple(this));
			} else {
				// copy to parent
				parent.set(this, this.get(transaction));
			}
		}

		/**
		 * called when a top level transaction is aborted
		 * @param val your data
		 */
		void commit(T val);

		/**
		 * called when any level transaction is aborted
		 */
		void abort(T val);
	}

	/**
	 * set the value for the transaction
	 *
	 * <b>DO NOT CALL THIS, ONLY IMPLEMENT</b>
	 */
	void store(Transaction transaction, T value);

	/**
	 * get the value for the transaction, or null if there was no data for it
	 *
	 * <b>DO NOT CALL THIS, ONLY IMPLEMENT</b>
	 */
	@Nullable
	T get(Transaction transaction);

	void commit(Transaction transaction);

	void abort(Transaction transaction);
}
