package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
 
/**
 * Pops an object off the stack, and pushes the value
 * of one of its fields onto the stack.
 */
public class PushFieldVariable extends CompoundInstruction {
	
	private String fDeclaringTypeSignature;
	
	private String fName;
	
	private int  fSuperClassLevel;
	
	public PushFieldVariable(String name, int superClassLevel, int start) {
		super(start);
		fName= name;
		fSuperClassLevel= superClassLevel;
	}
	
	public PushFieldVariable(String name, String declaringTypeSignature, int start) {
		super(start);
		fName= name;
		fDeclaringTypeSignature= declaringTypeSignature;
	}
	
	public void execute() throws CoreException {
		IJavaObject receiver=(IJavaObject) popValue();
		
		IJavaVariable field= null;
		
		if (fDeclaringTypeSignature == null) {
			field= ((JDIObjectValue)receiver).getField(fName, fSuperClassLevel);
		} else {
			field= ((IJavaObject)receiver).getField(fName, fDeclaringTypeSignature);
		}
		
		if (field == null) {
			throw new CoreException(new Status(Status.ERROR, JDIDebugPlugin.getUniqueIdentifier(), Status.OK, InstructionsEvaluationMessages.getString("PushFieldVariable.Cannot_find_the_field__2") + fName + InstructionsEvaluationMessages.getString("PushFieldVariable._for_the_object__3") + receiver, null)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			push(field);
		}
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushFieldVariable.push_field__4") + fName; //$NON-NLS-1$
	}
}

