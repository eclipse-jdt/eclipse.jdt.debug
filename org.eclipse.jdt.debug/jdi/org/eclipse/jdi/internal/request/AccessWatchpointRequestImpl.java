package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.AccessWatchpointEventImpl;

import com.sun.jdi.request.AccessWatchpointRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class AccessWatchpointRequestImpl extends WatchpointRequestImpl implements AccessWatchpointRequest {
	/**
	 * Creates new AccessWatchpointRequest.
	 */
	public AccessWatchpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("AccessWatchpointRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return AccessWatchpointEventImpl.EVENT_KIND;
	}
}
