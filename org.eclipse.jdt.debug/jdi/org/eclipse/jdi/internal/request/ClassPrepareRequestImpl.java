/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.request;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ClassPrepareEventImpl;
import org.eclipse.jdi.internal.event.EventImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.ClassPrepareRequest;

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
		super("ClassPrepareRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return ClassPrepareEventImpl.EVENT_KIND;
	}
	
	/**
	 * @see com.sun.jdi.request.ClassPrepareRequest#addSourceNameFilter(java.lang.String)
	 * @since 3.3
	 */
	public void addSourceNameFilter(String sourceNamePattern) {
		checkDisabled();
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(), "event kind", EventImpl.eventKindMap(), outData); //$NON-NLS-1$
			writeByte(suspendPolicyJDWP(), "suspend policy", outData); //$NON-NLS-1$
			writeInt(super.modifierCount(), "modifier count", outData); //$NON-NLS-1$
			super.addNewSourceNameFilter(sourceNamePattern);
			super.writeModifiers(outData);
			
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.ER_SET, outBytes);
			switch(replyPacket.errorCode()) {
				case JdwpReplyPacket.NOT_IMPLEMENTED:
					throw new UnsupportedOperationException(RequestMessages.ClassPrepareRequestImpl_does_not_support_source_name_filters);
				case JdwpReplyPacket.VM_DEAD:
					throw new VMDisconnectedException(RequestMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fRequestID = RequestID.read(this, replyData);
			virtualMachineImpl().eventRequestManagerImpl().addRequestIDMapping(this);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}
}
