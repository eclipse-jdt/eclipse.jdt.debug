package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.debug.eval.model.IClassType;
import org.eclipse.jdt.internal.debug.eval.model.IObject;
import org.eclipse.jdt.internal.debug.eval.model.IValue;
import org.eclipse.jdt.internal.debug.eval.model.IVariable;
 
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
		IValue[] args = new IValue[fArgCount];
		// args are in reverse order
		for (int i= fArgCount - 1; i >= 0; i--) {
			args[i] = (IValue)popValue();
		}
		
		IClassType receiver= (IClassType)getType(Signature.toString(fTypeSignature));
		
		IValue result= receiver.sendMessage(fSelector, fSignature, args, getContext().getThread());
		
		if (!fSignature.endsWith(")V")) {
			// only push the result if not a void method
			push(result);
		}
	}
	
	public String toString() {
		return "send static message " + fSelector + " " + fSignature;
	}

}

