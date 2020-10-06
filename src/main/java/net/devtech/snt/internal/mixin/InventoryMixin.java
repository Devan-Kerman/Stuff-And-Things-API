package net.devtech.snt.internal.mixin;

import java.util.Iterator;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.RigidContainer;
import net.devtech.snt.api.Supported;
import net.devtech.snt.api.WildParticipant;
import net.devtech.snt.internal.inventory.InventoryBackedState;
import net.devtech.snt.api.util.data.Capacity;
import net.devtech.snt.internal.inventory.InventoryCapacityIterator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Inventory.class)
public interface InventoryMixin extends WildParticipant<InventoryBackedState>, Supported, RigidContainer {
	@Override
	default void transfer(Transaction transaction, Participant<?> destination, int amount) {
		if(!(destination instanceof Supported) || ((Supported) destination).isPushSupported(ItemStack.class)) {
			transaction.getOrCompute(this, InventoryBackedState::of).transferTo(transaction, destination, amount);
		}
	}

	@Override
	default int take(Transaction transaction, Object type, int amount) {
		if (type instanceof Item) {
			return transaction.getOrCompute(this, InventoryBackedState::of).take((Item) type, amount);
		}
		return 0;
	}

	@Override
	default int push(Transaction transaction, Object type, int amount) {
		if (type instanceof ItemStack) {
			ItemStack cast = ((ItemStack) type).copy();
			cast.setCount(amount);
			return transaction.getOrCompute(this, InventoryBackedState::of).addStack(cast).getCount();
		}
		return amount;
	}

	@Override
	default void onCommit(InventoryBackedState obj) {
		obj.copyTo((Inventory) this);
	}

	@Override
	default @NotNull Iterator<Capacity<?>> iterator() {
		return new InventoryCapacityIterator((Inventory) this);
	}

	@Override
	default boolean isPullSupported(Class<?> type) {
		return type == ItemStack.class;
	}

	@Override
	default boolean isPushSupported(Class<?> type) {
		return type == ItemStack.class;
	}
}
