package net.devtech.snt.internal.fluids;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.concrete.WildParticipant;
import net.devtech.snt.api.util.FluidUtil;
import org.jetbrains.annotations.Range;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class CauldronParticipant implements WildParticipant<Integer> {
	private final WorldAccess world;
	private final BlockPos pos;
	private BlockState current;

	public CauldronParticipant(WorldAccess world, BlockPos pos, BlockState current) {
		this.world = world;
		this.pos = pos;
		this.current = current;
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int take(Transaction transaction,
			Object type,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		int level = transaction.getOrCompute(this, i -> i.current.get(CauldronBlock.LEVEL));
		int take = Math.min(FluidUtil.floorDiv(amount, FluidUtil.ONE_THIRD), level);
		transaction.mutate(this, take, (i, dt) -> i-=dt);
		return (amount - FluidUtil.ONE_THIRD * take);
	}

	@Override
	public @Range (from = 0, to = Integer.MAX_VALUE) int push(Transaction transaction,
			Object type,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		int level = transaction.getOrCompute(this, i -> i.current.get(CauldronBlock.LEVEL));
		int toAdd = Math.min(FluidUtil.floorDiv(amount, FluidUtil.ONE_THIRD), 3-level);
		transaction.mutate(this, toAdd, (i, dt) -> i+=dt);
		return (amount - FluidUtil.ONE_THIRD * toAdd);
	}

	@Override
	public void onCommit(Integer obj) {
		this.world.setBlockState(this.pos, this.current = this.current.with(CauldronBlock.LEVEL, obj), 2);
	}

	// prevent duplication and exploits by technical players
	@Override
	public void postCommit(Integer obj) {
		this.world.updateNeighbors(this.pos, Blocks.CAULDRON);
	}

	// prevent multiple cauldron participants at the same position from conflicting
	@Override
	public boolean isEqual(Participant<?> other) {
		if (other instanceof CauldronParticipant) {
			CauldronParticipant participant = (CauldronParticipant) other;
			return participant.world.equals(this.world) && participant.pos.equals(this.pos);
		}
		return false;
	}

	@Override
	public void transfer(Transaction transaction,
			Participant<?> destination,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount) {
		int level = transaction.getOrCompute(this, i -> i.current.get(CauldronBlock.LEVEL));
		int take = Math.min(FluidUtil.floorDiv(amount, FluidUtil.ONE_THIRD), level);
		Transaction inner = new Transaction();
		// attempt to push our amount through into the destination
		destination.push(inner, Fluids.WATER, take*FluidUtil.ONE_THIRD);
		transaction.mutate(this, take, (i, dt) -> i-=dt);
	}
}
