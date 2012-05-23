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
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.Locatable;
import com.sun.jdi.Location;

/**
 * This class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public abstract class LocatableEventImpl extends EventImpl implements Locatable {
	/** Location where event occurred. */
	protected LocationImpl fLocation;

	/**
	 * Creates new LocatableEventImpl, only used by subclasses.
	 */
	protected LocatableEventImpl(String description, VirtualMachineImpl vmImpl,
			RequestID requestID) {
		super(description, vmImpl, requestID);
	}

	/**
	 * Reads Thread and Location.
	 */
	public void readThreadAndLocation(MirrorImpl target,
			DataInputStream dataInStream) throws IOException {
		fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		fLocation = LocationImpl.read(target, dataInStream);
	}

	/**
	 * @return Returns Location where event occurred.
	 */
	public Location location() {
		return fLocation;
	}
}
