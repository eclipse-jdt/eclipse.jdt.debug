/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.eval.model.IArrayType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IType;

/**
 * @version 	1.0
 * @author
 */
public abstract class ArrayInstruction extends CompoundInstruction {

	
	public ArrayInstruction(int start) {
		super(start);
	}
	

	protected IArrayType getType(String sign, int dimension) throws CoreException {
		String qualifiedName = Signature.toString(sign);
		String braces = "";
		for (int i = 0; i < dimension; i++) {
			qualifiedName += "[]";
			braces += "[";
		}
		String signature = braces + sign;
		// Force the class to be loaded, and record the class reference
		// for later use if there are multiple classes with the same name.
		IObject classReference= classForName(signature);
		if (classReference == null) {
			throw new CoreException(null); // could not resolve type
		}
		IType[] types= getVM().classesByName(qualifiedName);
		checkTypes(types);
		if (types.length == 1) {
			// Found only one class.
			return (IArrayType)types[0];
		} else {
			// Found many classes, look for the right one for this scope.
			for(int i= 0, length= types.length; i < length; i++) {
				IType type= types[i];
				if (classReference.equals(getClassObject(type))) {
					return (IArrayType)type;
				}
			}

			// At this point a very strange thing has happened,
			// the VM was able to return multiple types in the classesByName
			// call, but none of them were the class that was returned in
			// the classForName call.

			throw new CoreException(null);
		}
	}
	
	
}
