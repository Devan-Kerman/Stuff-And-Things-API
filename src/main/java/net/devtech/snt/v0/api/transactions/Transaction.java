package net.devtech.snt.v0.api.transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.devtech.snt.internal.event.Event;
import net.devtech.snt.v0.api.transactions.keys.Key;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class Transaction implements ServerTickEvents.EndTick, AutoCloseable {
	public static final Event<ServerTickEvents.EndTick> EVENT = new Event<>(ServerTickEvents.EndTick.class, a -> s -> {
		for (ServerTickEvents.EndTick tick : a) {
			tick.onEndTick(s);
		}
	});
	private static final ThreadLocal<Transaction> CURRENT = new ThreadLocal<>();

	static {
		ServerTickEvents.END_SERVER_TICK.register(m -> EVENT.invoker().onEndTick(m));
	}

	private final List<Key<?>> list;
	private final String name;
	@Nullable private Map<Key<?>, Object> simpleData;
	private Transaction parent;
	/**
	 * false if this is a parent transaction that has an uncommited child
	 */
	private boolean hasChild, invalidated;

	private Transaction(String name, int expectedParticipants) {
		this.list = new ArrayList<>(expectedParticipants);
		this.name = name;
	}

	private Transaction(String name) {
		this.list = new ArrayList<>();
		this.name = name;
	}

	public static Transaction create(int expectedParticipants) {
		return validate(new Transaction("unknown", expectedParticipants));
	}

	private static Transaction validate(Transaction instance) {
		EVENT.register(instance);
		Transaction parent = instance.parent = CURRENT.get();
		if (parent != null) {
			if (parent.hasChild) {
				throw new IllegalStateException("Parallel Transactions are not supported!");
			}
			parent.hasChild = true;
		}


		CURRENT.set(instance);
		return instance;
	}

	public static Transaction create() {
		return validate(new Transaction("unknown"));
	}

	public static Transaction create(String name, int expectedParticipants) {
		return validate(new Transaction(name, expectedParticipants));
	}

	public static Transaction create(String name) {
		return validate(new Transaction(name));
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

	public interface QuantityFunction<A, B> {
		B apply(A type, int amount);
	}

	public interface KeyQuantityFunction<A, B extends Key<C>, C> {
		C apply(B key, A type, int amount);
	}

	public <T, K, J extends Key<T>> boolean putIfAbsent(J key, K object, BiFunction<J, K, T> function) {
		if(this.get(key) == null) {
			this.set(key, function.apply(key, object));
			return true;
		}
		return false;
	}

	public <T, K, J extends Key<T>> boolean putIfAbsent(J key, K type, int amount, KeyQuantityFunction<K, J, T> function) {
		if(this.get(key) == null) {
			this.set(key, function.apply(key, type, amount));
			return true;
		}
		return false;
	}

	public <T, K> boolean putIfAbsent(Key<T> key, K type, int amount, QuantityFunction<K, T> function) {
		if(this.get(key) == null) {
			this.set(key, function.apply(type, amount));
			return true;
		}
		return false;
	}

	public <T, K, J extends Key<T>> boolean putIfAbsentLevel(J key, K object, BiFunction<J, K, T> function) {
		if(key.get(this) == null) {
			this.set(key, function.apply(key, object));
			return true;
		}
		return false;
	}

	public <T, K, J extends Key<T>> boolean putIfAbsentLevel(J key, K type, int amount, KeyQuantityFunction<K, J, T> function) {
		if(key.get(this) == null) {
			this.set(key, function.apply(key, type, amount));
			return true;
		}
		return false;
	}

	public <T, K> boolean putIfAbsentLevel(Key<T> key, K type, int amount, QuantityFunction<K, T> function) {
		if(key.get(this) == null) {
			this.set(key, function.apply(type, amount));
			return true;
		}
		return false;
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

	private Map<Key<?>, Object> getSimple() {
		Map<Key<?>, Object> simple = this.simpleData;
		if (simple == null) {
			simple = this.simpleData = new HashMap<>();
		}
		return simple;
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

	public void commit() {
		this.validate();
		for (Key<?> key : this.list) {
			key.commit(this);
		}
		this.invalidate();
	}

	private void validate() {
		if (this.invalidated) {
			throw new IllegalStateException("Cannot exit invalidated transaction! name: " + this.name);
		}
	}

	private void invalidate() {
		this.invalidated = true;
		Transaction parent = this.parent;
		if (parent != null) {
			parent.hasChild = false;
		}

		EVENT.deregister(this);
		CURRENT.set(parent);
	}

	public void abort() {
		this.validate();
		for (Key<?> key : this.list) {
			key.abort(this);
		}
		this.invalidate();
	}

	@Override
	public void onEndTick(MinecraftServer world) {
		if (!this.invalidated) {
			this.invalidate();
			throw new IllegalStateException("Transaction '" + this.name + "' not exited!");
		}
	}

	@Override
	public void close() {
		if(!this.invalidated) {
			this.commit();
		}
	}
}
