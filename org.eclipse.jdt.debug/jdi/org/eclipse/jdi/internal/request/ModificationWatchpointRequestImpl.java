package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ModificationWatchpointEventImpl;

import com.sun.jdi.request.ModificationWatchpointRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ModificationWatchpointRequestImpl extends WatchpointRequestImpl implements ModificationWatchpointRequest {
	/**
	 * Creates new ModificationWatchpointRequest.
	 */
	public ModificationWatchpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("ModificationWatchpointRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ModificationWatchpointEventImpl.EVENT_KIND;
	}
}
