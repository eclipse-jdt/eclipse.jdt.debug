package org.eclipse.jdi.internal.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.jdwp.*;
import org.eclipse.jdi.internal.request.*;
import java.io.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class BreakpointEventImpl extends LocatableEventImpl implements BreakpointEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_BREAKPOINT;

	/**
	 * Creates new BreakpointEventImpl.
	 */
	private BreakpointEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("BreakpointEvent", vmImpl, requestID);
	}
	
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static BreakpointEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		BreakpointEventImpl event = new BreakpointEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target,dataInStream);
		((ThreadReferenceImpl)event.thread()).setIsAtBreakpoint();
		return event;
   	}
}
