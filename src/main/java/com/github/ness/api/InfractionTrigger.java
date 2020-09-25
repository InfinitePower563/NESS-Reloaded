package com.github.ness.api;

/**
 * An action which is run when a player is detected for a cheat. <br>
 * Usage: <i>Implement</i> and add it using {@link NESSApi#addInfractionTrigger(ViolationTrigger)}.
 *
 * @author A248
 */
@FunctionalInterface
public interface InfractionTrigger {

	/**
	 * Determines the synchronisation context of this trigger. The returned value may be assumed constant
	 * and should therefore be so.
	 * 
	 * @return the synchronisation context
	 */
	default SynchronisationContext context() {
		return SynchronisationContext.FORCE_SYNC;
	}
	
	/**
	 * Called when a player is detected for cheating. <br>
	 * <br>
     * The infraction gives the player detected, the name of the check, and the amount of times it has been violated
	 * 
	 * @param infraction the infraction
	 */
	void trigger(Infraction infraction);
	
	/**
	 * The context in which a trigger may run
	 * 
	 * @author A248
	 *
	 */
	enum SynchronisationContext {
		
		/**
		 * The trigger must run asynchronously
		 * 
		 */
		FORCE_ASYNC,
		/**
		 * The trigger may run in any thread context
		 * 
		 */
		ANY,
		/**
		 * The trigger must run synchronously to the main thread
		 * 
		 */
		FORCE_SYNC
		
	}
	
}
