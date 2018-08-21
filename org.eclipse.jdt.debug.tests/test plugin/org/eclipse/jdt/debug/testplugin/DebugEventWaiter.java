/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.swt.widgets.Display;

/**
 * The <code>DebugEventWaiter</code> is
 * to wait for a specific kind of debug event.
 * <p>
 * When a <code>DebugEventWaiter</code> is created, it
 * registers itself with the <code>DebugPlugin</code> as
 * a <code>IDebugEventSetListener</code>.
 * <p>
 * NOTE: <code>DebugEventWaiter</code> objects are intended for
 * one time use only!
 */
public class DebugEventWaiter implements IDebugEventSetListener {
	/**
	 * The kind of event the waiter is waiting for
	 */
	protected int fEventType;

	/**
	 * The number of milliseconds the waiter will wait before timing out.
	 */
	protected long fTimeout;

	/**
	 * The <code>IDebugModelManager</code> this waiter is listening to.
	 */
	protected DebugPlugin fDebugPlugin;

	/**
	 * The <code>DebugEvent</code> received.
	 */
	protected DebugEvent fEvent;

	/**
	 * The event set that was accepted
	 */
	protected DebugEvent[] fEventSet;

	/**
	 * Process UI events while waiting in UI thread
	 */
	private boolean enableUIEventLoopProcessing;

	/**
	 * The default timeout value if none is given (20000).
	 */
	public static final long DEFAULT_TIMEOUT= 15000;

	/**
	 * Constructor
	 * Creates a new <code>DebugEventWaiter</code> which
	 * waits for events of a kind <code>eventType</code>.
	 * The wait method will wait the default timeout value.
	 * @param eventType
	 */
	public DebugEventWaiter(int eventType) {
		fDebugPlugin= DebugPlugin.getDefault();
		fEventType= eventType;
		fTimeout= DEFAULT_TIMEOUT;

		fDebugPlugin.addDebugEventListener(this);
	}

	/**
	 * Answers true if the <code>DebugEvent</code> is acceptable.
	 * @param event
	 * @return if the event is accepted or not
	 */
	public boolean accept(DebugEvent event) {
		return event.getKind() == fEventType && event.getDetail() != DebugEvent.EVALUATION_IMPLICIT;
	}
	/**
	 * Answers the event name associated with the given flag.
	 * @return the name of the event kind
	 */
	public String getEventKindName() {
		switch (fEventType) {
			case DebugEvent.CREATE :
				return "Create";
			case DebugEvent.TERMINATE :
				return "Terminate";
			case DebugEvent.RESUME :
				return "Resume";
			case DebugEvent.SUSPEND :
				return "Suspend";
			default :
				return "UNKNOWN";
		}
	}

	/**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	@Override
	public synchronized void handleDebugEvents(DebugEvent[] events) {
		//printReceived(events);
		for (int i = 0; i < events.length; i++) {
			if (accept(events[i])) {
				fEvent= events[i];
				fEventSet = events;
				unregister();
				notifyAll();
				return;
			}
		}
	}

	/**
	 * Prints a message indicating which event was received.
	 */
	protected void printReceived(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			System.out.println(this +" got " + events[i]);
		}
	}

	/**
	 * Sets the number of milliseconds to wait for this callback
	 * @param milliseconds
	 */
	public void setTimeout(long milliseconds) {
		fTimeout= milliseconds;
	}

	/**
	 * Unregisters this waiter as a listener
	 */
	public void unregister() {
		fDebugPlugin.removeDebugEventListener(this);
	}

	/**
	 * Returns the source of the accepted event, or <code>null</code>
	 * if no event was accepted.
	 * @return the source of the event or <code>null</code>
	 */
	public synchronized Object waitForEvent() {
		if (fEvent == null) {
			try {
				if (enableUIEventLoopProcessing && Display.getCurrent() != null) {
					long endTime = System.currentTimeMillis() + fTimeout;
					while (fEvent == null && System.currentTimeMillis() < endTime) {
						wait(10);
						TestUtil.runEventLoop();
					}
				} else {
					wait(fTimeout);
				}
			} catch (InterruptedException ie) {
				System.err.println("Interrupted waiting for event");
			}
		}
		unregister();
		if (fEvent == null) {
			return null;
		}
		return fEvent.getSource();
	}

	/**
	 * Returns the accepted event, if any.
	 * @return the event if any
	 */
	public DebugEvent getEvent() {
		return fEvent;
	}

	/**
	 * Returns the accepted event set, if any.
	 * @return the even if any
	 */
	public DebugEvent[] getEventSet() {
		return fEventSet;
	}

	public void setEnableUIEventLoopProcessing(boolean on) {
		enableUIEventLoopProcessing = on;
	}
}

