package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
 
/**
 * Sends an message to an instance. The arguments are on the
 * stack in reverse order, followed by the receiver.
 * Pushes the result, if any, onto the stack
 */
public class SendMessage extends CompoundInstruction {
	
	private int fArgCount;
	private String fSelector;
	private String fSignature;
	private boolean fSuperSend;
	
	public SendMessage(String selector, String signature, int argCount, boolean superSend, int start) {
		super(start);
		fArgCount= argCount;
		fSelector= selector;
		fSignature= signature;
		fSuperSend= superSend;
	}
	
	public void execute() throws CoreException {
		IJavaValue[] args = new IJavaValue[fArgCount];
		// args are in reverse order
		for (int i= fArgCount - 1; i >= 0; i--) {
			args[i] = (IJavaValue)popValue();
		}
		Object receiver = pop();
		IJavaValue result = null;
		
		if (receiver instanceof IJavaVariable) {
			receiver = ((IJavaVariable) receiver).getValue();	
		}
		
		if (receiver instanceof IJavaObject) {
			result = ((IJavaObject)receiver).sendMessage(fSelector, fSignature, args, getContext().getThread(), fSuperSend);
		} else {
			throw new CoreException(new Status(Status.ERROR, DebugPlugin.PLUGIN_ID, Status.OK, "Try to send an message to an not object value", null));
		}
		if (!fSignature.endsWith(")V")) {
			// only push the result if not a void method
			push(result);
		}
	}
	
	public String toString() {
		return "send message " + fSelector + " " + fSignature;
	}

}

