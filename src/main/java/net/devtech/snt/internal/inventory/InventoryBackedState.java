package net.devtech.snt.internal.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.snt.api.util.data.Mutable;
import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.util.InventoryUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/**
 * this is a mutable data type! the data type of an inventory
 */
public class InventoryBackedState implements Inventory, Mutable<InventoryBackedState> {
	protected final Inventory inventory;
	private final Int2ObjectMap<ItemStack> modified = new Int2ObjectOpenHashMap<>(0);

	public InventoryBackedState(Inventory inventory) {
		this.inventory = inventory;
	}

	public static InventoryBackedState of(SidedInventory inventory, Direction direction) {
		return new SidedInventoryBackedState(inventory, direction);
	}

	public static InventoryBackedState of(Inventory inventory) {
		return new InventoryBackedState(inventory);
	}

	@Deprecated
	public static InventoryBackedState of(Object object) {
		return of((Inventory)object);
	}

	public void copyTo(Inventory target) {
		for (int i = 0; i < this.size(); i++) {
			ItemStack stack = this.getStack(i);
			if (stack.isEmpty()) {
				target.setStack(i, ItemStack.EMPTY);
			} else {
				target.setStack(i, stack);
			}
		}
	}

	public ItemStack addStack(ItemStack stack) {
		ItemStack itemStack = stack.copy();
		for (int i = 0; i < this.size(); ++i) {
			if (this.addToExistingSlot(itemStack, i)) {
				break;
			}
		}
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			for (int i = 0; i < this.size(); ++i) {
				if (this.addToNewSlot(itemStack, i)) {
					break;
				}
			}
			return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
		}
	}

	public int take(Item item, int count) {
		int sum = 0;
		for (int i = 0; i < this.size(); i++) {
			ItemStack stack = this.getStack(i);
			if (stack.getItem() == item) {
				int toTake = Math.min(stack.getCount(), count);
				stack.decrement(toTake);
				sum += toTake;
			}
		}
		return sum;
	}

	/**
	 * Searches this inventory for the specified item and removes the given amount from this inventory.
	 */
	public void transferTo(Transaction transaction, Participant<?> dest, int count) {
		for (int i = 0; i < this.size(); ++i) {
			ItemStack stack = this.getStack(i);
			int ret = dest.push(transaction, stack, Math.min(count, stack.getCount()));
			stack.setCount(ret);
			count -= ret;
		}
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.modified.computeIfAbsent(slot, i -> this.inventory.getStack(slot).copy());
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack removeStack(int slot) {
		throw new UnsupportedOperationException();
	}

	protected boolean addToNewSlot(ItemStack stack, int slot) {
		ItemStack itemStack = this.getStack(slot);
		if (itemStack.isEmpty() && this.inventory.isValid(slot, stack)) {
			this.setStack(slot, stack.copy());
			stack.setCount(0);
			return true;
		}
		return false;
	}

	protected boolean addToExistingSlot(ItemStack stack, int slot) {
		ItemStack itemStack = this.getStack(slot);
		if (InventoryUtil.canStackIgnoreStackSize(itemStack, stack) && this.inventory.isValid(slot, stack)) {
			int i = Math.min(this.inventory.getMaxCountPerStack(), itemStack.getMaxCount());
			int j = Math.min(stack.getCount(), i - itemStack.getCount());
			if (j > 0) {
				itemStack.increment(j);
				stack.decrement(j);
			}
			return stack.isEmpty();
		}
		return false;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.modified.put(slot, stack);
	}

	@Override
	public void markDirty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InventoryBackedState copy() {
		return new InventoryBackedState(this);
	}
}
