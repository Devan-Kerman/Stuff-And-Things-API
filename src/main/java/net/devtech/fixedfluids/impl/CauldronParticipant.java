package net.devtech.fixedfluids.impl;

import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.FluidUtil.clamp;

import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.FluidUtil;
import net.devtech.fixedfluids.api.util.Transaction;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronParticipant implements Participant.State<Integer> {
	private final World world;
	private final BlockPos pos;
	private final BlockState state;

	public CauldronParticipant(World world, BlockPos pos, BlockState state) {
		this.world = world;
		this.pos = pos;
		this.state = state;
	}

	@Override
	public long interact(Transaction transaction, Object type, long amount) {
		Integer count = transaction.getOrDefault(this, this.state.get(CauldronBlock.LEVEL));
		amount = floorDiv(amount, FluidUtil.ONE_THIRD);
		amount = clamp(amount, count, 3);
		transaction.mutate(this, i -> i - finalAmount);
		return finalAmount;
	}

	@Override
	public void onCommit(Integer data) {
		if (data != null) {
			this.world.setBlockState(this.pos, this.state.with(CauldronBlock.LEVEL, data));
		}
	}
}
