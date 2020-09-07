package net.devtech.fixedfluids.impl;


import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.Util.ONE_BUCKET;

import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class AbstractEmptyBucketItemParticipant implements Participant.State<Integer> {
	public static final class Bucket extends AbstractEmptyBucketItemParticipant {
		public Bucket(Participant<?> inventory, ItemStack stack) {
			super(inventory, stack);
		}

		@Override
		public Item of(Fluid fluid) {
			return fluid.getBucketItem();
		}
	}

	private final Participant<?> inventory;
	private final ItemStack original;

	public AbstractEmptyBucketItemParticipant(Participant<?> inventory, ItemStack stack) {
		this.inventory = inventory;
		this.original = stack;
	}

	public abstract Item of(Fluid fluid);

	@Override
	public long add(Transaction transaction, Object type, long amount) {
		if (!(type instanceof Fluid)) {
			return amount;
		}

		// can only fill empty buckets
		long original = amount;
		Integer count = transaction.getOrDefault(this, this.original.getCount());
		amount = Math.min(floorDiv(amount, ONE_BUCKET), count);
		amount = this.inventory.add(transaction, this.of((Fluid) type), amount);
		transaction.set(this, count + (int) amount);
		return original - amount * ONE_BUCKET;
	}

	@Override
	public long take(Transaction transaction, Object type, long amount) {
		return 0;
	}


	@Override
	public void onCommit(Integer data) {
		if (data != null) {
			this.original.setCount(data);
		}
	}
}
