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

import java.util.Vector;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
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
 * An event reader that continuously reads events coming from the VM
 * and dispatch them to the registered listeners.
 */

public class EventReader extends AbstractReader {
	private EventQueue fEventQueue;
	private Vector<EventListener> fEventListeners = new Vector<>(); // A Vector of EventListener

	/**
	 * Constructor
	 * @param name
	 * @param queue
	 */
	public EventReader(String name, EventQueue queue) {
		super(name);
		fEventQueue = queue;
	}
	/**
	 * Registers the given event listener.
	 * @param listener
	 */
	public synchronized void addEventListener(EventListener listener) {
		fEventListeners.addElement(listener);
	}
	/**
	 * Dispatches the given event to the given listener.
	 * Returns whether the VM should be resumed.
	 */
	private boolean dispath(Event event, EventListener listener) {
		if (event instanceof AccessWatchpointEvent)
			return listener.accessWatchpoint((AccessWatchpointEvent) event);
		if (event instanceof BreakpointEvent)
			return listener.breakpoint((BreakpointEvent) event);
		if (event instanceof ClassPrepareEvent)
			return listener.classPrepare((ClassPrepareEvent) event);
		if (event instanceof ClassUnloadEvent)
			return listener.classUnload((ClassUnloadEvent) event);
		if (event instanceof ExceptionEvent)
			return listener.exception((ExceptionEvent) event);
		if (event instanceof MethodEntryEvent)
			return listener.methodEntry((MethodEntryEvent) event);
		if (event instanceof MethodExitEvent)
			return listener.methodExit((MethodExitEvent) event);
		if (event instanceof ModificationWatchpointEvent)
			return listener.modificationWatchpoint(
				(ModificationWatchpointEvent) event);
		if (event instanceof StepEvent)
			return listener.step((StepEvent) event);
		if (event instanceof ThreadDeathEvent)
			return listener.threadDeath((ThreadDeathEvent) event);
		if (event instanceof ThreadStartEvent)
			return listener.threadStart((ThreadStartEvent) event);
		if (event instanceof VMDisconnectEvent)
			return listener.vmDisconnect((VMDisconnectEvent) event);
		if (event instanceof VMDeathEvent)
			return listener.vmDeath((VMDeathEvent) event);
		return true;
	}
	/**
	 * Continuously reads events that are coming from the event queue.
	 */
	@Override
	protected void readerLoop() {
		while (!fIsStopping) {
			try {
				if (!fIsStopping) {
					// Get the next event
					EventSet eventSet = fEventQueue.remove();

					// Dispatch the events
					boolean shouldGo = true;
					EventIterator iterator = eventSet.eventIterator();
					while (iterator.hasNext()) {
						Event event = iterator.nextEvent();
						for (int i = 0; i < fEventListeners.size(); i++) {
							EventListener listener =
								fEventListeners.elementAt(i);
							shouldGo = shouldGo & dispath(event, listener);
						}
						if (event instanceof VMDeathEvent)
							stop();
					}

					// Let the VM go if it was interrupted
					if ((!fIsStopping)
						&& (eventSet != null)
						&& (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL)
						&& shouldGo)
						synchronized (this) {
							fEventQueue.virtualMachine().resume();
						}
				}
			} catch (InterruptedException e) {
				if (!fIsStopping) {
					System.out.println("Event reader loop was interrupted");
					return;
				}
			} catch (VMDisconnectedException e) {
				return;
			}
		}
	}
	/**
	 * De-registers the given event listener.
	 * @param listener
	 */
	public synchronized void removeEventListener(EventListener listener) {
		fEventListeners.removeElement(listener);
	}
}
