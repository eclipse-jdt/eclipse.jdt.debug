/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IArray;
import org.eclipse.jdt.debug.eval.ast.model.IArrayType;
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.ast.model.IType;

/**
 * @version 	1.0
 * @author
 */
public class ArrayAllocation extends ArrayInstruction {

	private int fDimension;
	
	private int fExprDimension;
	
	private boolean fHasInitializer;
	
	private IArrayType[] fCachedArrayTypes;

	/**
	 * Constructor for ArrayAllocation.
	 * @param start
	 */
	public ArrayAllocation(int dimension, int exprDimension, boolean hasInitializer, int start) {
		super(start);
		fDimension = dimension;
		fExprDimension = exprDimension;
		fHasInitializer = hasInitializer;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		if (fHasInitializer) {
			IArray array = (IArray) popValue();
			pop(); // pop the type
			push(array);
		} else {
			
			int[] exprDimensions = new int[fExprDimension];
			
			for (int i = fExprDimension - 1; i >= 0; i--) {
				exprDimensions[i] = ((IPrimitiveValue)popValue()).getIntValue();
			}
			
			IType type = (IType) pop();
			
			fCachedArrayTypes = new IArrayType[fDimension + 1];
			
			for (int i =fDimension, lim = fDimension - fExprDimension ; i > lim; i--) {
				fCachedArrayTypes[i] = (IArrayType) type;
				type = ((IArrayType)type).getComponentType();
			}
			
			IArray array = createArray(fDimension, exprDimensions);
			
			push(array);
		}
	}
	
	/**
	 * Create and populate an array.
	 */
	private IArray createArray(int dimension, int[] exprDimensions) throws CoreException {
		
		IArray array = fCachedArrayTypes[dimension].newArray(exprDimensions[0]);
		
		if (exprDimensions.length > 1) {
			int[] newExprDimension = new int[exprDimensions.length - 1];
			for (int i = 0; i < newExprDimension.length; i++) {
				newExprDimension[i] = exprDimensions[i + 1];
			}
			
			for (int i = 0; i < exprDimensions[0]; i++) {
				array.setValue(i, createArray(dimension - 1, newExprDimension));
			}
			
		}
		
		return array;
	}		

	public String toString() {
		return "array allocation";
	}

}
