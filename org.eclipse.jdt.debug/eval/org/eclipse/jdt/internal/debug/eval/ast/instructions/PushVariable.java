package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IRuntimeContext;
import org.eclipse.jdt.debug.eval.model.IVariable;

 
/**
 * Pushes the value of a local, instance, or static
 * variable onto the stack.
 */
public class PushVariable extends SimpleInstruction {
	
	/**
	 * Name of variable to push.
	 */
	private String fName;
	
	public PushVariable(String name) {
		fName = name;
	}
	
	public void execute() throws CoreException {
		IRuntimeContext context= getContext();
		IVariable var = null;
		IVariable[] locals = context.getLocals();
		for (int i = 0; i < locals.length; i++) {
			if (locals[i].getName().equals(getName())) {
				push(locals[i]);
				return;
			}
		}
		IObject t = context.getThis();
		if (t != null) {
			var = t.getField(getName(), false);
			if (var != null) {
				push(var);
				return;
			}
		}
		IClassType ty = context.getReceivingType();
		var = ty.getField(getName());
		if (var != null) {
			push(var);
			return;
		}

		throw new CoreException(null);
	}
	
	/**
	 * Returns the name of the variable to push
	 * onto the stack.
	 * 
	 * @return the name of the variable to push
	 * onto the stack
	 */
	protected String getName() {
		return fName;
	}

	public String toString() {
		return "push '" + getName() + "'";
	}

}

