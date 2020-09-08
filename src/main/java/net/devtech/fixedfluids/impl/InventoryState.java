package net.devtech.fixedfluids.impl;

import java.util.Set;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryState implements Inventory {
	private final Inventory inventory;
	private final Int2ObjectMap<ItemStack> modified = new Int2ObjectOpenHashMap<>();

	public InventoryState(Inventory inventory) {
		this.inventory = inventory;
	}

	public ItemStack addStack(ItemStack stack) {
		ItemStack itemStack = stack.copy();
		this.addToExistingSlot(itemStack);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			this.addToNewSlot(itemStack);
			return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
		}
	}

	private void addToNewSlot(ItemStack stack) {
		for (int i = 0; i < this.size(); ++i) {
			ItemStack itemStack = this.getStack(i);
			if (itemStack.isEmpty()) {
				this.setStack(i, stack.copy());
				stack.setCount(0);
				return;
			}
		}

	}

	private void addToExistingSlot(ItemStack stack) {
		for (int i = 0; i < this.size(); ++i) {
			ItemStack itemStack = this.getStack(i);
			if (canCombine(itemStack, stack)) {
				this.transfer(stack, itemStack);
				if (stack.isEmpty()) {
					return;
				}
			}
		}
	}

	public static boolean canCombine(ItemStack one, ItemStack two) {
		return one.getItem() == two.getItem() && ItemStack.areTagsEqual(one, two);
	}

	private void transfer(ItemStack source, ItemStack target) {
		int i = Math.min(this.getMaxCountPerStack(), target.getMaxCount());
		int j = Math.min(source.getCount(), i - target.getCount());
		if (j > 0) {
			target.increment(j);
			source.decrement(j);
			this.markDirty();
		}

	}

	/**
	 * Searches this inventory for the specified item and removes the given amount from this inventory.
	 *
	 * @return the stack of removed items
	 */
	public ItemStack take(Predicate<ItemStack> item, int count) {
		ItemStack stack = null;
		for (int i = this.inventory.size() - 1; i >= 0; --i) {
			ItemStack itemStack2 = this.getStack(i);
			int current = 0;
			if (stack == null) {
				if(item.test(itemStack2)) {
					current = itemStack2.getCount();
					stack = itemStack2.copy();
					stack.setCount(0);
				} else {
					continue;
				}
			}

			int j = count - current;
			ItemStack itemStack3 = itemStack2.split(j);
			stack.increment(itemStack3.getCount());
			if (current == count) {
				break;
			}
		}

		return stack;
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.modified.getOrDefault(slot, this.inventory.getStack(slot));
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack stack = this.modified.get(slot);
		if (stack != null) {
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}

			int take = Math.min(stack.getCount(), amount);
			ItemStack copy = stack.copy();
			copy.setCount(take);
			stack.setCount(stack.getCount() - take);
			return copy;
		} else {
			this.modified.put(slot,
			                  this.inventory.getStack(slot)
			                                .copy());
			return this.removeStack(slot, amount);
		}
	}

	@Override
	public ItemStack removeStack(int slot) {
		return this.removeStack(slot, 1073741823);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.modified.put(slot, stack);
	}

	@Override
	public int getMaxCountPerStack() {
		return this.inventory.getMaxCountPerStack();
	}

	@Override
	public void markDirty() {
		this.inventory.markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public void onOpen(PlayerEntity player) {
		this.inventory.onOpen(player);
	}

	@Override
	public void onClose(PlayerEntity player) {
		this.inventory.onClose(player);
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return this.inventory.isValid(slot, stack);
	}

	@Override
	public int count(Item item) {
		return this.inventory.count(item);
	}

	@Override
	public boolean containsAny(Set<Item> items) {
		return this.inventory.containsAny(items);
	}

	@Override
	public void clear() {
		this.inventory.clear();
	}
}
