/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IArray;
import org.eclipse.jdt.debug.eval.model.IArrayType;

/**
 * @version 	1.0
 * @author
 */
public class ArrayInitializerInstruction extends ArrayInstruction {

	private String fTypeSignature;
	
	private int fDimensions;
	
	private int fLength;

	/**
	 * Constructor for ArrayInitializerInstruction.
	 * @param start
	 */
	public ArrayInitializerInstruction(String typeSignature, int length, int dimensions, int start) {
		super(start);
		fTypeSignature = typeSignature;
		fDimensions = dimensions;
		fLength = length;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		
		IArrayType arrayType = getType(fTypeSignature.replace('/','.'), fDimensions);
		IArray array = arrayType.newArray(fLength);
		
		for (int i = fLength - 1; i >= 0; i--) {
			array.setValue(i, popValue());
		}
		
		push(array);
		
	}

	public String toString() {
		return "array initializer";
	}

}
