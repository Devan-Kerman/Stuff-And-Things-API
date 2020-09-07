package net.devtech.fixedfluids.mixin.lba_compat;

import java.util.function.Predicate;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import net.devtech.fixedfluids.api.ItemWildcardParticipant;
import net.devtech.fixedfluids.api.util.Transaction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.ItemStack;

@Mixin(value = ItemWildcardParticipant.class, remap = false)
public interface ItemWildcardParticipantMixin extends ItemExtractable {

	@Shadow @NotNull ItemStack take(Transaction transaction, Predicate<ItemStack> stack, long amount);

	@Override
	default ItemStack attemptExtraction(ItemFilter filter, int maxAmount, Simulation simulation) {
		Transaction transaction = new Transaction();
		ItemStack stack = this.take(transaction, filter::matches, maxAmount);
		if(simulation.isSimulate()) {
			transaction.abort();
		} else {
			transaction.commit();
		}
		return stack;
	}

}
