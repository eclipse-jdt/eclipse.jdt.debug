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
public class ClassUnloadEventImpl extends EventImpl implements ClassUnloadEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_CLASS_UNLOAD;

	/** Type signature. */
	private String fSignature;

	/**
	 * Creates new ClassUnloadEventImpl.
	 */
	private ClassUnloadEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ClassUnloadEvent", vmImpl, requestID);
	}

	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static ClassUnloadEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ClassUnloadEventImpl event = new ClassUnloadEventImpl(vmImpl, requestID);
		event.fSignature = target.readString("signature", dataInStream);
		// Remove the class from classes that are known by the application to be loaded in the VM.
		vmImpl.removeKnownRefType(event.fSignature);
		return event;
   	}

	/**
	 * @return Returns the name of the class that has been unloaded.
  	 */
	public String className() {
		return ReferenceTypeImpl.signatureToName(fSignature);
	}
	
	/**
	 * @return Returns the JNI-style signature of the class that has been unloaded.
	 */
	public String classSignature() {
		return fSignature;
	}
}
