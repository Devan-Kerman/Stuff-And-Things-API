package net.devtech.snt.api.transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.snt.api.transactions.keys.Key;
import org.jetbrains.annotations.Nullable;

public class Transaction {
	// todo should we keep this, may lead to exploits?
	// todo add post-commit for blockstate containers
	public static final Transaction AUTO_COMMIT = new Transaction() {
		@Override
		public <T> void set(Key<T> key, T data) {
			key.store(this, data);
			key.commit(this);
		}
	};

	private static final ThreadLocal<Transaction> CURRENT = new ThreadLocal<>();
	private final List<Key<?>> list;
	@Nullable
	private Map<Key<?>, Object> simpleData;
	private Transaction parent;
	/**
	 * false if this is a parent transaction that has an uncommited child
	 */
	private boolean hasChild, invalidated;

	public static Transaction create(int expectedParticipants) {
		return validate(new Transaction(expectedParticipants));
	}

	public static Transaction create() {
		return validate(new Transaction());
	}

	private static Transaction validate(Transaction instance) {
		Transaction parent = instance.parent = CURRENT.get();
		if (parent != null) {
			if(parent.hasChild) {
				throw new IllegalStateException("Parallel Transactions are not supported!");
			}
			parent.hasChild = true;
		}


		CURRENT.set(instance);
		return instance;
	}

	private Transaction(int expectedParticipants) {
		this.list = new ArrayList<>(expectedParticipants);
	}

	private Transaction() {
		this.list = new ArrayList<>();
	}

	/**
	 * get the data for this transaction for the given key, your participant can even implement the key interface
	 * directly if you don't want to keep an extra object laying around.
	 *
	 * @param key the data key
	 * @return the data or null
	 */
	@Nullable
	public <T> T get(Key<T> key) {
		T val = null;
		Transaction current = this;
		while (current != null && (val = key.get(current)) == null) {
			current = current.parent;
		}
		return val;
	}

	/**
	 * call this function to set the data for this transaction
	 */
	public <T> void set(Key<T> key, T data) {
		this.list.add(key);
		key.store(this, data);
	}

	@Nullable
	public Transaction getParent() {
		return this.parent;
	}

	/**
	 * @deprecated internal
	 */
	@Deprecated
	public void storeSimple(Key.Simple<?> key, Object object) {
		this.list.add(key);
		this.getSimple().put(key, object);
	}

	/**
	 * @deprecated internal
	 */
	@Deprecated
	public <T> T getSimple(Key.Simple<T> key) {
		return (T) this.getSimple().get(key);
	}

	/**
	 * @deprecated internal
	 */
	@Deprecated
	public <T> T removeSimple(Key.Simple<T> key) {
		return (T) this.getSimple().remove(key);
	}

	private Map<Key<?>, Object> getSimple() {
		Map<Key<?>, Object> simple = this.simpleData;
		if(simple == null) {
			simple = this.simpleData = new HashMap<>();
		}
		return simple;
	}

	public void commit() {
		this.validate();
		for (Key<?> key : this.list) {
			key.commit(this);
		}
		this.invalidate();
	}

	public void abort() {
		this.validate();
		for (Key<?> key : this.list) {
			key.abort(this);
		}
		this.invalidate();
	}

	private void validate() {
		if(this.invalidated) {
			throw new IllegalStateException("Cannot exit invalidated transaction! (you can't commit and abort the same transaction)");
		}
	}

	private void invalidate() {
		this.invalidated = true;
		Transaction parent = this.parent;
		if(parent != null) {
			parent.hasChild = false;
		}
	}
}
