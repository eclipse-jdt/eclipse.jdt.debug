package org.eclipse.jdi.internal.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ValueImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.Value;
import com.sun.jdi.event.ModificationWatchpointEvent;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ModificationWatchpointEventImpl extends WatchpointEventImpl implements ModificationWatchpointEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_FIELD_MODIFICATION;

	/** Value to be assigned. */
	private ValueImpl fValueToBe;
	
	/**
	 * Creates new ModificationWatchpointEventImpl.
	 */
	private ModificationWatchpointEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ModificationWatchpointEvent", vmImpl, requestID); //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static WatchpointEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ModificationWatchpointEventImpl event = new ModificationWatchpointEventImpl(vmImpl, requestID);
		event.readWatchpointEventFields(target, dataInStream);
		event.fValueToBe = ValueImpl.readWithTag(target, dataInStream);
		return event;
   	}
   	
   	/**
	 * @return Returns value that will be assigned to the field when the instruction completes.
	 */
	public Value valueToBe() {
		return fValueToBe;
	}
}
