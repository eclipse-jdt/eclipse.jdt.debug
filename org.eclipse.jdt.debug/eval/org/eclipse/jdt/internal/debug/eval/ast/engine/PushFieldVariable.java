package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IArray;
import org.eclipse.jdt.debug.eval.ast.model.IClassType;
import org.eclipse.jdt.debug.eval.ast.model.IInterfaceType;
import org.eclipse.jdt.debug.eval.ast.model.IObject;
import org.eclipse.jdt.debug.eval.ast.model.IVariable;
 
/**
 * Pops an object off the stack, and pushes the value
 * of one of its fields onto the stack.
 */
public class PushFieldVariable extends CompoundInstruction {
	
	private String fName;
	
	private String fTypeSignature;
	
	private boolean fSuperField;
	
	public static final String LENGTH= "length";
	
	public PushFieldVariable(String name, boolean superField, int start) {
		super(start);
		fName= name;
		fSuperField= superField;
	}
	
	public PushFieldVariable(String name, String typeSignature, int start) {
		super(start);
		fName= name;
		fTypeSignature= typeSignature;
	}
	
	public void execute() throws CoreException {
		Object receiver= pop();
		
		IVariable field= null;
		
		if (receiver instanceof IVariable) {
			receiver = ((IVariable) receiver).getValue();
		}
		
		if (receiver instanceof IArray && LENGTH.equals(fName)) {
			int length= ((IArray)receiver).getLength();
			pushNewValue(length);
			return;
		} else if (receiver instanceof IObject) {
			if (fTypeSignature == null) {
				field= ((IObject)receiver).getField(fName, fSuperField);
			} else {
				field= ((IObject)receiver).getField(fName, fTypeSignature);
			}
		} else if (receiver instanceof IInterfaceType) {
			field= ((IInterfaceType)receiver).getField(fName);
		} else if (receiver instanceof IClassType) {
			field= ((IClassType)receiver).getField(fName);
		}
		if (field == null) {
			throw new CoreException(null); // couldn't find the field
		} else {
			push(field);
		}
	}
	
	public String toString() {
		return "push field " + fName;
	}

}

