package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ExceptionEventImpl;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ExceptionRequest;

/**
 * This class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ExceptionRequestImpl extends EventRequestImpl implements ExceptionRequest {
	/**
	 * Creates new EventRequestManager.
	 */
	public ExceptionRequestImpl(VirtualMachineImpl vmImpl) {
		super("ExceptionRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * Returns exception type for which exception events are requested.
	 */
	public ReferenceType exception() {
		return ((EventRequestImpl.ExceptionFilter)fExceptionFilters.get(0)).fException;
	}
	
	/**
	 * @return Returns true if caught exceptions will be reported.
	 */
	public boolean notifyCaught() {
		return ((EventRequestImpl.ExceptionFilter)fExceptionFilters.get(0)).fNotifyCaught;
	}
   
	/**
	 * @return Returns true if uncaught exceptions will be reported.
	 */
	public boolean notifyUncaught() {
		return ((EventRequestImpl.ExceptionFilter)fExceptionFilters.get(0)).fNotifyUncaught;
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ExceptionEventImpl.EVENT_KIND;
	}
}
