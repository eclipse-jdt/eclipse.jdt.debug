/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;

public abstract class ArrayInstruction extends CompoundInstruction {

	
	public ArrayInstruction(int start) {
		super(start);
	}
	

	protected IJavaArrayType getType(String sign, int dimension) throws CoreException {
		String qualifiedName = Signature.toString(sign);
		String braces = ""; //$NON-NLS-1$
		for (int i = 0; i < dimension; i++) {
			qualifiedName += "[]"; //$NON-NLS-1$
			braces += "["; //$NON-NLS-1$
		}
		String signature = braces + sign;
		// Force the class to be loaded, and record the class reference
		// for later use if there are multiple classes with the same name.
		IJavaObject classReference= classForName(signature);
		if (classReference == null) {
			throw new CoreException(null); // could not resolve type
		}
		IJavaType[] types= getVM().getJavaTypes(qualifiedName);
		checkTypes(types);
		if (types.length == 1) {
			// Found only one class.
			return (IJavaArrayType)types[0];
		} else {
			// Found many classes, look for the right one for this scope.
			for(int i= 0, length= types.length; i < length; i++) {
				IJavaType type= types[i];
				if (classReference.equals(getClassObject(type))) {
					return (IJavaArrayType)type;
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
