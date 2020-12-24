package net.devtech.snt.internal.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * an event listener thing that goes fast
 * this exists for de-registration
 * @param <T>
 */
public final class Event<T> {
	private final List<T> listeners = new ArrayList<>();
	private T compiled;
	private final Class<T> cls;
	private final Function<T[], T> func;
	public Event(Class<T> cls, Function<T[], T> combiner) {
		this.cls = cls;
		this.func = combiner;
		this.recompile();
	}

	public void register(T listener) {
		this.listeners.add(listener);
		this.recompile();
	}

	public void deregister(T listener) {
		this.listeners.remove(listener);
		this.recompile();
	}

	private void recompile() {
		T[] arr = (T[]) Array.newInstance(this.cls, this.listeners.size());
		arr = this.listeners.toArray(arr);
		this.compiled = this.func.apply(arr);
	}

	public T invoker() {
		return this.compiled;
	}
}
