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
public class ExceptionEventImpl extends LocatableEventImpl implements ExceptionEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_EXCEPTION;

	/** Thrown exception. */
	private ObjectReferenceImpl fException;
	/** Location of catch, or 0 if not caught. */
	private LocationImpl fCatchLocation;

	/**
	 * Creates new ExceptionEventImpl.
	 */
	private ExceptionEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ExceptionEvent", vmImpl, requestID);
	}
		
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static ExceptionEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ExceptionEventImpl event = new ExceptionEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		event.fException = ObjectReferenceImpl.readObjectRefWithTag(target, dataInStream);
		event.fCatchLocation = LocationImpl.read(target, dataInStream);
		return event;
   	}

	/**
	 * @return Returns the location where the exception will be caught.
	 */
	public Location catchLocation() {
		return fCatchLocation;
	}
	
	/**
	 * @return Returns the thrown exception object. 
	 */
	public ObjectReference exception() {
		return fException;
	}
}
