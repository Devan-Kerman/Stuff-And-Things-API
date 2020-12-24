package net.devtech.snt.v0.api.fluids;

import java.util.Objects;

import net.minecraft.fluid.Fluid;

public class FluidStack {
	private final Fluid fluid;
	private final int amount;

	public FluidStack(Fluid fluid, int amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	public int getAmount() {
		return this.amount;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof FluidStack)) {
			return false;
		}

		FluidStack stack = (FluidStack) object;

		if (this.amount != stack.amount) {
			return false;
		}
		return Objects.equals(this.fluid, stack.fluid);
	}

	@Override
	public int hashCode() {
		int result = this.fluid != null ? this.fluid.hashCode() : 0;
		result = 31 * result + this.amount;
		return result;
	}
}
