package org.eclipse.jdi.internal.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.event.StepEvent;

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
		super("StepEvent", vmImpl, requestID); //$NON-NLS-1$
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
