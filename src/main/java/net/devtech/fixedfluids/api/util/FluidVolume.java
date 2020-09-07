package net.devtech.fixedfluids.api.util;

import java.util.Objects;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public final class FluidVolume {
	public static final FluidVolume EMPTY = new FluidVolume(Fluids.EMPTY, 0);
	private final Fluid fluid;
	private final long amount;

	public FluidVolume(Fluid fluid, long amount) {
		if (fluid == null) {
			throw new IllegalArgumentException("fluid cannot be null!");
		}

		if (fluid == Fluids.EMPTY) {
			this.amount = 0;
		} else {
			if (amount == 0) {
				fluid = Fluids.EMPTY;
			}
			this.amount = amount;
		}

		this.fluid = fluid;
	}

	public boolean isEmpty() {
		return this.fluid == Fluids.EMPTY || this.amount == 0;
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	public long getAmount() {
		return this.amount;
	}

	@Override
	public String toString() {
		return this.amount + " drops of " + this.fluid.getClass()
		                                              .getSimpleName();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof FluidVolume)) {
			return false;
		}

		FluidVolume volume = (FluidVolume) object;

		if (this.amount != volume.amount) {
			return false;
		}
		return Objects.equals(this.fluid, volume.fluid);
	}

	@Override
	public int hashCode() {
		int result = this.fluid.hashCode();
		result = 31 * result + (int) (this.amount ^ (this.amount >>> 32));
		return result;
	}
}
