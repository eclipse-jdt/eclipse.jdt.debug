package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
 
/**
 * Pushes the value of the static fields of the given type
 * onto the stack.
 */
public class PushStaticFieldVariable extends CompoundInstruction {
	
	private String fFieldName;
	
	private String fQualifiedTypeName;
	
	public PushStaticFieldVariable(String fieldName, String qualifiedTypeName, int start) {
		super(start);
		fFieldName= fieldName;
		fQualifiedTypeName= qualifiedTypeName;
	}
	
	public void execute() throws CoreException {
		IJavaType receiver= getType(fQualifiedTypeName);
		
		IJavaVariable field= null;

		if (receiver instanceof IJavaInterfaceType) {
			field= ((IJavaInterfaceType)receiver).getField(fFieldName);
		} else if (receiver instanceof IJavaClassType) {
			field= ((IJavaClassType)receiver).getField(fFieldName);
		}
		if (field == null) {
			throw new CoreException(new Status(Status.ERROR, DebugPlugin.PLUGIN_ID, Status.OK, "Cannot find the field " + fFieldName + " in " + fQualifiedTypeName, null)); // couldn't find the field
		}
		push(field);
	}
	
	public String toString() {
		return "push static field " + fFieldName;
	}

}

