package net.devtech.snt.api.util.participants;

import java.util.Iterator;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;
import net.devtech.snt.api.concrete.RigidContainer;
import net.devtech.snt.api.concrete.Supported;
import net.devtech.snt.api.concrete.WildParticipant;
import net.devtech.snt.api.util.data.TypeSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum EmptyParticipant implements WildParticipant<Void>, RigidContainer, Supported {
	INSTANCE;

	EmptyParticipant() {}
	private static final Iterator<TypeSlot<?>> EMPTY = new Iterator<TypeSlot<?>>() {
		@Override
		public boolean hasNext() {
			return false;
		}
		@Override
		public TypeSlot<?> next() {
			throw new IllegalStateException();
		}
	};

	@Override
	public int take(Transaction transaction, Object type, int amount) {
		return 0;
	}
	@Override
	public int push(Transaction transaction, Object type, int amount) {
		return amount;
	}
	@Override
	public void onCommit(Void obj) {}

	@Override
	public void transfer(Transaction transaction,
			Participant<?> destination,
			@Range (from = 0, to = Integer.MAX_VALUE) int amount) {}

	@Override
	public @NotNull Iterator<TypeSlot<?>> iterator() {
		return EMPTY;
	}

	@Override
	public boolean isPullSupported(Class<?> type) {
		return false;
	}

	@Override
	public boolean isPushSupported(Class<?> type) {
		return false;
	}
}
