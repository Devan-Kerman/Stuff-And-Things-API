package net.devtech.fixedfluids.api;

import net.minecraft.item.ItemStack;

public interface ItemParticipant<T> {
	/**
	 * @param inventory a place to put byproducts, like empty buckets
	 * @param stack the current itemstack
	 */
	Participant<T> get(Participant<?> inventory, ItemStack stack);
}
