package net.devtech.snt.api.util.access;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.access.ItemParticipantProvider;
import net.devtech.snt.api.util.Participants;
import net.devtech.snt.api.util.participants.EmptyParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public final class LambdaItemFunction<T> implements ItemParticipantProvider {
	private final Map<T, ItemParticipantProvider> providers = new HashMap<>();
	private final Function<ItemStack, T> getter;

	public LambdaItemFunction(Function<ItemStack, T> getter) {
		this.getter = getter;
	}
	@Override
	@NotNull
	public Participant<?> getParticipant(Participant<?> output, ItemStack stack, @Nullable Direction face) {
		ItemParticipantProvider provider = this.providers.get(this.getter.apply(stack));
		if(provider != null) {
			return provider.getParticipant(output, stack, face);
		}
		return EmptyParticipant.INSTANCE;
	}

	/**
	 * register a provider for that instance
	 */
	public void register(T instance, ItemParticipantProvider provider) {
		this.providers.put(instance, provider);
	}

	/**
	 * register a provider without registry replacing the item if it's already registered
	 * @return true if the item was registered
	 */
	public boolean registerSafe(T instance, ItemParticipantProvider provider) {
		return this.providers.putIfAbsent(instance, provider) == null;
	}
}
