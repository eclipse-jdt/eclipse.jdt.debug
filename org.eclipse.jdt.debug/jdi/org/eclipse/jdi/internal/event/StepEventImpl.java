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
public class StepEventImpl extends LocatableEventImpl implements StepEvent {
	/** Jdwp Event Kind. */
	public static final int EVENT_KIND = EVENT_SINGLE_STEP;

	/**
	 * Creates new StepEventImpl.
	 */
	private StepEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("StepEvent", vmImpl, requestID);
	}
		
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static StepEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		StepEventImpl event = new StepEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		return event;
   	}
}
