package net.devtech.snt.internal.mixin;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.access.ItemParticipantProvider;
import net.devtech.snt.api.util.participants.fluid.BucketParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

@Mixin(BucketItem.class)
public class BucketItemMixin implements ItemParticipantProvider {
	@Override
	public @NotNull Participant<?> getParticipant(Participant<?> inventory, ItemStack stack, @Nullable Direction direction) {
		return new BucketParticipant(stack, inventory);
	}
}
