package net.devtech.fixedfluids.api.util;

import static java.lang.Math.max;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import net.devtech.fixedfluids.api.BlockParticipant;
import net.devtech.fixedfluids.api.ItemParticipant;
import net.devtech.fixedfluids.api.Participant;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;

public class FluidUtil {
	/**
	 * number of drops per bucket
	 */
	public static final long DROPS = 72;
	public static final long ONE_THIRD = fraction(1, 3);

	public static long clamp(long val, long min, long max) {
		return val > max ? max : max(val, min);
	}

	public static long fraction(long numerator, long denominator) {
		long num = DROPS * numerator;
		if(num % denominator == 0) {
			return num / denominator;
		}
		throw new IllegalArgumentException("invalid fraction!");
	}

	private static final Map<Item, ItemParticipant<?>> ITEM_PARTICIPANT_MAP = new WeakHashMap<>();
	private static final Map<Block, BlockParticipant<?>> BLOCK_PARTICIPANT_MAP = new WeakHashMap<>();

	public static long floor(long val, long floor) {
		return Math.floorDiv(val, floor) * floor;
	}

	/**
	 * for adding vanilla item compat without mixin
	 */
	public static void register(Item item, ItemParticipant<?> participant) {
		ITEM_PARTICIPANT_MAP.put(item, participant);
	}

	/**
	 * for adding vanilla block compat without mixin
	 */
	public static void register(Block block, BlockParticipant<?> participant) {
		BLOCK_PARTICIPANT_MAP.put(block, participant);
	}

	public static Participant<?> get(Participant<?> inventory, ItemStack stack) {
		Participant<?> participant = getNullable(inventory, stack);
		if (participant == null) {
			return EmptyParticipant.INSTANCE;
		}
		return participant;
	}

	public static Optional<Participant<?>> getOptional(Participant<?> inventory, ItemStack stack) {
		return Optional.ofNullable(getNullable(inventory, stack));
	}

	/**
	 * @param inventory a dumping ground for empty buckets, or a source of empty buckets
	 */
	public static Participant<?> getNullable(Participant<?> inventory, ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemParticipant) {
			return ((ItemParticipant) item).get(inventory, stack);
		}
		ItemParticipant<?> participant = ITEM_PARTICIPANT_MAP.get(item);
		if (participant != null) {
			return participant.get(inventory, stack);
		}
		return null;
	}

	public static Participant<?> get(WorldView world, BlockPos pos, @Nullable Direction direction) {
		Participant<?> participant = getNullable(world, pos, direction);
		if (participant == null) {
			return EmptyParticipant.INSTANCE;
		}
		return participant;
	}

	public static Optional<Participant<?>> getOptional(WorldView world, BlockPos pos, @Nullable Direction direction) {
		return Optional.ofNullable(getNullable(world, pos, direction));
	}

	public static @Nullable Participant<?> getNullable(WorldView world, BlockPos pos, @Nullable Direction direction) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BlockParticipant) {
			return ((BlockParticipant) block).get(state, world, pos, direction);
		}
		BlockParticipant<?> participant = BLOCK_PARTICIPANT_MAP.get(block);
		if (participant != null) {
			return participant.get(state, world, pos, direction);
		}
		return null;
	}
}
