package net.devtech.fixedfluids.api.util;

import static java.lang.Math.floorDiv;
import static java.lang.Math.max;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import net.devtech.fixedfluids.api.BlockParticipant;
import net.devtech.fixedfluids.api.ItemParticipant;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.impl.CauldronParticipant;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Util {
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

	static {
		register(Blocks.CAULDRON, (BlockParticipant<Integer>) (state, world, pos, face) -> new CauldronParticipant(world, pos, state));
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

	public static Participant<?> get(World world, BlockPos pos, @Nullable Direction direction) {
		Participant<?> participant = getNullable(world, pos, direction);
		if (participant == null) {
			return EmptyParticipant.INSTANCE;
		}
		return participant;
	}

	public static Optional<Participant<?>> getOptional(World world, BlockPos pos, @Nullable Direction direction) {
		return Optional.ofNullable(getNullable(world, pos, direction));
	}

	public static @Nullable Participant<?> getNullable(World world, BlockPos pos, @Nullable Direction direction) {
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

	/**
	 * number of drops per bucket, I'd recommend serializing quantities as fractions in the event I change this number
	 */
	public static final long ONE_BUCKET = 72;
	public static final long ONE_THIRD = fraction(1, 3);

	public static CompoundTag safeWrite(CompoundTag tag, long amount) {
		tag.putLong("amount", amount);
		tag.putLong("bucket", ONE_BUCKET);
		return tag;
	}

	public static long safeRead(CompoundTag tag) {
		long amount = tag.getLong("amount"), bucket = tag.getLong("bucket");
		double ratio = ((double) ONE_BUCKET) / bucket;
		return (long) (amount * ratio);
	}

	/**
	 * [min, max]
	 */
	public static long clamp(long val, long min, long max) {
		return val > max ? max : max(val, min);
	}

	public static long fraction(long numerator, long denominator) {
		long num = ONE_BUCKET * numerator;
		if (num % denominator == 0) {
			return num / denominator;
		}
		throw new IllegalArgumentException("invalid fraction!");
	}


	private static final Map<Item, ItemParticipant<?>> ITEM_PARTICIPANT_MAP = new WeakHashMap<>();
	private static final Map<Block, BlockParticipant<?>> BLOCK_PARTICIPANT_MAP = new WeakHashMap<>();

	public static long floor(long val, long floor) {
		return Math.floorDiv(val, floor) * floor;
	}
}
