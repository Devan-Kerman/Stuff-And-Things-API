package net.devtech.snt.api.util.data;

import org.jetbrains.annotations.Contract;

public interface Mutable<Self extends Mutable<Self>> {
	@Contract("->new")
	Self copy();
}
