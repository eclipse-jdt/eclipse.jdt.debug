package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.BreakpointEventImpl;

import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class BreakpointRequestImpl extends EventRequestImpl implements BreakpointRequest, Locatable {
	/**
	 * Creates new BreakpointRequest.
	 */
	public BreakpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("BreakpointRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns location of Breakpoint Request.
	 */
	public Location location() {
		return (Location)fLocationFilters.get(0);
	}
	
	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return BreakpointEventImpl.EVENT_KIND;
	}

}
