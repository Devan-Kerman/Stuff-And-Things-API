package net.devtech.snt.api.util.participants.fluid;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.util.FluidUtil;
import org.jetbrains.annotations.Range;

import net.minecraft.fluid.Fluid;

/**
 * it's a cauldron
 */
public class FixedVolumeFixedUnitParticipant extends FixedVolumeParticipant {
	private final int unit;

	public FixedVolumeFixedUnitParticipant(int volume, int unit) {
		super(volume);
		this.unit = unit;
	}

	public FixedVolumeFixedUnitParticipant(int volume, Fluid fluid, int amount, int unit) {
		super(volume, fluid, amount);
		this.unit = unit;
	}

	@Override
	public void transfer(Transaction transaction, Participant<?> destination, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		super.transfer(transaction, destination, FluidUtil.floorDiv(amount, this.unit));
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int take(Transaction transaction, Object type, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		return super.take(transaction, type, FluidUtil.floorDiv(amount, this.unit));
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int push(Transaction transaction, Object type, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		return super.push(transaction, type, FluidUtil.floorDiv(amount, this.unit));
	}
}
