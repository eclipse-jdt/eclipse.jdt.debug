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
public class ModificationWatchpointRequestImpl extends WatchpointRequestImpl implements ModificationWatchpointRequest {
	/**
	 * Creates new ModificationWatchpointRequest.
	 */
	public ModificationWatchpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("ModificationWatchpointRequest", vmImpl);
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ModificationWatchpointEventImpl.EVENT_KIND;
	}
}
