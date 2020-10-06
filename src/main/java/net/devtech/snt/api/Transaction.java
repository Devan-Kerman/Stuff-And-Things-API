package net.devtech.snt.api;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.devtech.snt.api.util.data.Mutable;
import net.devtech.snt.internal.ParticipantStrategy;

/**
 * <b>What</b>
 * Transactions are a cleaner way of 'simulating' the world, for dealing with the transfer of 'stuff'. When the api only provides this method for transfer, then
 * it's really useful and effective.
 *
 * ## Why
 * If you're making a cobble generator, and you don't want to have an internal buffer for the fluids, you want to check whether or not the containers to your
 * left and right contain lava and water, at the same time. For example of those two containers were connected somehow, eg. some player bound tank, then taking
 * lava out of one of them, and then the other may not be valid, one tank could drain from the other. With transactions this isn't a problem because you can tell ahead of time that
 * this wouldn't be valid.
 */
@SuppressWarnings ({
		"unchecked",
		"rawtypes"
})
public final class Transaction implements AutoCloseable {
	private static final ThreadLocal<Transaction> TRANSACTIONS = new ThreadLocal<>();
	private final Map<Participant, Object> state = new Object2ObjectOpenCustomHashMap<>(ParticipantStrategy.INSTANCE);
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
		return (T) this.state.put(participant, data);
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
				parent = participant.copyTransactionData(parent);
				// in theory if we want to conserve memory, we should only copy/set if the type is mutable
				// but I decided against it for the sake of simplicity.
				this.set(participant, parent);
			}
		}

		return val;
	}

	public <T, V extends Participant<T>> T getOrCompute(V participant, Function<V, T> val) {
		T get = this.get(participant);
		if (get == null) {
			return val.apply(participant);
		}
		return get;
	}

	public <T, V extends Participant<T>> T computeIfAbsent(V participant, Function<V, T> val) {
		T get = this.get(participant);
		if (get == null) {
			this.set(participant, get = val.apply(participant));
		}
		return get;
	}

	/**
	 * @return the new data for the participant in the transaction state
	 */
	public <T> T mutate(Participant<T> participant, UnaryOperator<T> data) {
		T newData = data.apply(this.get(participant));
		this.set(participant, newData);
		return newData;
	}

	public <T extends Mutable<T>> T mutateMutable(Participant<T> participant, Consumer<T> data) {
		T newData = this.get(participant);
		data.accept(newData);
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
