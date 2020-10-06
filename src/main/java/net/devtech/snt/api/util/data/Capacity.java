package net.devtech.snt.api.util.data;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * an amount of a thing compared to the maximum amount of stuff a participant can hold,
 * this is implemented on ItemStack
 */
public interface Capacity<T> {
	/**
	 * @return the amount of that thing it can store
	 */
	int getCapacity();

	/**
	 * @return the amount of stuff
	 */
	int getAmount();

	/**
	 * @return the stuff in it
	 */
	T getInstance();

	static Capacity<ItemStack> getItemStack(@Nullable Inventory inventory, ItemStack stack) {
		// if the inventory can accommodate the stack fully, then there's no need for an extra object
		if(inventory == null || inventory.getMaxCountPerStack() >= stack.getMaxCount()) {
			return (Capacity<ItemStack>) (Object) stack;
		}
		return new Capacity.Mut<>(ItemStack::copy, stack, stack.getCount(), inventory.getMaxCountPerStack());
	}

	static Capacity<Fluid> getFluid(Fluid fluid, int drops, int max) {
		return new Capacity.Impl<>(fluid, drops, max);
	}

	static <T> Impl<T> get(T instance, int amount, int capacity) {
		return new Impl<>(instance, amount, capacity);
	}

	static <T> Mut<T> getMut(UnaryOperator<T> copier, T instance, int amount, int capacity) {
		return new Mut<>(copier, instance, amount, capacity);
	}

	class Impl<T> implements Capacity<T> {
		private final T instance;
		private final int amount, capacity;

		Impl(T instance, int amount, int capacity) {
			this.instance = instance;
			this.amount = amount;
			this.capacity = capacity;
		}

		@Override
		public int getCapacity() {
			return this.capacity;
		}

		@Override
		public int getAmount() {
			return this.amount;
		}

		@Override
		public T getInstance() {
			return this.instance;
		}
	}

	/**
	 * if the 'thing' is not actually mutable then you can use this to prevent people from meddling with your data
	 * @param <T>
	 */
	class Mut<T> extends Impl<T> {
		private final UnaryOperator<T> copier;

		public Mut(UnaryOperator<T> copier, T instance, int amount, int capacity) {
			super(instance, amount, capacity);
			this.copier = copier;
		}

		@Override
		public T getInstance() {
			return this.copier.apply(super.getInstance());
		}
	}
}