package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
public class ClassPrepareRequestImpl extends EventRequestImpl implements ClassPrepareRequest {
	/**
	 * Creates new ClassPrepareRequest.
	 */
	public ClassPrepareRequestImpl(VirtualMachineImpl vmImpl) {
		super("ClassPrepareRequest", vmImpl);
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ClassPrepareEventImpl.EVENT_KIND;
	}
}
