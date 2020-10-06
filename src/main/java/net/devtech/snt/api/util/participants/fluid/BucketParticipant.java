package net.devtech.snt.api.util.participants.fluid;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.RigidContainer;
import net.devtech.snt.api.Supported;
import net.devtech.snt.api.util.FluidUtil;
import net.devtech.snt.api.util.data.Capacity;
import net.devtech.snt.internal.access.BucketItemAccess;
import org.jetbrains.annotations.NotNull;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BucketParticipant implements Participant<Integer>, Supported, RigidContainer {
	private final BucketItemAccess access;
	private final ItemStack stack;
	private final Participant<?> output;

	public BucketParticipant(ItemStack stack, Participant<?> output) {
		this.stack = stack;
		this.output = output;
		this.access = (BucketItemAccess) stack.getItem();
	}

	@Override
	public int take(Transaction transaction, Object type, int amount) {
		if (this.access.getFluid() == type) {
			return this.interact(transaction, this.access.getEmpty(), amount);
		}
		return 0;
	}

	@Override
	public int push(Transaction transaction, Object type, int amount) {
		if (this.isEmpty() && type instanceof Fluid) {
			return this.interact(transaction, this.access.getFilled((Fluid) type), amount);
		}
		return amount;
	}

	private int interact(Transaction transaction, Item byproduct, int amount) {
		int floored = Math.min(FluidUtil.floorToBucket(amount), transaction.getOrCompute(this, i -> i.stack.getCount()));
		if (floored > 0) {
			int space = this.output.push(transaction, new ItemStack(byproduct), floored);
			if(space > 0) {
				transaction.mutate(this, i -> i - space);
				return space * FluidUtil.DENOMINATOR;
			}
		}
		return amount;
	}

	@Override
	public void onCommit(Integer obj) {
		this.stack.setCount(obj);
	}

	@Override
	public boolean isPullSupported(Class<?> type) {
		return !this.isEmpty() && Fluid.class.isAssignableFrom(type);
	}

	@Override
	public boolean isPushSupported(Class<?> type) {
		return this.isEmpty() && Fluid.class.isAssignableFrom(type);
	}

	public boolean isEmpty() {
		return this.access.getFluid() == Fluids.EMPTY;
	}

	@Override
	public @NotNull Iterator<Capacity<?>> iterator() {
		return Iterators.singletonIterator(Capacity.getItemStack(null, this.stack));
	}
}
