package net.devtech.snt.internal.inventory;

import net.devtech.snt.api.util.data.Mutable;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class SidedInventoryBackedState extends InventoryBackedState {
	private final int[] slots;
	private final Direction face;

	public SidedInventoryBackedState(SidedInventoryBackedState state) {
		super(state);
		this.slots = state.slots;
		this.face = state.face;
	}

	public SidedInventoryBackedState(SidedInventory inventory, Direction face) {
		super(inventory);
		this.slots = inventory.getAvailableSlots(face);
		this.face = face;
	}

	@Override
	public void copyTo(Inventory target) {
		for (int i = 0; i < this.size(); i++) {
			ItemStack stack = this.getStack(i);
			if (stack.isEmpty()) {
				target.setStack(this.slots[i], ItemStack.EMPTY);
			} else {
				target.setStack(this.slots[i], stack);
			}
		}
	}

	@Override
	public int size() {
		return this.slots.length;
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.inventory.getStack(this.slots[slot]);
	}

	@Override
	protected boolean addToNewSlot(ItemStack stack, int slot) {
		return ((SidedInventory) this.inventory).canInsert(slot, stack, this.face) && super.addToNewSlot(stack, slot);
	}

	@Override
	protected boolean addToExistingSlot(ItemStack stack, int slot) {
		return ((SidedInventory) this.inventory).canInsert(slot, stack, this.face) && super.addToExistingSlot(stack, slot);
	}

	@Override
	public InventoryBackedState copy() {
		return new SidedInventoryBackedState(this);
	}
}