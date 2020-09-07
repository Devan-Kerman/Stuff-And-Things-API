package net.devtech.fixedfluids.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public interface BlockParticipant<T> {
	Participant<T> get(BlockState state, World world, BlockPos pos, @Nullable Direction face);
}
