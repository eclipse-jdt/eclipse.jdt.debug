package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.debug.eval.model.IVariable;
 
/**
 * Sends a message. The arguments are on the
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
		IValue[] args = new IValue[fArgCount];
		// args are in reverse order
		for (int i= fArgCount - 1; i >= 0; i--) {
			args[i] = (IValue)popValue();
		}
		Object receiver = pop();
		IValue result = null;
		
		if (receiver instanceof IVariable) {
			receiver = ((IVariable) receiver).getValue();	
		}
		
		if (receiver instanceof IObject) {
			result = ((IObject)receiver).sendMessage(fSelector, fSignature, args, fSuperSend, getContext().getThread());
		} else if (receiver instanceof IClassType) {
			result = ((IClassType)receiver).sendMessage(fSelector, fSignature, args, getContext().getThread());
		} else {
			throw new CoreException(null);
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

