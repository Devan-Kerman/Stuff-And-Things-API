package net.devtech.snt.api.access;

import net.devtech.snt.api.Participant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/**
 * Should be implemented on your item class
 */
public interface ItemParticipantProvider {
	/**
	 * get a participant from the item. The inventory is for byproducts, not a source of items. For example filled buckets shouldn't try to pull
	 * empty buckets from the inventory when a stack-compatible fluid is added to it. but Empty buckets being filled <b>are</b> allowed to do so.
	 *
	 * It's best to keep the inventory that the item is in and the inventory where the byproducts go seperate, so they don't accidentally influence each
	 * other when emptied.
	 *
	 * @param inventory a place for byproducts to go
	 * @param face the face to access the item from, just because a block is in item form, doesn't mean it looses it's shape.
	 * @return the inventory
	 */
	@NotNull
	Participant<?> getParticipant(Participant<?> inventory, ItemStack stack, @Nullable Direction face);

	@NotNull
	default Participant<?> getParticipant(Participant<?> inventory, ItemStack stack) {
		return this.getParticipant(inventory, stack, null);
	}
}
