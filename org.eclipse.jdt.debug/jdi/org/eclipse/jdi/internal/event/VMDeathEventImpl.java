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

import com.sun.jdi.event.VMDeathEvent;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class VMDeathEventImpl extends EventImpl implements VMDeathEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_VM_DEATH;

	/**
	 * Creates new VMDeathEventImpl.
	 */
	public VMDeathEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("VMDeathEvent", vmImpl, requestID);
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static VMDeathEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		VMDeathEventImpl event = new VMDeathEventImpl(vmImpl, requestID);
		return event;
   	}
}
