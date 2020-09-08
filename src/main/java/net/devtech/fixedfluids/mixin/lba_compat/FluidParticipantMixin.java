package net.devtech.fixedfluids.mixin.lba_compat;

import java.math.RoundingMode;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;
import net.devtech.fixedfluids.api.util.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.fluid.Fluid;

@Mixin(Participant.class)
public interface FluidParticipantMixin extends FluidInsertable {
	@Shadow
	long add(Transaction transaction, Object type, long amount);

	@Override
	default alexiil.mc.lib.attributes.fluid.volume.FluidVolume attemptInsertion(alexiil.mc.lib.attributes.fluid.volume.FluidVolume volume, Simulation simulation) {
		Transaction transaction = new Transaction();
		Fluid fluid = volume.getFluidKey().getRawFluid();
		if(fluid != null) {
			int amount = (int) this.add(transaction, fluid, volume.getAmount_F().asLong(FluidUtil.ONE_BUCKET, RoundingMode.FLOOR));
			if (simulation.isSimulate()) {
				transaction.abort();
			} else {
				transaction.commit();
			}
			return volume.withAmount(FluidAmount.of(amount, FluidUtil.ONE_BUCKET));
		}
		return volume;
	}

}
