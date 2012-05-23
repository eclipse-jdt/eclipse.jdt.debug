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
import org.eclipse.jdi.internal.TypeImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.event.ClassUnloadEvent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ClassUnloadEventImpl extends EventImpl implements ClassUnloadEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_CLASS_UNLOAD;

	/** Type signature. */
	private String fSignature;

	/**
	 * Creates new ClassUnloadEventImpl.
	 */
	private ClassUnloadEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ClassUnloadEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static ClassUnloadEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ClassUnloadEventImpl event = new ClassUnloadEventImpl(vmImpl, requestID);
		event.fSignature = target.readString("signature", dataInStream); //$NON-NLS-1$
		// Remove the class from classes that are known by the application to be
		// loaded in the VM.
		vmImpl.removeKnownRefType(event.fSignature);
		return event;
	}

	/**
	 * @return Returns the name of the class that has been unloaded.
	 */
	public String className() {
		return TypeImpl.signatureToName(fSignature);
	}

	/**
	 * @return Returns the JNI-style signature of the class that has been
	 *         unloaded.
	 */
	public String classSignature() {
		return fSignature;
	}
}
