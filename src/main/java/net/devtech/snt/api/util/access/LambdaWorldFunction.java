package net.devtech.snt.api.util.access;

import java.util.HashMap;
import java.util.Map;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.access.BlockParticipantProvider;
import net.devtech.snt.api.access.ItemParticipantProvider;
import net.devtech.snt.api.util.participants.EmptyParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class LambdaWorldFunction<T> implements BlockParticipantProvider {
	public interface WorldFunction<T> {
		/**
		 * get the participant at the location, or return null
		 */
		@Nullable T apply(@Nullable BlockEntity entity,
				BlockState state,
				WorldAccess world,
				BlockPos pos,
				@Nullable Direction direction);
	}
	private final Map<T, BlockParticipantProvider> providers = new HashMap<>();
	private final WorldFunction<T> getter;

	public LambdaWorldFunction(WorldFunction<T> getter) {
		this.getter = getter;
	}
	@NotNull
	@Override
	public Participant<?> getParticipant(@Nullable BlockEntity entity,
			BlockState state,
			WorldAccess world,
			BlockPos pos,
			@Nullable Direction direction) {
		BlockParticipantProvider provider = this.providers.get(this.getter.apply(entity, state, world, pos, direction));
		if(provider != null) {
			return provider.getParticipant(entity, state, world, pos, direction);
		}
		return EmptyParticipant.INSTANCE;
	}


	/**
	 * register a provider for that instance
	 */
	public void register(T instance, BlockParticipantProvider provider) {
		this.providers.put(instance, provider);
	}

	/**
	 * register a provider without registry replacing the item if it's already registered
	 * @return true if the item was registered
	 */
	public boolean registerSafe(T instance, BlockParticipantProvider provider) {
		return this.providers.putIfAbsent(instance, provider) == null;
	}

}
