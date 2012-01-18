/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.event.VMDisconnectEvent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class VMDisconnectEventImpl extends EventImpl implements
		VMDisconnectEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_VM_DISCONNECTED;

	/**
	 * Creates new VMDisconnectEventImpl.
	 */
	public VMDisconnectEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("VMDisconnectEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 * @param target
	 * @param requestID
	 * @param dataInStream
	 * @return the new event
	 */
	public static VMDisconnectEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream) {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		VMDisconnectEventImpl event = new VMDisconnectEventImpl(vmImpl,
				requestID);
		return event;
	}
}
