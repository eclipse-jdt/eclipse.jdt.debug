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
public abstract class WatchpointEventImpl extends LocatableEventImpl implements WatchpointEvent {
	/** The field that is about to be accessed/modified. */
	protected FieldImpl fField;
	/** The object whose field is about to be accessed/modified. */
	protected ObjectReferenceImpl fObjectReference;
	
	/**	
	 * Creates new WatchpointEventImpl.
	 */
	protected WatchpointEventImpl(String description, VirtualMachineImpl vmImpl, RequestID requestID) {
		super(description, vmImpl, requestID);
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public void readWatchpointEventFields(MirrorImpl target, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		readThreadAndLocation(target, dataInStream);
		fField = FieldImpl.readWithReferenceTypeWithTag(target, dataInStream);
		fObjectReference = ObjectReferenceImpl.readObjectRefWithTag(target, dataInStream);
   	}

   	/**
	 * Returns the field that is about to be accessed/modified.
	 */
	public Field field() {
		return fField;
	}
	
	/**
	 * Returns the object whose field is about to be accessed/modified.
	 */
	public ObjectReference object() {
		return fObjectReference;
	}
	
	/**
	 * Current value of the field.
	 */
	public Value valueCurrent() {
		// Note: if field is static, fObjectReference will be null.
		if (fObjectReference == null)
			return fField.declaringType().getValue(fField);
		return fObjectReference.getValue(fField);
	}
}
