package net.devtech.snt.api.util.data;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * A 'slot' in a container
 */
public interface TypeSlot<T> {
	/**
	 * @return the amount of that thing it can store
	 */
	int getCapacity();

	/**
	 * @return the amount of stuff
	 */
	int getAmount();

	/**
	 * @return the increment in which this slot stores fluid
	 * @see CauldronBlock (1/3)
	 */
	int getQuanta();

	T getInstance();

	static TypeSlot<ItemStack> getItemStack(@Nullable Inventory inventory, ItemStack stack) {
		// if the inventory can accommodate the stack fully, then there's no need for an extra object
		if (inventory == null || inventory.getMaxCountPerStack() >= stack.getMaxCount()) {
			return (TypeSlot<ItemStack>) (Object) stack;
		}
		return new TypeSlot.Mut<>(ItemStack::copy, stack, stack.getCount(), inventory.getMaxCountPerStack(), 1);
	}

	static TypeSlot<Fluid> getFluid(Fluid fluid, int drops, int capacity, int quanta) {
		return new TypeSlot.Impl<>(fluid, drops, capacity, quanta);
	}

	static <T> Impl<T> get(T instance, int amount, int capacity, int quanta) {
		return new Impl<>(instance, amount, capacity, quanta);
	}

	static <T> Mut<T> getMut(UnaryOperator<T> copier, T instance, int amount, int capacity, int quanta) {
		return new Mut<>(copier, instance, amount, capacity, quanta);
	}

	static TypeSlot<Fluid> getFluid(Fluid fluid, int drops, int capacity) {
		return getFluid(fluid, drops, capacity, 1);
	}

	static <T> Impl<T> get(T instance, int amount, int capacity) {
		return get(instance, amount, capacity, 1);
	}

	static <T> Mut<T> getMut(UnaryOperator<T> copier, T instance, int amount, int capacity) {
		return getMut(copier, instance, amount, capacity, 1);
	}

	static TypeSlot<Fluid> getFluid(Fluid fluid, int drops) {
		return getFluid(fluid, drops, Integer.MAX_VALUE, 1);
	}

	static <T> Impl<T> get(T instance, int amount) {
		return get(instance, amount, Integer.MAX_VALUE, 1);
	}

	static <T> Mut<T> getMut(UnaryOperator<T> copier, T instance, int amount) {
		return getMut(copier, instance, amount, Integer.MAX_VALUE, 1);
	}


	class Impl<T> implements TypeSlot<T> {
		private final T instance;
		private final int amount, capacity, quanta;

		Impl(T instance, int amount, int capacity, int quanta) {
			this.instance = instance;
			this.amount = amount;
			this.capacity = capacity;
			this.quanta = quanta;
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
		public int getQuanta() {
			return quanta;
		}

		@Override
		public T getInstance() {
			return this.instance;
		}
	}

	/**
	 * if the 'thing' is not actually mutable then you can use this to prevent people from meddling with your data
	 *
	 * @param <T>
	 */
	class Mut<T> extends Impl<T> {
		private final UnaryOperator<T> copier;

		public Mut(UnaryOperator<T> copier, T instance, int amount, int capacity, int quanta) {
			super(instance, amount, capacity, quanta);
			this.copier = copier;
		}

		@Override
		public T getInstance() {
			return this.copier.apply(super.getInstance());
		}
	}
}