package net.devtech.snt.internal.inventory;

import java.util.Iterator;

import net.devtech.snt.api.util.data.Capacity;

import net.minecraft.inventory.Inventory;

public class InventoryCapacityIterator implements Iterator<Capacity<?>> {
	private final Inventory inventory;
	private int i = 0;

	public InventoryCapacityIterator(Inventory inventory) {this.inventory = inventory;}

	@Override
	public boolean hasNext() {
		return this.i < this.inventory.size();
	}

	@Override
	public Capacity<?> next() {
		return Capacity.getItemStack(this.inventory, this.inventory.getStack(this.i++));
	}
}
