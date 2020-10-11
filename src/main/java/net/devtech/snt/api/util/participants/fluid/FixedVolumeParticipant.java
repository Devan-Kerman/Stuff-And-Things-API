package net.devtech.snt.api.util.participants.fluid;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import net.devtech.snt.api.util.data.Mutable;
import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.concrete.RigidContainer;
import net.devtech.snt.api.concrete.Supported;
import net.devtech.snt.api.concrete.WildParticipant;
import net.devtech.snt.api.util.data.TypeSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

/**
 * it's a tank.
 */
public class FixedVolumeParticipant implements WildParticipant<FixedVolumeParticipant>, RigidContainer, Supported, Mutable<FixedVolumeParticipant> {
	protected final int volume;
	private Fluid fluid = Fluids.EMPTY;
	private int amount;

	public static FixedVolumeParticipant getMaxVolume() {
		return new FixedVolumeParticipant(Integer.MAX_VALUE);
	}

	public FixedVolumeParticipant(int volume) {this.volume = volume;}

	public FixedVolumeParticipant(int volume, Fluid fluid, int amount) {
		if (amount <= 0) {
			fluid = Fluids.EMPTY;
		}

		if (fluid == Fluids.EMPTY) {
			amount = 0;
		}

		this.volume = volume;
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int take(Transaction transaction, Object type, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		if (amount > 0 && type == this.fluid) {
			int toTake = Math.min(transaction.getOrCompute(this, FixedVolumeParticipant::copy).amount, amount);
			transaction.mutateMutable(this, i -> i.subtract(toTake));
			return toTake;
		}
		return 0;
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int push(Transaction transaction, Object type, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		if (amount > 0 && type instanceof Fluid) {
			FixedVolumeParticipant participant = transaction.getOrCompute(this, FixedVolumeParticipant::copy);
			if (participant.fluid == Fluids.EMPTY) {
				participant.fluid = (Fluid) type;
			}

			if (participant.fluid == type) {
				int toAdd = Math.min(participant.volume - participant.amount, amount);
				transaction.mutateMutable(this, i -> i.amount += toAdd);
				return amount - toAdd;
			}
		}
		return amount;
	}

	@Override
	public void transfer(Transaction transaction, Participant<?> destination, @Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		FixedVolumeParticipant participant = transaction.getOrCompute(this, FixedVolumeParticipant::copy);
		if (amount > 0 && participant.fluid != Fluids.EMPTY) {
			int left = destination.push(transaction, this.fluid, participant.amount);
			transaction.mutateMutable(this, i -> i.subtract(left));
		}
	}

	@Override
	public void onCommit(FixedVolumeParticipant obj) {
		this.amount = obj.amount;
		this.fluid = obj.fluid;
	}

	@Override
	public @NotNull Iterator<TypeSlot<?>> iterator() {
		return Iterators.singletonIterator(TypeSlot.getFluid(this.fluid, this.amount, this.volume));
	}

	public TypeSlot<Fluid> getCapacity() {
		return TypeSlot.getFluid(this.fluid, this.amount, this.volume);
	}

	@Override
	public boolean isPullSupported(Class<?> type) {
		return Fluid.class.isAssignableFrom(type);
	}

	@Override
	public boolean isPushSupported(Class<?> type) {
		return Fluid.class.isAssignableFrom(type);
	}

	public int getVolume() {
		return this.volume;
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	public int getAmount() {
		return this.amount;
	}

	public void subtract(int amount) {
		this.amount -= amount;
		if(this.amount < 0) this.fluid = Fluids.EMPTY;
	}

	@Override
	public FixedVolumeParticipant copy() {
		return new FixedVolumeParticipant(this.volume, this.fluid, this.amount);
	}
}
