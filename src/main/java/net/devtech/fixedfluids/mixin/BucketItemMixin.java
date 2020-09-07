package net.devtech.fixedfluids.mixin;

import net.devtech.fixedfluids.api.ItemParticipant;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.impl.AbstractEmptyBucketItemParticipant;
import net.devtech.fixedfluids.impl.AbstractFilledBucketItemParticipant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;

@Mixin (BucketItem.class)
public class BucketItemMixin implements ItemParticipant<Integer> {
	@Shadow @Final private Fluid fluid;

	@Override
	public Participant<Integer> get(Participant<?> inventory, ItemStack stack) {
		if (this.fluid == Fluids.EMPTY) {
			return new AbstractEmptyBucketItemParticipant.Bucket(inventory, stack);
		}
		return new AbstractFilledBucketItemParticipant.Bucket(inventory, stack, this.fluid);
	}
}
