/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.jdi.internal.event.EventImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

/**
 * this class implements the corresponding interfaces declared by the OTI Hot
 * Code Replacement extentions of the JDI specification.
 */

public class ReenterStepRequestImpl extends StepRequestImpl implements
		org.eclipse.jdi.hcr.ReenterStepRequest {
	/**
	 * Creates new ReenterStepRequestImpl.
	 */
	public ReenterStepRequestImpl(VirtualMachineImpl vmImpl) {
		super("ReenterStepRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP constant for step depth.
	 */
	@Override
	public int threadStepDepthJDWP(int threadStepDepth) {
		return STEP_DEPTH_REENTER_JDWP_HCR;
	}

	/**
	 * Enables event request.
	 */
	@Override
	public void enable() {
		if (isEnabled())
			return;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(),
					"event kind", EventImpl.eventKindMap(), outData); // Always 01 for Step event. //$NON-NLS-1$
			writeByte(suspendPolicyJDWP(), "suspend policy", outData); //$NON-NLS-1$
			writeInt(modifierCount(), "modifiers", outData); //$NON-NLS-1$
			writeModifiers(outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.HCR_REENTER_ON_EXIT, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fRequestID = RequestID.read(this, replyData);
			virtualMachineImpl().eventRequestManagerImpl().addRequestIDMapping(
					this);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}
}
