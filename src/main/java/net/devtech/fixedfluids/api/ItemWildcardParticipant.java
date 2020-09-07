package net.devtech.fixedfluids.api;

import java.util.function.Predicate;

import net.devtech.fixedfluids.api.util.Transaction;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

/**
 * a participant that can take things selectively
 * @param <T>
 */
public interface ItemWildcardParticipant<T> extends Participant<T> {
	/**
	 * @return the amount actually taken from the container
	 */
	@NotNull
	ItemStack take(Transaction transaction, Predicate<ItemStack> stack, long amount);
}
