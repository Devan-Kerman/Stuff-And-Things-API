package net.devtech.snt.internal.fluids;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.concrete.RigidContainer;
import net.devtech.snt.api.concrete.Supported;
import net.devtech.snt.api.util.FluidUtil;
import net.devtech.snt.api.util.data.TypeSlot;
import net.devtech.snt.internal.mixin.BucketItemAccess;
import org.jetbrains.annotations.NotNull;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class BucketParticipant implements Participant<Integer>, Supported, RigidContainer {
	private final BucketItemAccess access;
	private final ItemStack stack;
	private final Participant<?> output;

	public BucketParticipant(Participant<?> output, ItemStack stack) {
		this.stack = stack;
		this.output = output;
		this.access = (BucketItemAccess) stack.getItem();
	}

	@Override
	public int take(Transaction transaction, Object type, int amount) {
		if (this.access.getFluid() == type) {
			return this.interact(transaction, Items.BUCKET, amount);
		}
		return 0;
	}

	@Override
	public int push(Transaction transaction, Object type, int amount) {
		if (this.isEmpty() && type instanceof Fluid) {
			return this.interact(transaction, ((Fluid) type).getBucketItem(), amount);
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
	public @NotNull Iterator<TypeSlot<?>> iterator() {
		return Iterators.singletonIterator(TypeSlot.getItemStack(null, this.stack));
	}
}
