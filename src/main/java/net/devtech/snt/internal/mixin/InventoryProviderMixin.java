package net.devtech.snt.internal.mixin;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.util.InventoryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

@Mixin(InventoryProvider.class)
public interface InventoryProviderMixin extends BlockParticipantProvider {
	@Shadow SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos);

	@Override
	default @NotNull Participant<?> getParticipant(@Nullable BlockEntity entity, BlockState state, WorldAccess world, BlockPos pos, @Nullable Direction direction) {
		return InventoryUtil.getParticipant(this.getInventory(state, world, pos), direction);
	}
}
