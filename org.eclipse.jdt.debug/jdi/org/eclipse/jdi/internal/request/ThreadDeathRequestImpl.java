package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ThreadDeathEventImpl;

import com.sun.jdi.request.ThreadDeathRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ThreadDeathRequestImpl extends EventRequestImpl implements ThreadDeathRequest {
	/**
	 * Creates new ThreadDeathRequest.
	 */
	public ThreadDeathRequestImpl(VirtualMachineImpl vmImpl) {
		super("ThreadDeathRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ThreadDeathEventImpl.EVENT_KIND;
	}
}
