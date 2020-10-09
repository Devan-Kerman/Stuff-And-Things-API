package net.devtech.snt.internal.mixin;

import net.devtech.snt.api.util.data.Capacity;
import net.devtech.snt.api.util.data.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.ItemStack;

@Mixin (ItemStack.class)
public abstract class ItemStackMixin implements Capacity<ItemStack>, Mutable {
	@Shadow
	public abstract int getMaxCount();

	@Shadow
	public abstract ItemStack shadow$copy();

	@Shadow
	public abstract int getCount();

	@Override
	public int getCapacity() {
		return this.getMaxCount();
	}

	@Override
	public ItemStack getInstance() {
		return this.shadow$copy();
	}

	@Override
	public int getAmount() {
		return this.getCount();
	}

	@Override
	public Mutable copy() {
		return (Mutable) (Object) this.shadow$copy();
	}
}
