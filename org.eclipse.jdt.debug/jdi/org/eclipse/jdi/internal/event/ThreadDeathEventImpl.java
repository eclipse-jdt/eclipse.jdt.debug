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

import com.sun.jdi.event.ThreadDeathEvent;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ThreadDeathEventImpl extends EventImpl implements ThreadDeathEvent {
	/** Jdwp Event Kind. */
	public static final byte EVENT_KIND = EVENT_THREAD_DEATH;

	/**
	 * Creates new ThreadDeathEventImpl.
	 */
	private ThreadDeathEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("ThreadDeathEvent", vmImpl, requestID); //$NON-NLS-1$
	}
		
	/**
	 * @return Creates, reads and returns new EventImpl, of which requestID has already been read.
	 */
	public static ThreadDeathEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		ThreadDeathEventImpl event = new ThreadDeathEventImpl(vmImpl, requestID);
		event.fThreadRef = ThreadReferenceImpl.read(target, dataInStream);
		return event;
   	}
}
