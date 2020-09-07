package net.devtech.fixedfluids.impl;

import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.Util.ONE_BUCKET;

import java.util.function.Predicate;

import net.devtech.fixedfluids.api.FluidWildcardParticipant;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.FluidVolume;
import net.devtech.fixedfluids.api.util.Transaction;
import org.jetbrains.annotations.NotNull;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * todo combine empty and filled logic into one
 */
public abstract class AbstractFilledBucketItemParticipant implements Participant.State<Integer>, FluidWildcardParticipant<Integer> {
	public static final class Bucket extends AbstractFilledBucketItemParticipant {
		public Bucket(Participant<?> inventory, ItemStack original, Fluid fluid) {
			super(inventory, original, fluid);
		}

		@Override
		protected Item empty() {
			return Items.BUCKET;
		}
	}

	private final Participant<?> inventory;
	private final ItemStack original;
	private final Fluid fluid;

	public AbstractFilledBucketItemParticipant(Participant<?> inventory, ItemStack original, Fluid fluid) {
		this.inventory = inventory;
		this.original = original;
		this.fluid = fluid;
	}

	protected abstract Item empty();

	@Override
	public long take(Transaction transaction, Object type, long amount) {
		if(type == this.fluid) return amount;

		// emptying the stack
		Integer count = transaction.getOrDefault(this, this.original.getCount());
		// how much we can take out given the number of buckets
		amount = Math.min(floorDiv(amount, ONE_BUCKET), count);
		// the amount of empty buckets the inventory can actually take
		if(amount == 0) return 0;
		amount -= this.inventory.add(transaction, this.empty(), amount);
		transaction.set(this, count - (int)amount);
		return amount * ONE_BUCKET;
	}

	@Override
	public @NotNull FluidVolume take(Transaction transaction, Predicate<Fluid> stack, long amount) {
		if(stack.test(this.fluid)) {
			return new FluidVolume(this.fluid, this.take(transaction, this.fluid, amount));
		}
		return FluidVolume.EMPTY;
	}

	@Override
	public long add(Transaction transaction, Object type, long amount) {
		return amount;
	}

	@Override
	public void onCommit(Integer data) {
		this.original.setCount(data);
	}
}
