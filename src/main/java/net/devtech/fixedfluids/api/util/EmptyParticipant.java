package net.devtech.fixedfluids.api.util;

import net.devtech.fixedfluids.api.Participant;

public enum EmptyParticipant implements Participant<Object> {
	INSTANCE;

	EmptyParticipant() {}
	@Override
	public long interact(Transaction transaction, Object type, long amount) {
		return amount > 0 ? amount : 0;
	}

	@Override
	public void onAbort(Object data) {}

	@Override
	public void onCommit(Object data) {}
}
