/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.jdi.tests;

import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.EventRequest;

/**
 * Listen for a specific kind of event.
 */
public class EventWaiter implements EventListener {
	protected EventRequest fRequest;
	protected boolean fShouldGo;
	protected Event fEvent;
	/**
	 * Creates a new EventWaiter for the given request. Sets whether it
	 * should let the VM go after it got the event.
	 */
	public EventWaiter(EventRequest request, boolean shouldGo) {
		fRequest = request;
		fShouldGo = shouldGo;
	}
	public boolean accessWatchpoint(AccessWatchpointEvent event) {
		return handleEvent(event);
	}
	public boolean methodEntry(MethodEntryEvent event) {
		return handleEvent(event);
	}
	public boolean methodExit(MethodExitEvent event) {
		return handleEvent(event);
	}
	public boolean breakpoint(BreakpointEvent event) {
		return handleEvent(event);
	}
	public boolean classPrepare(ClassPrepareEvent event) {
		return handleEvent(event);
	}
	public boolean classUnload(ClassUnloadEvent event) {
		return handleEvent(event);
	}
	public boolean exception(ExceptionEvent event) {
		return handleEvent(event);
	}
	/**
	 * Handles an incoming event.
	 * Returns whether the VM should be resumed if it was suspended.
	 */
	protected boolean handleEvent(Event event) {
		if ((event.request() != null) && (event.request().equals(fRequest))) {
			notifyEvent(event);
			return fShouldGo;
		} 
		return true;
	}
	public boolean modificationWatchpoint(ModificationWatchpointEvent event) {
		return handleEvent(event);
	}
	/**
	 * Notify any object that is waiting for an event.
	 */
	synchronized protected void notifyEvent(Event event) {
		notify();
		fEvent = event;
	}
	public boolean step(StepEvent event) {
		return handleEvent(event);
	}
	public boolean threadDeath(ThreadDeathEvent event) {
		return handleEvent(event);
	}
	public boolean threadStart(ThreadStartEvent event) {
		return handleEvent(event);
	}
	public boolean vmDeath(VMDeathEvent event) {
		if (fEvent == null) {
			// This is the last event we can ever get an this was not the one we expected
			notifyEvent(null);
			return true;
		}
		return handleEvent(event);
	}
	public boolean vmDisconnect(VMDisconnectEvent event) {
		return handleEvent(event);
	}
	/**
	 * Waits for the first event corresponding to this waiter's request.
	 */
	synchronized public Event waitEvent() throws InterruptedException {
		if (fEvent == null) // If event didn't already come in
			wait();
		Event result = fEvent;
		fEvent = null;
		return result;
	}
	/**
	 * Waits for the first event corresponding to this waiter's request
	 * for the given time (in ms). If it times out, return null.
	 */
	synchronized public Event waitEvent(long time)
		throws InterruptedException {
		if (fEvent == null) // If event didn't already come in
			wait(time);
		Event result = fEvent;
		fEvent = null;
		return result;
	}
}
