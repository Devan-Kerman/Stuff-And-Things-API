package net.devtech.fixedfluids.impl;

import static java.lang.Math.floorDiv;
import static net.devtech.fixedfluids.api.util.FluidUtil.ONE_THIRD;

import java.util.function.Predicate;

import net.devtech.fixedfluids.api.FluidWildcardParticipant;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.FluidVolume;
import net.devtech.fixedfluids.api.util.Transaction;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronParticipant implements Participant.State<Integer>, FluidWildcardParticipant<Integer> {
	private final World world;
	private final BlockPos pos;
	private final BlockState state;

	public CauldronParticipant(World world, BlockPos pos, BlockState state) {
		this.world = world;
		this.pos = pos;
		this.state = state;
	}

	@Override
	public long take(Transaction transaction, Object type, long amount) {
		if(type == Fluids.WATER) {
			int levels = (int) Math.min(floorDiv(amount, ONE_THIRD), transaction.getOrCompute(this, () -> this.state.get(CauldronBlock.LEVEL)));
			transaction.mutate(this, i -> i - levels);
			return levels * ONE_THIRD;
		}
		return 0;
	}

	@Override
	public long add(Transaction transaction, Object type, long amount) {
		if(type == Fluids.WATER) {
			int levels = (int) Math.min(floorDiv(amount, ONE_THIRD), 3 - transaction.getOrCompute(this, () -> this.state.get(CauldronBlock.LEVEL)));
			transaction.mutate(this, i -> i + levels);
			return amount - levels * ONE_THIRD;
		}
		return amount;
	}

	@Override
	public void onCommit(Integer data) {
		if (data != null) {
			this.world.setBlockState(this.pos, this.state.with(CauldronBlock.LEVEL, data));
		}
	}

	@Override
	public FluidVolume take(Transaction transaction, Predicate<Fluid> stack, long amount) {
		if(stack.test(Fluids.WATER)) {
			return new FluidVolume(Fluids.WATER, this.take(transaction, Fluids.WATER, amount));
		}
		return FluidVolume.EMPTY;
	}
}
