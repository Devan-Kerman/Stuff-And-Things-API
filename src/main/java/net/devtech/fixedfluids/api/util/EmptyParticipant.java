package net.devtech.fixedfluids.api.util;

import net.devtech.fixedfluids.api.Participant;

public enum EmptyParticipant implements Participant<Object> {
	INSTANCE;

	EmptyParticipant() {}

	@Override
	public long take(Transaction transaction, Object type, long amount) {
		return 0;
	}

	@Override
	public long add(Transaction transaction, Object type, long amount) {
		return amount;
	}

	@Override
	public void onAbort(Object data) {}

	@Override
	public void onCommit(Object data) {}
}
