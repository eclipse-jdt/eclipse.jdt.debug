package org.eclipse.jdt.internal.debug.eval.model;


/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.debug.eval.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
 
/**
 * An evaluation variable - a proxy to a java variable
 */
public class EvaluationVariable extends EvaluationElement implements IVariable {
	
	/**
	 * Underlying variable
	 */
	private IJavaVariable fVariable;
	
	/**
	 * Constructs a new evaluation variable on the given
	 * Java variable
	 * 
	 * @param variable the underlying java debug model variable
	 * @return a variable to be used for evaluation
	 */
	public EvaluationVariable(IJavaVariable variable) {
		setJavaVariable(variable);
	}
	

	/**
	 * @see IVariable#getType()
	 */
	public IType getType() throws CoreException {
		return new EvaluationType(getJavaVariable().getJavaType());
	}

	/**
	 * @see IVariable#getValue()
	 */
	public IValue getValue() throws CoreException {
		return EvaluationValue.createValue((IJavaValue)getJavaVariable().getValue());
	}

	/**
	 * @see IVariable#setValue(IValue)
	 */
	public void setValue(IValue value) throws CoreException {
		getJavaVariable().setValue(((EvaluationValue)value).getJavaValue());
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws CoreException {
		return getJavaVariable().getName();
	}

	/**
	 * Sets the variable that this evaluation variable
	 * is a proxy to.
	 * 
	 * @param variable the variable that this evaluation variable
	 *  is a proxy to
	 */
	private void setJavaVariable(IJavaVariable variable) {
		fVariable = variable;
	}
	
	/**
	 * Returns the variable that this evaluation variable
	 * is a proxy to.
	 * 
	 * @param variable the variable that this evaluation variable
	 *  is a proxy to
	 */
	private IJavaVariable getJavaVariable() {
		return fVariable;
	}
		
	/**
	 * @see EvaluationElement#getUnderlyingModelObject()
	 */
	protected Object getUnderlyingModelObject() {
		return getJavaVariable();
	}

}

