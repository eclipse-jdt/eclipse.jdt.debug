package org.eclipse.jdi.internal.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.WatchpointEvent;

/**
 * This class implements the corresponding interfaces
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
