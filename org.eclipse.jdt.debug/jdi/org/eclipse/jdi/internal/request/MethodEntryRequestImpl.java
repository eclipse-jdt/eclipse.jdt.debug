package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.MethodEntryEventImpl;

import com.sun.jdi.request.MethodEntryRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class MethodEntryRequestImpl extends EventRequestImpl implements MethodEntryRequest {
	/**
	 * Creates new MethodEntryRequest.
	 */
	public MethodEntryRequestImpl(VirtualMachineImpl vmImpl) {
		super("MethodEntryRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return MethodEntryEventImpl.EVENT_KIND;
	}
}
