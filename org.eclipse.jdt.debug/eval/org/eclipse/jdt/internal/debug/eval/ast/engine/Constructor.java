package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IClassType;
import org.eclipse.jdt.debug.eval.ast.model.IValue;

/**
 * Invokes a constructor. The arguments are on the
 * stack in reverse order, followed by the type.
 * Pushes the result onto the stack
 */
public class Constructor extends CompoundInstruction {
	
	private int fArgCount;
	private String fSignature;
	
	public Constructor(String signature, int argCount, int start) {
		super(start);
		fArgCount = argCount;
		fSignature = signature;
	}
	
	public void execute() throws CoreException {
		IValue[] args = new IValue[fArgCount];
		// args are in reverse order
		for (int i= fArgCount - 1; i >= 0; i--) {
			args[i] = (IValue)popValue();
		}
		IClassType clazz = (IClassType)pop();
		IValue result = clazz.newInstance(fSignature, args, getContext().getThread());
		push(result);
	}
	
	public String toString() {
		return "constructor " + fSignature;
	}

}

