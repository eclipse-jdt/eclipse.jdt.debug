package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ClassPrepareEventImpl;

import com.sun.jdi.request.ClassPrepareRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ClassPrepareRequestImpl extends EventRequestImpl implements ClassPrepareRequest {
	/**
	 * Creates new ClassPrepareRequest.
	 */
	public ClassPrepareRequestImpl(VirtualMachineImpl vmImpl) {
		super("ClassPrepareRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ClassPrepareEventImpl.EVENT_KIND;
	}
}
