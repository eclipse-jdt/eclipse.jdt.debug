package org.eclipse.jdi.internal.request;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.event.*;
import org.eclipse.jdi.internal.jdwp.*;
import java.io.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ExceptionRequestImpl extends EventRequestImpl implements ExceptionRequest {
	/**
	 * Creates new EventRequestManager.
	 */
	public ExceptionRequestImpl(VirtualMachineImpl vmImpl) {
		super("ExceptionRequest", vmImpl);
	}

	/**
	 * Returns exception type for which exception events are requested.
	 */
	public ReferenceType exception() {
		return fExceptionFilter;
	}
	
	/**
	 * @return Returns true if caught exceptions will be reported.
	 */
	public boolean notifyCaught() {
		return fNotifyCaught;
	}
   
	/**
	 * @return Returns true if uncaught exceptions will be reported.
	 */
	public boolean notifyUncaught() {
		return fNotifyUncaught;
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ExceptionEventImpl.EVENT_KIND;
	}
}
