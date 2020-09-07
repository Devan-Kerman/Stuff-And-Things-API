package net.devtech.fixedfluids.impl;

import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.Util.clamp;
import static net.devtech.fixedfluids.api.util.Util.noOp;

import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;
import net.devtech.fixedfluids.api.util.Util;

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
		if ((int) amount != amount) {
			return noOp(amount);
		}

		long orig = amount;
		Integer count = transaction.getOrDefault(this, this.state.get(CauldronBlock.LEVEL));
		amount = floorDiv(amount, Util.ONE_THIRD);
		amount += count;
		amount = clamp(amount, 0, 3);
		long finalAmount = amount;
		transaction.mutate(this, i -> (int) finalAmount);
		if (orig > 0) {
			return orig - (finalAmount - count) * Util.ONE_THIRD;
		} else {
			return (count - finalAmount) * Util.ONE_THIRD;
		}
	}

	@Override
	public void onCommit(Integer data) {
		if (data != null) {
			this.world.setBlockState(this.pos, this.state.with(CauldronBlock.LEVEL, data));
		}
	}
}
