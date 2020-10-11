package net.devtech.snt.api.concrete;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.util.data.TypeSlot;
import net.devtech.snt.api.util.participants.fluid.FixedVolumeFixedUnitParticipant;
import org.jetbrains.annotations.Range;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

/**
 * for containers that can be iterated through, this is recommended to be implemented whenever possible
 */
public interface WildParticipant<T> extends Participant<T> {
	/**
	 * take an amount of something from the participant and put it into the destination.
	 * This allows for complete customizability and perfect filtering.
	 * universally recognized types: {@link Fluid} and {@link ItemStack}
	 *
	 * @see #take(Transaction, int)
	 * @param transaction the current transaction
	 * @param amount the amount to take, must be positive
	 */
	void transfer(Transaction transaction, Participant<?> destination, @Range(from = 0, to = Integer.MAX_VALUE) int amount);

	/**
	 * takes one bucket, of any fluid
	 */
	default TypeSlot<Fluid> take(Transaction transaction, int amount) {
		FixedVolumeFixedUnitParticipant participant = new FixedVolumeFixedUnitParticipant(amount, amount);
		this.transfer(transaction, participant, amount);
		return participant.getCapacity();
	}
}
