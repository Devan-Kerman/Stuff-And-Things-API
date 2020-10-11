package net.devtech.snt.internal.inventory;

import java.util.Iterator;
import java.util.function.Function;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.concrete.RigidContainer;
import net.devtech.snt.api.concrete.Supported;
import net.devtech.snt.api.concrete.WildParticipant;
import net.devtech.snt.api.util.data.TypeSlot;
import org.jetbrains.annotations.NotNull;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class SidedInventoryParticipant implements WildParticipant<SidedInventoryBackedState>, Supported, RigidContainer {
	protected final Direction direction;
	protected final SidedInventory inventory;
	public SidedInventoryParticipant(Direction direction, SidedInventory inventory) {
		this.direction = direction;
		this.inventory = inventory;
	}

	@Override
	public void transfer(Transaction transaction, Participant<?> destination, int amount) {
		if(!(destination instanceof Supported) || ((Supported) destination).isPushSupported(ItemStack.class)) {
			transaction.getOrCompute(this, get(this.direction)).transferTo(transaction, destination, amount);
		}
	}

	@Override
	public int take(Transaction transaction, Object type, int amount) {
		if (type instanceof Item) {
			return transaction.getOrCompute(this, get(this.direction)).take((Item) type, amount);
		}
		return 0;
	}

	@Override
	public int push(Transaction transaction, Object type, int amount) {
		if (type instanceof ItemStack) {
			ItemStack cast = ((ItemStack) type).copy();
			cast.setCount(amount);
			return transaction.getOrCompute(this, get(this.direction)).addStack(cast).getCount();
		}
		return amount;
	}

	@Override
	public void onCommit(SidedInventoryBackedState obj) {
		obj.copyTo((Inventory) this);
	}

	@Override
	public SidedInventoryBackedState copyTransactionData(SidedInventoryBackedState data) {
		return new SidedInventoryBackedState(data);
	}

	@Override
	public @NotNull Iterator<TypeSlot<?>> iterator() {
		return new InventoryCapacityIterator((Inventory) this);
	}

	@Override
	public boolean isPullSupported(Class<?> type) {
		return type == ItemStack.class;
	}

	@Override
	public boolean isPushSupported(Class<?> type) {
		return type == ItemStack.class;
	}

	private static final Direction[] VALUES = Direction.values();
	private static final Function<SidedInventoryParticipant, SidedInventoryBackedState>[] FUNCTIONS = new Function[VALUES.length];
	static {
		for (int i = 0; i < FUNCTIONS.length; i++) {
			Direction dir = VALUES[i];
			FUNCTIONS[i] = s -> new SidedInventoryBackedState(s.inventory, dir);
		}
	}

	public static Function<SidedInventoryParticipant, SidedInventoryBackedState> get(Direction direction) {
		return FUNCTIONS[direction.ordinal()];
	}

	public static class InventoryCapacityIterator implements Iterator<TypeSlot<?>> {
		private final Inventory inventory;
		private int i = 0;

		public InventoryCapacityIterator(Inventory inventory) {this.inventory = inventory;}

		@Override
		public boolean hasNext() {
			return this.i < this.inventory.size();
		}

		@Override
		public TypeSlot<?> next() {
			return TypeSlot.getItemStack(this.inventory, this.inventory.getStack(this.i++));
		}
	}
}
