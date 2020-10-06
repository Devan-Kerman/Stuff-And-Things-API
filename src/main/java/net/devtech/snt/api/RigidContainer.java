package net.devtech.snt.api;

import java.util.Iterator;

import net.devtech.snt.api.util.data.Capacity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * a container that has 'slots' in which a thing can take up space, like an inventory.
 * This is used for things like the one probe, WAILA or AE2 for their displays
 */
public interface RigidContainer extends Iterable<Capacity<?>>, InternalParticipant {
	/**
	 * @see Capacity#getItemStack(Inventory, ItemStack)
	 * @return the capacities of all of the internal objects in this rigid container
	 */
	@NotNull
	@Override
	Iterator<Capacity<?>> iterator();
}
