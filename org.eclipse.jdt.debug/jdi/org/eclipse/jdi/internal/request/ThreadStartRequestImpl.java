package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ThreadStartEventImpl;

import com.sun.jdi.request.ThreadStartRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ThreadStartRequestImpl extends EventRequestImpl implements ThreadStartRequest {
	/**
	 * Creates new ThreadStartRequest.
	 */
	public ThreadStartRequestImpl(VirtualMachineImpl vmImpl) {
		super("ThreadStartRequest", vmImpl);
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ThreadStartEventImpl.EVENT_KIND;
	}
}
