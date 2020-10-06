package net.devtech.snt.internal;

import it.unimi.dsi.fastutil.Hash;
import net.devtech.snt.api.Participant;

@SuppressWarnings ({
		"unchecked",
		"rawtypes"
})
public class ParticipantStrategy implements Hash.Strategy<Participant> {
	public static final ParticipantStrategy INSTANCE = new ParticipantStrategy();

	@Override
	public int hashCode(Participant o) {
		return o.hashCode();
	}

	@Override
	public boolean equals(Participant a, Participant b) {
		return a.isEqual(b);
	}
}
