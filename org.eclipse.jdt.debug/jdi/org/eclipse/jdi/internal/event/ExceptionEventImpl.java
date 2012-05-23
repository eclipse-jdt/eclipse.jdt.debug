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
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.ExceptionEvent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ExceptionEventImpl extends LocatableEventImpl implements
		ExceptionEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_EXCEPTION;

	/** Thrown exception. */
	private ObjectReferenceImpl fException;
	/** Location of catch, or 0 if not caught. */
	private LocationImpl fCatchLocation;

	/**
	 * Creates new ExceptionEventImpl.
	 */
	private ExceptionEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ExceptionEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static ExceptionEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ExceptionEventImpl event = new ExceptionEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		event.fException = ObjectReferenceImpl.readObjectRefWithTag(target,
				dataInStream);
		event.fCatchLocation = LocationImpl.read(target, dataInStream);
		return event;
	}

	/**
	 * @return Returns the location where the exception will be caught.
	 */
	public Location catchLocation() {
		return fCatchLocation;
	}

	/**
	 * @return Returns the thrown exception object.
	 */
	public ObjectReference exception() {
		return fException;
	}
}
