/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * @param request
	 * @param shouldGo
	 */
	public EventWaiter(EventRequest request, boolean shouldGo) {
		fRequest = request;
		fShouldGo = shouldGo;
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#accessWatchpoint(com.sun.jdi.event.AccessWatchpointEvent)
	 */
	@Override
	public boolean accessWatchpoint(AccessWatchpointEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#methodEntry(com.sun.jdi.event.MethodEntryEvent)
	 */
	@Override
	public boolean methodEntry(MethodEntryEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#methodExit(com.sun.jdi.event.MethodExitEvent)
	 */
	@Override
	public boolean methodExit(MethodExitEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#breakpoint(com.sun.jdi.event.BreakpointEvent)
	 */
	@Override
	public boolean breakpoint(BreakpointEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#classPrepare(com.sun.jdi.event.ClassPrepareEvent)
	 */
	@Override
	public boolean classPrepare(ClassPrepareEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#classUnload(com.sun.jdi.event.ClassUnloadEvent)
	 */
	@Override
	public boolean classUnload(ClassUnloadEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#exception(com.sun.jdi.event.ExceptionEvent)
	 */
	@Override
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
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#modificationWatchpoint(com.sun.jdi.event.ModificationWatchpointEvent)
	 */
	@Override
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
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#step(com.sun.jdi.event.StepEvent)
	 */
	@Override
	public boolean step(StepEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#threadDeath(com.sun.jdi.event.ThreadDeathEvent)
	 */
	@Override
	public boolean threadDeath(ThreadDeathEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#threadStart(com.sun.jdi.event.ThreadStartEvent)
	 */
	@Override
	public boolean threadStart(ThreadStartEvent event) {
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#vmDeath(com.sun.jdi.event.VMDeathEvent)
	 */
	@Override
	public boolean vmDeath(VMDeathEvent event) {
		if (fEvent == null) {
			// This is the last event we can ever get an this was not the one we expected
			notifyEvent(null);
			return true;
		}
		return handleEvent(event);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventListener#vmDisconnect(com.sun.jdi.event.VMDisconnectEvent)
	 */
	@Override
	public boolean vmDisconnect(VMDisconnectEvent event) {
		return handleEvent(event);
	}
	/**
	 * Waits for the first event corresponding to this waiter's request.
	 * @return if the vm should be restarted
	 * @throws InterruptedException
	 */
	synchronized public Event waitEvent() throws InterruptedException {
		if (fEvent == null) {
			wait();
		}
		Event result = fEvent;
		fEvent = null;
		return result;
	}
	/**
	 * Waits for the first event corresponding to this waiter's request
	 * for the given time (in ms). If it times out, return null.
	 * @param time
	 * @return if the vm should be restarted or not
	 * @throws InterruptedException
	 */
	synchronized public Event waitEvent(long time)
		throws InterruptedException {
		if (fEvent == null) {
			wait(time);
		}
		Event result = fEvent;
		fEvent = null;
		return result;
	}
}
