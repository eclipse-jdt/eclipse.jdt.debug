package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;

import com.sun.jdi.Field;
import com.sun.jdi.request.WatchpointRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class WatchpointRequestImpl extends EventRequestImpl implements WatchpointRequest {
	/**
	 * Creates new WatchpointRequest, only used by subclasses.
	 */
	public WatchpointRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}
	
	/**
	 * @return Returns field for which Watchpoint requests is issued.
	 */
	public Field field() {
		return (Field)fFieldFilters.get(0);
	}
}
