package net.devtech.fixedfluids.mixin.lba_compat;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.ItemStack;

@Mixin (value = Participant.class, remap = false)
public interface ItemParticipantMixin extends ItemInsertable {
	@Shadow long add(Transaction transaction, Object type, long amount);

	@Override
	default ItemStack attemptInsertion(ItemStack stack, Simulation simulation) {
		Transaction transaction = new Transaction();
		int amount = (int) this.add(transaction, stack, stack.getCount());
		if(simulation.isSimulate()) {
			transaction.abort();
		} else {
			transaction.commit();
		}
		ItemStack copy = stack.copy();
		copy.setCount(amount);
		return copy;
	}
}
