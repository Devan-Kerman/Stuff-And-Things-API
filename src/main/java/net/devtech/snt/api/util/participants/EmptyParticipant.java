package net.devtech.snt.api.util.participants;

import net.devtech.snt.api.Participant;
import net.devtech.snt.api.Transaction;

public enum EmptyParticipant implements Participant<Void> {
	INSTANCE;

	EmptyParticipant() {}

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
}
