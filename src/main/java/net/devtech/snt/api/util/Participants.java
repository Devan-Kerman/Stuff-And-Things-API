package net.devtech.snt.api.util;

import java.util.List;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.access.EntityParticipantProvider;
import net.devtech.snt.api.access.ItemParticipantProvider;
import net.devtech.snt.api.util.access.LambdaItemFunction;
import net.devtech.snt.api.util.access.LambdaWorldFunction;
import net.devtech.snt.api.util.participants.EmptyParticipant;
import net.devtech.snt.internal.fluids.BucketParticipant;
import net.devtech.snt.internal.fluids.CauldronParticipant;
import net.devtech.snt.internal.fluids.ShulkerboxParticipant;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;

public class Participants {
	// @formatter:off
	public static final LambdaItemFunction<Class<? extends Item>> PROVIDER_BY_ITEM_CLASS =
			new LambdaItemFunction<>(i -> i.getItem().getClass());
	// @formatter:on
	public static final LambdaItemFunction<Item> PROVIDER_BY_ITEM = new LambdaItemFunction<>(ItemStack::getItem);
	public static final LambdaWorldFunction<Block> PROVIDER_BY_BLOCK = new LambdaWorldFunction<>((entity, state, world
			, pos, direction) -> state.getBlock());
	public static final LambdaWorldFunction<Class<? extends Block>> PROVIDER_BY_BLOCK_CLASS =
			new LambdaWorldFunction<>(
			(entity, state, world, pos, direction) -> state.getBlock().getClass());

	static {
		// item providers
		register((inventory, stack, face) -> {
			Item item = stack.getItem();
			if (item instanceof ItemParticipantProvider) {
				ItemParticipantProvider provider = (ItemParticipantProvider) item;
				return provider.getParticipant(inventory, stack, face);
			}
			return EmptyParticipant.INSTANCE;
		});
		register(PROVIDER_BY_ITEM);
		register(PROVIDER_BY_ITEM_CLASS);
		register((inventory, stack, face) -> {
			Item item = stack.getItem();
			if (item instanceof BlockItem) {
				if (((BlockItem) item).getBlock() instanceof ShulkerBoxBlock) {
					return new ShulkerboxParticipant(stack);
				}
			}
			return EmptyParticipant.INSTANCE;
		});

		// block providers
		register((entity, state, world, pos, direction) -> {
			if (entity instanceof Participant<?>) {
				return (Participant<?>) entity;
			}
			return EmptyParticipant.INSTANCE;
		});
		register((entity, state, world, pos, direction) -> {
			Block block = state.getBlock();
			if (block instanceof BlockParticipantProvider) {
				return ((BlockParticipantProvider) block).getParticipant(entity, state, world, pos, direction);
			}
			return EmptyParticipant.INSTANCE;
		});
		register(PROVIDER_BY_BLOCK);
		register(PROVIDER_BY_ITEM_CLASS);
		register((entity, state, world, pos, direction) -> {
			if (world != null) {
				int x = pos.getX(), y = pos.getY(), z = pos.getZ();
				List<Entity> list = world.getOtherEntities(null,
						new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D),
						e -> e instanceof EntityParticipantProvider);
				for (Entity e : list) {
					EntityParticipantProvider provider = (EntityParticipantProvider) e;
					Participant<?> participant = provider.getParticipant(direction);
					if (participant != EmptyParticipant.INSTANCE) {
						return participant;
					}
				}
			}
			return EmptyParticipant.INSTANCE;
		});

		PROVIDER_BY_BLOCK.register(Blocks.CAULDRON,
				(entity, state, world, pos, direction) -> new CauldronParticipant(world, pos, state));
		PROVIDER_BY_ITEM_CLASS.register(BucketItem.class,
				(inventory, stack, face) -> new BucketParticipant(inventory, stack));
	}

	private static BlockParticipantProvider[] blockProviders = {};
	private static ItemParticipantProvider[] itemProviders = {};

	public static void register(ItemParticipantProvider provider) {
		itemProviders = ArrayUtils.add(itemProviders, provider);
	}

	public static void register(BlockParticipantProvider provider) {
		blockProviders = ArrayUtils.add(blockProviders, provider);
	}

	/**
	 * @param entity should only be null if state#getBlock#hasBlockEntity is false
	 * @return the participant at the given location, accessed from the given face
	 */
	@NotNull
	public static Participant<?> getParticipantAt(@Nullable BlockEntity entity,
			BlockState state,
			WorldAccess world,
			BlockPos pos,
			@Nullable Direction direction) {
		for (BlockParticipantProvider provider : blockProviders) {
			Participant<?> participant = provider.getParticipant(entity, state, world, pos, direction);
			if (participant != EmptyParticipant.INSTANCE) {
				return participant;
			}
		}
		return EmptyParticipant.INSTANCE;
	}

	/**
	 * get an item participant from the itemstack for the given face
	 *
	 * @param output the output inventory for the itemstack, for example buckets being filled with water will try to
	 * 		put water buckets into the participant
	 * @param face just because a block is broken, doesn't mean it suddenly loses it's geometry. Items have faces
	 * 		too!
	 * @return the participant for the itemstack
	 */
	@NotNull
	public static Participant<?> getParticipant(Participant<?> output, ItemStack stack, @Nullable Direction face) {
		// item providers
		for (ItemParticipantProvider provider : itemProviders) {
			Participant<?> participant = provider.getParticipant(output, stack, face);
			if (participant != EmptyParticipant.INSTANCE) {
				return participant;
			}
		}

		return EmptyParticipant.INSTANCE;
	}

	/**
	 * @return the participant at the given location, accessed from the given face
	 */
	public static Participant<?> getParticipantAt(WorldAccess world, BlockPos pos, @Nullable Direction direction) {
		Chunk chunk = world.getChunk(pos);
		BlockState state = chunk.getBlockState(pos);
		if (state.getBlock().hasBlockEntity()) {
			return getParticipantAt(chunk.getBlockEntity(pos), state, world, pos, direction);
		}
		return getParticipantAt(null, state, world, pos, direction);
	}

	/**
	 * @return the participant at the given location, accessed from the given face
	 */
	public static Participant<?> getParticipantAt(@NotNull BlockState state,
			WorldAccess world,
			BlockPos pos,
			@Nullable Direction direction) {
		if (state.getBlock().hasBlockEntity()) {
			return getParticipantAt(world.getBlockEntity(pos), state, world, pos, direction);
		}
		return getParticipantAt(null, state, world, pos, direction);
	}

	/**
	 * get the participant from a itemstack
	 */
	public static Participant<?> getParticipant(Participant<?> output, ItemStack stack) {
		return getParticipant(output, stack, null);
	}
}