package net.devtech.snt.v0.api.base;

import static net.minecraft.fluid.Fluids.EMPTY;

import net.devtech.snt.v0.api.fluids.FluidStack;
import net.devtech.snt.v0.api.participants.Extractable;
import net.devtech.snt.v0.api.participants.Insertable;
import net.devtech.snt.v0.api.transactions.Transaction;
import net.devtech.snt.v0.api.transactions.keys.FastKey;

import net.minecraft.Bootstrap;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.registry.Registry;

public class BaseFluidContainer implements Insertable.Fluids, Extractable.Fluids, FastKey.Participant<FluidStack> {
	private final FastKey<FluidStack> key = new FastKey<>(this);
	private final int max, increment;
	private Fluid fluid;
	private int amount;

	public BaseFluidContainer() {
		this(EMPTY, 0);
	}

	public BaseFluidContainer(Fluid fluid, int amount) {
		this(fluid, amount, Integer.MAX_VALUE, 1);
	}

	public BaseFluidContainer(Fluid fluid, int amount, int max, int increment) {
		this.fluid = fluid;
		this.amount = amount;
		this.max = max;
		this.increment = increment;
		this.updateEmpty();
	}

	private void updateEmpty() {
		if (this.amount == 0) {
			this.fluid = EMPTY;
		} else if (this.fluid == EMPTY) {
			this.amount = 0;
		}
	}

	@Override
	public int put(Transaction transaction, Fluid fluid, int amount) {
		amount = Math.floorDiv(amount, this.increment);
		int current = this.amount, max = this.max;
		Fluid cFluid = this.fluid;
		if ((cFluid == EMPTY || cFluid == fluid) && amount > 0 && max != current) {
			// save current state if not already saved
			transaction.putIfAbsentLevel(this.key, cFluid, current, FluidStack::new);
			int toAdd = Math.min(max - current, amount);
			this.amount += toAdd;
			this.fluid = fluid;
			return amount - toAdd;
		}
		return amount;
	}

	@Override
	public int take(Transaction transaction, Fluid fluid, int amount) {
		amount = Math.floorDiv(amount, this.increment);
		int current = this.amount;
		Fluid cFluid = this.fluid;
		if (fluid == cFluid && amount > 0 && current > 0) {
			// save current state if not already saved
			transaction.putIfAbsentLevel(this.key, cFluid, current, FluidStack::new);
			int toTake = Math.min(amount, current);
			this.amount -= toTake;
			this.updateEmpty();
			return toTake;
		}
		return 0;
	}

	/**
	 * revert to old state
	 */
	@Override
	public void abort(FluidStack val) {
		this.fluid = val.getFluid();
		this.amount = val.getAmount();
	}

	@Override
	public String toString() {
		return String.valueOf(Registry.FLUID.getId(this.fluid)) + ' ' + this.amount / 1000 + "mb" + " " + this.amount % 81 + "/81";
	}
}
