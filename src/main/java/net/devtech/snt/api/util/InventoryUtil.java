package net.devtech.snt.api.util;

import net.devtech.snt.api.Participant;
import net.devtech.snt.internal.inventory.SidedInventoryParticipant;
import org.jetbrains.annotations.Nullable;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class InventoryUtil {
	/**
	 * can a stack with b, is compatible with mods that hook into ItemStack#getMaxCount
	 */
	public static boolean canStack(ItemStack a, ItemStack b) {
		return canStackIgnoreStackSize(a, b) && Math.min(a.getMaxCount(), b.getMaxCount()) >= a.getCount() + b.getCount();
	}

	public static boolean canStackIgnoreStackSize(ItemStack a, ItemStack b) {
		return a.getItem() == b.getItem() && ItemStack.areTagsEqual(a, b);
	}

	/**
	 * @return get the participant for a specific sided inventory
	 */
	public static Participant<?> getParticipant(Inventory inventory, @Nullable Direction direction) {
		if (inventory instanceof SidedInventory && direction != null) {
			return new SidedInventoryParticipant(direction, (SidedInventory) inventory);
		}
		return (Participant<?>) inventory;
	}
}
