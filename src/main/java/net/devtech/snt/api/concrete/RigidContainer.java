package net.devtech.snt.api.concrete;

import java.util.Iterator;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.util.data.TypeSlot;
import org.jetbrains.annotations.NotNull;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * a container that has 'slots' in which a thing can take up space, like an inventory.
 * This can be used for things like the one probe, or WAILA
 * implemented on {@link Participant}
 */
public interface RigidContainer extends Iterable<TypeSlot<?>> {
	/**
	 * @see TypeSlot#getItemStack(Inventory, ItemStack)
	 * @return the capacities of all of the internal objects in this rigid container
	 */
	@NotNull
	@Override
	Iterator<TypeSlot<?>> iterator();
}
