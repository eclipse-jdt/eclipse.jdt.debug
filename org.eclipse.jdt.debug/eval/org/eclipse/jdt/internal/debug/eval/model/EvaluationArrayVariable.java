/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IArray;
import org.eclipse.jdt.debug.eval.model.IArrayType;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.debug.eval.model.IVariable;

/**
 * @version 	1.0
 * @author
 */
public class EvaluationArrayVariable implements IVariable {

	private IArray fArray;
	
	private int fIndex;
	
	public EvaluationArrayVariable(IArray array, int index) {
		fArray = array;
		fIndex = index;
	}

	/*
	 * @see IVariable#getName()
	 */
	public String getName() throws CoreException {
		return fArray.getType().getName() + "[" + fIndex + "]";
	}

	/*
	 * @see IVariable#getType()
	 */
	public IType getType() throws CoreException {
		return ((IArrayType)fArray.getType()).getComponentType();
	}

	/*
	 * @see IVariable#getValue()
	 */
	public IValue getValue() throws CoreException {
		return fArray.getValue(fIndex);
	}

	/*
	 * @see IVariable#setValue(IValue)
	 */
	public void setValue(IValue value) throws CoreException {
		fArray.setValue(fIndex, value);
	}

}
