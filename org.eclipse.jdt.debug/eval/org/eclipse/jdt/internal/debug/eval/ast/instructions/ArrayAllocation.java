/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaType;

public class ArrayAllocation extends ArrayInstruction {

	private int fDimension;

	private int fExprDimension;

	private boolean fHasInitializer;

	private IJavaArrayType[] fCachedArrayTypes;

	/**
	 * Constructor for ArrayAllocation.
	 * 
	 * @param start
	 */
	public ArrayAllocation(int dimension, int exprDimension,
			boolean hasInitializer, int start) {
		super(start);
		fDimension = dimension;
		fExprDimension = exprDimension;
		fHasInitializer = hasInitializer;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		if (fHasInitializer) {
			IJavaArray array = (IJavaArray) popValue();
			pop(); // pop the type
			push(array);
		} else {

			int[] exprDimensions = new int[fExprDimension];

			for (int i = fExprDimension - 1; i >= 0; i--) {
				exprDimensions[i] = ((IJavaPrimitiveValue) popValue())
						.getIntValue();
			}

			IJavaType type = (IJavaType) pop();

			fCachedArrayTypes = new IJavaArrayType[fDimension + 1];

			for (int i = fDimension, lim = fDimension - fExprDimension; i > lim; i--) {
				fCachedArrayTypes[i] = (IJavaArrayType) type;
				type = ((IJavaArrayType) type).getComponentType();
			}

			IJavaArray array = createArray(fDimension, exprDimensions);

			push(array);
		}
	}

	/**
	 * Create and populate an array.
	 */
	private IJavaArray createArray(int dimension, int[] exprDimensions)
			throws CoreException {

		IJavaArray array = fCachedArrayTypes[dimension]
				.newInstance(exprDimensions[0]);

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

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.ArrayAllocation_array_allocation_1;
	}

}
