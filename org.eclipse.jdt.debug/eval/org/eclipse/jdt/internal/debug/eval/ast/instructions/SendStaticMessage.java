package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
 
/**
 * Sends a message. The arguments are on the
 * stack in reverse order, followed by the receiver.
 * Pushes the result, if any, onto the stack
 */
public class SendStaticMessage extends CompoundInstruction {
	
	private int fArgCount;
	private String fSelector;
	private String fSignature;
	private String fTypeSignature;
	
	public SendStaticMessage(String typeSignature, String selector, String signature, int argCount, int start) {
		super(start);
		fArgCount= argCount;
		fSelector= selector;
		fSignature= signature;
		fTypeSignature= typeSignature;
	}
	
	public void execute() throws CoreException {
		IJavaValue[] args = new IJavaValue[fArgCount];
		// args are in reverse order
		for (int i= fArgCount - 1; i >= 0; i--) {
			args[i] = (IJavaValue)popValue();
		}
		
		IJavaType receiver= getType(Signature.toString(fTypeSignature));
		IJavaValue result;
		if (receiver instanceof IJavaClassType) {
			result= ((IJavaClassType)receiver).sendMessage(fSelector, fSignature, args, getContext().getThread());
		} else {
			throw new CoreException(new Status(Status.ERROR, DebugPlugin.PLUGIN_ID, Status.OK, "Cannot send a static message to a not class type object.", null));
		}
		
		if (!fSignature.endsWith(")V")) { //$NON-NLS-1$
			// only push the result if not a void method
			push(result);
		}
	}
	
	public String toString() {
		return "send static message " + fSelector + " " + fSignature;
	}

}

