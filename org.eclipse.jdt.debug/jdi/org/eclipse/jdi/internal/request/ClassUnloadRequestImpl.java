package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ClassUnloadEventImpl;

import com.sun.jdi.request.ClassUnloadRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ClassUnloadRequestImpl extends EventRequestImpl implements ClassUnloadRequest {
	/**
	 * Creates new ClassUnloadRequest.
	 */
	public ClassUnloadRequestImpl(VirtualMachineImpl vmImpl) {
		super("ClassUnloadRequest", vmImpl); //$NON-NLS-1$
	}
	
	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ClassUnloadEventImpl.EVENT_KIND;
	}
}
