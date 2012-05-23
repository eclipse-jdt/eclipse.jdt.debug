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
package org.eclipse.jdi.internal.event;

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.LocationImpl;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.ValueImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.MonitorWaitedEvent;

/**
 * This class provides an implementation of MonitorContendedEnterEvent according
 * to Sun's 1.6 specs
 * 
 * @since 3.3
 */
public class MonitorWaitedEventImpl extends LocatableEventImpl implements
		MonitorWaitedEvent {

	/** Jdwp event id **/
	public static final byte EVENT_KIND = EVENT_MONITOR_WAITED;

	/** if the wait timed out or not **/
	private boolean fTimedOut;

	/** the monitor reference **/
	private ObjectReference fMonitor;

	/** Constructor **/
	private MonitorWaitedEventImpl(VirtualMachineImpl vmImpl,
			RequestID requestID) {
		super("MonitorWaited", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static MonitorWaitedEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		MonitorWaitedEventImpl event = new MonitorWaitedEventImpl(vmImpl,
				requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		event.fMonitor = ObjectReferenceImpl.readObjectRefWithTag(target,
				dataInStream);
		event.fLocation = LocationImpl.read(target, dataInStream);
		event.fTimedOut = ((BooleanValue) ValueImpl.readWithTag(target,
				dataInStream)).value();
		return event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.event.MonitorWaitedEvent#monitor()
	 */
	public ObjectReference monitor() {
		return fMonitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.event.MonitorWaitedEvent#timedout()
	 */
	public boolean timedout() {
		return fTimedOut;
	}

}
