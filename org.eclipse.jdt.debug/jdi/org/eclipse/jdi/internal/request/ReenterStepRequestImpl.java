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
 * declared by the OTI Hot Code Replacement extentions of the
 * JDI specification.
 */

public class ReenterStepRequestImpl extends StepRequestImpl implements org.eclipse.jdi.hcr.ReenterStepRequest {
	/**
	 * Creates new ReenterStepRequestImpl.
	 */
	public ReenterStepRequestImpl(VirtualMachineImpl vmImpl) {
		super("ReenterStepRequest", vmImpl);
	}
	
	/**
	 * @return Returns JDWP constant for step depth.
	 */
	public int threadStepDepthJDWP() {
		return STEP_DEPTH_REENTER_JDWP_HCR;
	}

	/**
	 * Enables event request.
	 */
	public void enable() {
		if (isEnabled())
			return;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(), "event kind", EventImpl.eventKindMap(), outData);	// Always 01 for Step event.
			writeByte(suspendPolicyJDWP(), "suspend policy", outData);
			writeInt(modifierCount(), "modifiers", outData);
			writeModifiers(outData);
			
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.HCR_REENTER_ON_EXIT, outBytes);
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