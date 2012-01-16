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

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.event.BreakpointEvent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class BreakpointEventImpl extends LocatableEventImpl implements
		BreakpointEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_BREAKPOINT;

	/**
	 * Creates new BreakpointEventImpl.
	 * @param vmImpl the VM
	 * @param requestID the request ID
	 */
	private BreakpointEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("BreakpointEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @param target the target
	 * @param requestID the request ID
	 * @param dataInStream the stream
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 * @throws IOException if the read fails
	 */
	public static BreakpointEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		BreakpointEventImpl event = new BreakpointEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		((ThreadReferenceImpl) event.thread()).setIsAtBreakpoint();
		return event;
	}
}
