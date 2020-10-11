package net.devtech.snt.internal.fluids;

import net.devtech.snt.internal.inventory.SidedInventoryBackedState;
import net.devtech.snt.internal.inventory.SidedInventoryParticipant;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

public class ShulkerboxParticipant extends SidedInventoryParticipant {
	private final ItemStack stack;

	public ShulkerboxParticipant(ItemStack stack) {
		super(Direction.UP, from(stack));
		this.stack = stack;
	}

	@Override
	public void onCommit(SidedInventoryBackedState obj) {
		super.onCommit(obj);
		this.stack.putSubTag("BlockEntityTag",
				((ShulkerBoxBlockEntity) this.inventory).serializeInventory(this.stack.getOrCreateSubTag(
						"BlockEntityTag")));
	}

	// todo read id and stuff for mod compat
	public static SidedInventory from(ItemStack stack) {
		ShulkerBoxBlockEntity blockEntity = new ShulkerBoxBlockEntity(DyeColor.PURPLE);
		CompoundTag compoundTag = stack.getSubTag("BlockEntityTag");
		if (compoundTag != null) {
			blockEntity.fromTag(Blocks.SHULKER_BOX.getDefaultState(), compoundTag);
		}
		return blockEntity;
	}
}
