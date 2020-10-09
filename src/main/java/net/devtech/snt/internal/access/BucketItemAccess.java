package net.devtech.snt.internal.access;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

public interface BucketItemAccess {
	Fluid getFluid();
	Item getFilled(Fluid fluid);
	Item getEmpty();
}
