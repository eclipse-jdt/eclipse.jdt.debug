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
public class MethodEntryEventImpl extends LocatableEventImpl implements MethodEntryEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_METHOD_ENTRY;

	/**
	 * Creates new MethodEntryEventImpl.
	 */
	private MethodEntryEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("MethodEntryEvent", vmImpl, requestID);
	}
		
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static MethodEntryEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		MethodEntryEventImpl event = new MethodEntryEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		return event;
   	}
   	
	/**
	 * @return Returns the method that was entered.
	 */
	public Method method() {
		return fLocation.method();
	}
}
