package net.devtech.snt.api.util.participants;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.WildParticipant;
import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.util.FluidUtil;
import net.devtech.snt.api.util.participants.fluid.FixedVolumeFixedUnitParticipant;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

/**
 * adds compatibility to a BlockParticipantProvider with Mojang fluids
 */
public interface MojangFluidBlockParticipant<K extends WildParticipant<?>> extends FluidFillable, FluidDrainable, BlockParticipantProvider<K> {
	@Override
	default Fluid tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
		Participant<?> participant = Participant.getParticipantAt(state, world, pos, null);
		Transaction transaction = new Transaction();
		if (participant instanceof WildParticipant) {
			FixedVolumeFixedUnitParticipant unit = new FixedVolumeFixedUnitParticipant(FluidUtil.DENOMINATOR, FluidUtil.DENOMINATOR);
			((WildParticipant<?>) participant).transfer(transaction, unit, FluidUtil.DENOMINATOR);
			if (unit.getFluid() == Fluids.EMPTY) {
				transaction.abort();
			} else {
				transaction.commit();
				return unit.getFluid();
			}
		}
		return Fluids.EMPTY;
	}

	@Override
	default boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		if(world instanceof WorldAccess) {
			Transaction transaction = new Transaction();
			int leftOver = Participant.getParticipantAt(state, (WorldAccess) world, pos, null).take(transaction, fluid, FluidUtil.DENOMINATOR);
			transaction.abort();
			return leftOver == 0;
		}
		return false;
	}

	@Override
	default boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if(fluidState.isStill()) {
			Transaction transaction = new Transaction();
			int leftOver = Participant.getParticipantAt(state, world, pos, null).take(transaction, fluidState.getFluid(), FluidUtil.DENOMINATOR);
			if (leftOver == 0) {
				transaction.commit();
				return true;
			} else {
				transaction.abort();
				return false;
			}
		}
		return false;
	}
}
