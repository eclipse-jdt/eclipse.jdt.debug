package org.eclipse.jdt.internal.debug.core;

import com.sun.jdi.event.Event;


public interface IJDIEventListener {
	/**
	 * Handles the given event that this listener has registerd for and
	 * returns whether the thread in which the event occurred should
	 * be resumed. All event handlers for the events in an event set
	 * are given a chance to vote on whether the thread should be
	 * resumed. If all agree, the thread is resumed by the event dispatcher.
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target);
}

