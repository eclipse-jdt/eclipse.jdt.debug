package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IArray;
import org.eclipse.jdt.debug.eval.ast.model.IValue;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A proxy to a Java debug model array object
 */
public class EvaluationArray extends EvaluationObject implements IArray {

	/**
	 * Constructs a proxy to the given array.
	 * 
	 * @param array underlying Java debug model array
	 * @return a proxy to the given array
	 */
	protected EvaluationArray(IJavaArray array) {
		super(array);
	}

	/**
	 * @see IArray#getLength()
	 */
	public int getLength() throws CoreException {
		return getJavaArray().getLength();
	}

	/**
	 * @see IArray#getValue(int)
	 */
	public IValue getValue(int index) throws CoreException {
		return EvaluationValue.createValue(getJavaArray().getValue(index));
	}

	/**
	 * @see IArray#setValue(int, IValue)
	 */
	public void setValue(int index, IValue value) throws CoreException {
		IJavaValue jv = ((EvaluationValue)value).getJavaValue();
		getJavaArray().setValue(index, jv);
	}
	
	/**
	 * Returns the underlying Java debug model array
	 * 
	 * @return the underlying Java debug model array
	 */
	protected IJavaArray getJavaArray() {
		return (IJavaArray)getJavaValue();
	}

}

