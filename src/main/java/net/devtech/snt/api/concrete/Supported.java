package net.devtech.snt.api.concrete;


/**
 * This should be implemented on a <b>Participant</b> class, and it declares what types this Participant can reasonably support
 */
public interface Supported {
	/**
	 * Even if at that exact moment the object can't have stuff pulled out of it, that's fine, return true.
	 * For example if a tank is empty, technically u cant pull fluid out, but it does support it.
	 *
	 * the types that can be pulled out of this participant, this doesn't guarantee that mods wont call your push/pull methods with valid types though.
	 */
	boolean isPullSupported(Class<?> type);

	/**
	 * the types that can be pushed into this participant
	 */
	boolean isPushSupported(Class<?> type);
}
