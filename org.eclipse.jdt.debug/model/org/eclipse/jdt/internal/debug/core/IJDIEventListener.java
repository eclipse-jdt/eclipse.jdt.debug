/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequest;

/**
 * A jdi event listener is notified of events associated with a specific jdi
 * event request. A listener registers/deregisters event requests with a debug
 * target.
 * 
 * @see JDIDebugTarget#addJDIEventListener(IJDIEventListener, EventRequest)
 * @see JDIDebugTarget#removeJDIEventListener(IJDIEventListener, EventRequest)
 */

public interface IJDIEventListener {
	/**
	 * Handles the given event that this listener has registered for and returns
	 * whether the thread in which the event occurred should be resumed. All
	 * event handlers for the events in an event set are given a chance to vote
	 * on whether the thread should be resumed. If all agree, the thread is
	 * resumed by the event dispatcher. If any event handler returns
	 * <code>false</code> the thread in which the event originated is left in a
	 * suspended state.
	 * <p>
	 * Event listeners are provided with the current state of the suspend vote.
	 * For example, this could allow a conditional breakpoint to not bother
	 * running its evaluation since the vote is already to suspend (if it
	 * coincides with a step end).
	 * </p>
	 * 
	 * @param event
	 *            the event to handle
	 * @param target
	 *            the debug target in which the event occurred
	 * @param suspendVote
	 *            whether the current vote among event listeners is to suspend
	 * @param eventSet
	 *            the event set the event is contained in
	 * @return whether the thread in which the event occurred should be resumed
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet);

	/**
	 * Notification that all event handlers for an event set have handled their
	 * associated events and whether the event set will suspend.
	 * 
	 * @param event
	 *            event the listener was registered for/handled
	 * @param target
	 *            target in which the event occurred
	 * @param suspend
	 *            whether the event will cause the event thread to suspend
	 * @param eventSet
	 *            the event set the event is contained in
	 */
	public void eventSetComplete(Event event, JDIDebugTarget target,
			boolean suspend, EventSet eventSet);
}
