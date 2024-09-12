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
package org.eclipse.jdi.internal.event;

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ClassPrepareEvent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class ClassPrepareEventImpl extends EventImpl implements
		ClassPrepareEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_CLASS_PREPARE;

	/** Reference type for which this event was generated. */
	private ReferenceTypeImpl fReferenceType;

	/**
	 * Creates new BreakpointEventImpl.
	 */
	private ClassPrepareEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ClassPrepareEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has
	 *         already been read.
	 */
	public static ClassPrepareEventImpl read(MirrorImpl target,
			RequestID requestID, DataInputStream dataInStream)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ClassPrepareEventImpl event = new ClassPrepareEventImpl(vmImpl,
				requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		event.fReferenceType = ReferenceTypeImpl.readWithTypeTagAndSignature(
				target, false, dataInStream);
		target.readInt(
				"class status", ReferenceTypeImpl.classStatusStrings(), dataInStream); //$NON-NLS-1$
		return event;
	}

	/**
	 * @return Returns the reference type for which this event was generated.
	 */
	@Override
	public ReferenceType referenceType() {
		return fReferenceType;
	}

	/**
	 * @return Returns the JNI-style signature of the class that has been
	 *         unloaded.
	 */
	public String classSignature() {
		return referenceType().signature();
	}

	@Override
	public String toString() {
		return super.toString() + " referenceType=" + referenceType().name(); //$NON-NLS-1$
	}

}
