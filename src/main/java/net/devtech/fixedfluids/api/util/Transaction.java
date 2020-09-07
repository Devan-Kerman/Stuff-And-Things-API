package net.devtech.fixedfluids.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import alexiil.mc.lib.attributes.Simulation;
import net.devtech.fixedfluids.api.Participant;

public class Transaction implements AutoCloseable {
	private static final ThreadLocal<Transaction> TRANSACTIONS = new ThreadLocal<>();
	private final Map<Participant<Object>, Object> state = new HashMap<>();
	private final Transaction parent;
	private final Thread thread;
	private Transaction child;
	private boolean invalidated;

	public Transaction() {
		Transaction parent = TRANSACTIONS.get();
		if (parent != null) {
			parent.validateThread();
			if (parent.child != null) {
				throw new UnsupportedOperationException("There cannot be multiple unfinished transactions on the same level!");
			}
			parent.child = this;
			this.thread = parent.thread;
		} else {
			this.thread = Thread.currentThread();
		}

		this.parent = parent;
	}

	public <T> T getOrDefault(Participant<T> participant, T data) {
		T get = this.get(participant);
		if (get == null) {
			return data;
		}
		return get;
	}

	/**
	 * set the data in the transaction state for the participant
	 */
	@SuppressWarnings ("unchecked")
	public <T> T set(Participant<T> participant, T data) {
		this.validateThread();
		return (T) this.state.put((Participant<Object>) participant, data);
	}

	/**
	 * get the data from the transaction state for the participant
	 *
	 * @return may be null
	 */
	@SuppressWarnings ("unchecked")
	public <T> T get(Participant<T> participant) {
		this.validateThread();
		T val = (T) this.state.get(participant);
		if (val == null && this.parent != null) {
			T parent = this.parent.get(participant);
			if (parent != null) {
				parent = participant.copy(parent);
				// in theory if we want to conserve memory, we should only copy/set if the type is mutable
				// but I decided against it for the sake of simplicity.
				this.set(participant, parent);
			}
		}

		return val;
	}

	public <T> T getOrCompute(Participant<T> participant, Supplier<T> val) {
		T get = this.get(participant);
		if (get == null) {
			return val.get();
		}
		return get;
	}

	/**
	 * @return the new data for the participant in the transaction state
	 */
	public <T> T mutate(Participant<T> participant, UnaryOperator</*nullable*/ T> data) {
		T newData = data.apply(this.get(participant));
		this.set(participant, newData);
		return newData;
	}

	public Transaction abort() {
		this.validateThread();
		this.invalidated = true;
		TRANSACTIONS.set(this.parent);
		if (this.child != null) {
			throw new UnsupportedOperationException("You must finalize all child transactions before finalizing parents!");
		}

		this.state.forEach(Participant::onAbort);

		if (this.parent != null) {
			this.parent.child = null;
		}
		return this.parent;
	}

	public Transaction commit() {
		this.validateThread();
		this.invalidated = true;
		TRANSACTIONS.set(this.parent);
		if (this.child != null) {
			throw new UnsupportedOperationException("You must finalize all child transactions before finalizing parents!");
		}

		// only when the parent commits, confirming and finalizing the transaction to we notify our listeners
		if (this.parent == null) {
			this.state.forEach(Participant::onCommit);
		} else { // otherwise we merge our state with the parent
			this.parent.state.putAll(this.state);
		}

		if (this.parent != null) {
			this.parent.child = null;
		}

		return this.parent;
	}

	/**
	 * ensures the transaction is occurring on the same thread it was created in.
	 */
	public void validateThread() {
		if (Thread.currentThread() != this.thread || this.invalidated) {
			throw new UnsupportedOperationException("Transactions must begin, apply and end on the same thread!");
		}
	}

	public boolean isTopLevel() {
		return this.parent == null;
	}

	@Override
	public void close() {
		// if not aborted, commit
		if(!this.invalidated) {
			this.commit();
		}
	}
}
