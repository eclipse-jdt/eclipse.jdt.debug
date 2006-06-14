/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.event;

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ValueImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.Method;
import com.sun.jdi.Value;
import com.sun.jdi.event.MethodExitEvent;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 * 
 * @since 3.3
 */
public class MethodExitWithReturnValueEventImpl extends LocatableEventImpl implements MethodExitEvent {

	/** even id **/
	public static final byte EVENT_KIND = EVENT_METHOD_EXIT_WITH_RETURN_VALUE;
	
	/** return value for the method **/
	private Value fReturnValue = null;
	
	/**
	 * Creates new MethodExitEventImpl.
	 */
	private MethodExitWithReturnValueEventImpl(VirtualMachineImpl vmImpl, RequestID requestID) {
		super("MethodExitWithReturnValueEvent", vmImpl, requestID); //$NON-NLS-1$
	} 
	
	/**
	 * @return Creates, reads and returns new EventImpl with the method return value, of which requestID has already been read.
	 */
	public static MethodExitWithReturnValueEventImpl read(MirrorImpl target, RequestID requestID, DataInputStream dataInStream) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		MethodExitWithReturnValueEventImpl event = new MethodExitWithReturnValueEventImpl(vmImpl, requestID);
		event.readThreadAndLocation(target, dataInStream);
		event.fReturnValue = ValueImpl.readWithTag(target, dataInStream);
		return event;
	}
	
	/**
	 * @see com.sun.jdi.event.MethodExitEvent#method()
	 */
	public Method method() {
		return fLocation.method();
	}

	/**
	 * @see com.sun.jdi.event.MethodExitEvent#returnValue()
	 */
	public Value returnValue() {
		return fReturnValue;
	}
}
