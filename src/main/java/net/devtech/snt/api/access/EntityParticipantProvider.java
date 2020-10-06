package net.devtech.snt.api.access;

import net.devtech.snt.api.Participant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.Direction;

public interface EntityParticipantProvider {
	@NotNull
	Participant<?> getParticipant(@Nullable Direction direction);
}
