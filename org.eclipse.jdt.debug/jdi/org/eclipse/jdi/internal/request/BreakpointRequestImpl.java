package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.event.*;
import org.eclipse.jdi.internal.jdwp.*;
import java.io.*;

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
		super("BreakpointRequest", vmImpl);
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
