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
public abstract class LocatableEventImpl extends EventImpl implements Locatable {
	/** Location where event occurred. */
	protected LocationImpl fLocation;
	
	/**
	 * Creates new LocatableEventImpl, only used by subclasses.
	 */
	protected LocatableEventImpl(String description, VirtualMachineImpl vmImpl, RequestID requestID) {
		super(description, vmImpl, requestID);
	}
	
	/**
	 * Reads Thread and Location.
	 */
	public void readThreadAndLocation(MirrorImpl target, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		fLocation = LocationImpl.read(target, dataInStream);
   	}
	
	/**
	 * @return Returns Location where event occurred.
	 */
	public Location location() {
		return fLocation;
	}
}