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
import java.io.*;

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
		return fFieldFilter;
	}
}
