package net.devtech.snt.api.util.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * an array of objects' data type
 * @param <T> an immutable type, or a class that implements {@link Mutable}
 * @see Mutable
 */
@SuppressWarnings ({
		"rawtypes",
		"unchecked",
		"SuspiciousSystemArraycopy"
})
public class ArrayData<T> {
	private final Object data;
	private final Int2ObjectMap<T> modification = new Int2ObjectOpenHashMap<>(0);

	public ArrayData(Object[] data) {this.data = data;}

	public ArrayData(ArrayData<T> data) {this.data = data;}

	public void copyTo(Object[] data) {
		if(this.modification.isEmpty()) {
			if(this.data instanceof ArrayData) {
				((ArrayData) this.data).copyTo(data);
			} else if(data != this.data) {
				System.arraycopy(this.data, 0, data, 0, data.length);
			}
		}

		for (int i = 0; i < this.size(); i++) {
			data[i] = this.get(i);
		}
	}

	public int size() {
		if(this.data instanceof Object[]) {
			return ((Object[]) this.data).length;
		} else {
			return ((ArrayData) this.data).size();
		}
	}

	public void set(int index, T object) {
		if(index >= this.size()) throw new ArrayIndexOutOfBoundsException(index);
		this.modification.put(index, object);
	}

	public T get(int index) {
		if(index >= this.size()) throw new ArrayIndexOutOfBoundsException(index);

		Object val = this.modification.get(index);
		if (val == null) {
			if (this.data instanceof ArrayData) {
				val = ((ArrayData) this.data).get(index);
			} else {
				val = ((Object[]) this.data)[index];
			}

			if (val instanceof Mutable) {
				val = ((Mutable) val).copy();
			}
			this.modification.put(index, (T) val);
		}

		return (T) val;
	}
}
