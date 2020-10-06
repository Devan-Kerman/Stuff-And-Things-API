package net.devtech.snt.api;

import org.jetbrains.annotations.Range;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

/**
 * for containers that can be iterated through, this is recommended to be implemented whenever possible
 */
public interface WildParticipant<T> extends Participant<T> {
	/**
	 * take an amount of something from the participant.
	 * universally recognized types: {@link Fluid} and {@link ItemStack}
	 *
	 * @param transaction the current transaction
	 * @param amount the amount to take, must be positive
	 */
	void transfer(Transaction transaction, Participant<?> destination, @Range(from = 0, to = Integer.MAX_VALUE) int amount);
}
