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
public class ThreadStartRequestImpl extends EventRequestImpl implements ThreadStartRequest {
	/**
	 * Creates new ThreadStartRequest.
	 */
	public ThreadStartRequestImpl(VirtualMachineImpl vmImpl) {
		super("ThreadStartRequest", vmImpl);
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ThreadStartEventImpl.EVENT_KIND;
	}
}
