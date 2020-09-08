package net.devtech.fixedfluids.mixin;

import java.util.function.Predicate;

import net.devtech.fixedfluids.api.ItemWildcardParticipant;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;
import net.devtech.fixedfluids.impl.InventoryState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

// todo reverse compat
@Mixin (Inventory.class)
public interface InventoryMixin extends Participant.State<InventoryState>, ItemWildcardParticipant<InventoryState> {
	// needs wrapper class for simulation/copying

	@Shadow
	void setStack(int slot, ItemStack stack);

	@Override
	@NotNull
	default ItemStack take(Transaction transaction, Predicate<ItemStack> stack, long amount) {
		if (amount != (int) amount) {
			return ItemStack.EMPTY;
		}

		InventoryState wrapper = transaction.getOrCompute(this, () -> new InventoryState((Inventory) this));
		return wrapper.take(stack, (int) amount);
	}

	@Override
	default long take(Transaction transaction, Object type, long amount) {
		if (amount != (int) amount) {
			return 0;
		}

		InventoryState wrapper = transaction.getOrCompute(this, () -> new InventoryState((Inventory) this));
		if (type instanceof ItemConvertible) {
			Item item = ((ItemConvertible) type).asItem();
			return wrapper.take(i -> i.getItem() == item, (int) amount).getCount();
		} else if(type instanceof ItemStack) {
			ItemStack two = (ItemStack) type;
			return wrapper.take(i -> InventoryState.canCombine(two, i), (int) amount).getCount();
		}
		return 0;
	}

	@Override
	default long add(Transaction transaction, Object type, long amount) {
		if (amount != (int) amount) {
			return 0;
		}

		InventoryState wrapper = transaction.getOrCompute(this, () -> new InventoryState((Inventory) this));
		if (type instanceof ItemConvertible) {
			return wrapper.addStack(new ItemStack((ItemConvertible) type, (int) amount))
			              .getCount();
		} else if(type instanceof ItemStack)  {
			ItemStack two = (ItemStack) type;
			return wrapper.addStack(two)
			              .getCount();
		}
		return 0;
	}

	@Override
	default void onCommit(InventoryState data) {
		for (int i = 0; i < data.size(); i++) {
			this.setStack(i, data.getStack(i));
		}
	}
}
