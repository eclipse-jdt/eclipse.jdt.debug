package org.eclipse.jdi.internal.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.event.ThreadStartEvent;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ThreadStartEventImpl extends EventImpl implements ThreadStartEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_THREAD_START;

	/**
	 * Creates new ThreadDeathEventImpl.
	 */
	private ThreadStartEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ThreadStartEvent", vmImpl, requestID); //$NON-NLS-1$
	}
		
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static ThreadStartEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ThreadStartEventImpl event = new ThreadStartEventImpl(vmImpl, requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		return event;
   	}
}
