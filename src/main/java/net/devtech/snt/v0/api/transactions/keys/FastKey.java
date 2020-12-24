package net.devtech.snt.v0.api.transactions.keys;

import java.util.HashMap;
import java.util.Map;

import net.devtech.snt.v0.api.transactions.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * This Key implementation for significantly faster access times. The key acts as a delegate, simplifying code while
 * optimizing common cases. The only disadvantage is you need to store this object in a field, which is easy enough.
 */
public class FastKey<T> implements Key<T> {
	public interface Participant<T> extends net.devtech.snt.v0.api.participants.Participant {
		/**
		 * called when a top level transaction is aborted
		 * @param val your data
		 */
		default void commit(T val) {}

		/**
		 * called when any level transaction is aborted
		 */
		void abort(T val);
	}

	@Nullable
	private Transaction a, b, c;
	@Nullable
	private T aVal, bVal, cVal;

	@Nullable
	private Map<Transaction, T> map;

	private final Participant<T> participant;
	public FastKey(Participant<T> participant) {this.participant = participant;}

	@Override
	public void store(Transaction transaction, T value) {
		if(this.a == null) {
			this.a = transaction;
			this.aVal = value;
		} else if(this.b == null) {
			this.b = transaction;
			this.bVal = value;
		} else if(this.c == null) {
			this.c = transaction;
			this.cVal = value;
		} else {
			Map<Transaction, T> map = this.map;
			if(map == null) {
				this.map = map = new HashMap<>();
			}
			map.put(transaction, value);
		}
	}

	@Override
	public T get(Transaction transaction) {
		if(this.a == transaction) {
			return this.aVal;
		} else if(this.b == transaction) {
			return this.bVal;
		} else if(this.c == transaction) {
			return this.cVal;
		} else if(this.map != null) {
			return this.map.get(transaction);
		}
		return null;
	}

	public T getAndRemove(Transaction transaction) {
		T temp = null;
		if(this.a == transaction) {
			temp = this.aVal;
			this.aVal = null;
		} else if(this.b == transaction) {
			temp = this.bVal;
			this.bVal = null;
		} else if(this.c == transaction) {
			temp = this.cVal;
			this.cVal = null;
		} else if(this.map != null) {
			temp = this.map.remove(transaction);
			if(this.map.isEmpty()) {
				this.map = null;
			}
		}
		return temp;
	}

	@Override
	public void commit(Transaction transaction) {
		Transaction parent = transaction.getParent();
		if(parent == null) {
			this.participant.commit(this.getAndRemove(transaction));
		} else {
			// copy to parent
			parent.set(this, this.get(transaction));
		}
	}

	@Override
	public void abort(Transaction transaction) {
		this.participant.abort(this.getAndRemove(transaction));
	}
}
