package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ReferenceType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IInterfaceType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IPrimitiveType;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.internal.debug.eval.model.*;
import org.eclipse.jdt.internal.debug.eval.model.EvaluationPrimitiveType;
 
/**
 * Pushes a type onto the stack.
 */
public class PushType extends SimpleInstruction {
	
	private String fTypeName;
	private boolean fIsBaseType;
	
	
	public PushType(String signature, boolean isBaseType) {
		fTypeName= signature;
		fIsBaseType= isBaseType;
	}
	
	public void execute() throws CoreException {
		if (fIsBaseType) {
			push(EvaluationPrimitiveType.getType(fTypeName));
		} else {
			push(getType(fTypeName));
		}
	}
	
	public String toString() {
		return "push type " + fTypeName;
	}

	
	
}

