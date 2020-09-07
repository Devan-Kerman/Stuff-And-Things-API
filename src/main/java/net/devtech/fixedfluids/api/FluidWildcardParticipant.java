package net.devtech.fixedfluids.api;

import java.util.function.Predicate;

import net.devtech.fixedfluids.api.util.FluidVolume;
import net.devtech.fixedfluids.api.util.Transaction;
import org.jetbrains.annotations.NotNull;

import net.minecraft.fluid.Fluid;

public interface FluidWildcardParticipant<T> extends Participant<T> {
	/**
	 * @return the amount of fluid actually drained
	 */
	@NotNull
	FluidVolume take(Transaction transaction, Predicate<Fluid> stack, long amount);
}
