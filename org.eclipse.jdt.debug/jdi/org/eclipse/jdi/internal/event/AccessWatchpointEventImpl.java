package org.eclipse.jdi.internal.event;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
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
public class AccessWatchpointEventImpl extends WatchpointEventImpl implements AccessWatchpointEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_FIELD_ACCESS;
	
	/**
	 * Creates new AccessWatchpointEventImpl.
	 */
	protected AccessWatchpointEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("AccessWatchpointEvent", vmImpl, requestID);
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static WatchpointEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		AccessWatchpointEventImpl event = new AccessWatchpointEventImpl(vmImpl, requestID);
		event.readWatchpointEventFields(target, dataInStream);
		return event;
   	}
}
