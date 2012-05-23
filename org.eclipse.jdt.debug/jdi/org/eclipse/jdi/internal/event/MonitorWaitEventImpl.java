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

import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.MonitorWaitEvent;

public class MonitorWaitEventImpl extends LocatableEventImpl implements
		MonitorWaitEvent {

	/** Jdwp event kind id **/
	public static final byte EVENT_KIND = EVENT_MONITOR_WAIT;

	/** howl ong the timeout is **/
	private long fTimeOut;

	/** the monitor reference **/
	private ObjectReference fMonitor;

	/** Constructor **/
	private MonitorWaitEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("MonitorWait", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static MonitorWaitEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		MonitorWaitEventImpl event = new MonitorWaitEventImpl(vmImpl, requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		event.fMonitor = ObjectReferenceImpl.readObjectRefWithTag(target,
				dataInStream);
		event.fLocation = LocationImpl.read(target, dataInStream);
		event.fTimeOut = ((LongValue) ValueImpl.readWithTag(target,
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
	 * @see com.sun.jdi.event.MonitorWaitEvent#timeout()
	 */
	public long timeout() {
		return fTimeOut;
	}
}
