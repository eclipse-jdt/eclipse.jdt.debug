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
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.MonitorContendedEnteredEvent;

/**
 * This class provides an implementation of MonitorContendedEnteredEvent
 * according to Sun's 1.6 specs
 * 
 * @since 3.3
 */
public class MonitorContendedEnteredEventImpl extends LocatableEventImpl
		implements MonitorContendedEnteredEvent {

	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_MONITOR_CONTENDED_ENTERED;

	/** the monitor information **/
	private ObjectReference fMonitor;

	/** Constructor **/
	private MonitorContendedEnteredEventImpl(VirtualMachineImpl vmImpl,
			RequestID requestID) {
		super("MonitorContendedEntered", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static MonitorContendedEnteredEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		MonitorContendedEnteredEventImpl event = new MonitorContendedEnteredEventImpl(
				vmImpl, requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		event.fMonitor = ObjectReferenceImpl.readObjectRefWithTag(target,
				dataInStream);
		event.fLocation = LocationImpl.read(target, dataInStream);
		return event;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.event.MonitorContendedEnteredEvent#monitor()
	 */
	public ObjectReference monitor() {
		return fMonitor;
	}
}
