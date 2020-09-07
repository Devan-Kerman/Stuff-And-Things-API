package net.devtech.fixedfluids.mixin.lba_compat;

import java.util.function.Predicate;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.devtech.fixedfluids.api.FluidWildcardParticipant;
import net.devtech.fixedfluids.api.util.Transaction;
import net.devtech.fixedfluids.api.util.Util;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.fluid.Fluid;

@Mixin(value = FluidWildcardParticipant.class, remap = false)
public interface FluidWildcardParticipantMixin extends FluidExtractable {
	@Shadow @NotNull net.devtech.fixedfluids.api.util.@NotNull FluidVolume take(Transaction transaction, Predicate<Fluid> stack, long amount);

	@Override
	default FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
		Transaction transaction = new Transaction();
		net.devtech.fixedfluids.api.util.FluidVolume volume = this.take(transaction, f -> filter.matches(FluidKeys.get(f)), maxAmount.asLong(Util.ONE_BUCKET));
		if(simulation.isSimulate()) {
			transaction.abort();
		} else {
			transaction.commit();
		}
		return FluidKeys.get(volume.getFluid()).withAmount(FluidAmount.of(volume.getAmount(), Util.ONE_BUCKET));
	}
}
