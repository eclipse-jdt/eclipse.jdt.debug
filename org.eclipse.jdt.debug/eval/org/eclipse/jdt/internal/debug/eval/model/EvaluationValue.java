package org.eclipse.jdt.internal.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IType;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * An evaluation value - a proxy to an underlying java
 * model value
 */
public class EvaluationValue extends EvaluationElement implements IValue {
	
	/**
	 * Underlying java debug model value
	 */
	private IJavaValue fJavaValue;
	
	/**
	 * Constructs a new proxy to the given underlying java
	 * debug model value
	 * 
	 * @param value underling java debug model value
	 * @return a value to be used for evaluation
	 */
	protected EvaluationValue(IJavaValue value) {
		setJavaValue(value);
	}

	/**
	 * @see EvaluationElement#getUnderlyingModelObject()
	 */
	protected Object getUnderlyingModelObject() {
		return getJavaValue();
	}

	/**
	 * @see IValue#getType()
	 */
	public IType getType() throws CoreException {
		return EvaluationType.createType(getJavaValue().getJavaType());
	}

	/**
	 * Returns the underlying java debug model value.
	 * 
	 * @return the underlying java debug model value
	 */
	public IJavaValue getJavaValue() {
		return fJavaValue;
	}

	/**
	 * Sets the underlying java debug model value.
	 * 
	 * @param javaValue the underlying java debug model value
	 */
	private void setJavaValue(IJavaValue javaValue) {
		fJavaValue = javaValue;
	}
	
	/**
	 * Returns an equivalent evaluation value for the given
	 * java debug model value.
	 * 
	 * @param value a java debug model value
	 * @return an equivalent evaluation value for the given
	 *  java debug model value
	 */
	protected static IValue createValue(IJavaValue value) {
		if (value instanceof IJavaArray) {
			return new EvaluationArray((IJavaArray)value);
		}
		if (value instanceof IJavaObject) {
			return new EvaluationObject((IJavaObject)value);
		}
		if (value instanceof IJavaPrimitiveValue) {
			return new EvaluationPrimitiveValue((IJavaPrimitiveValue)value);
		}
		return new EvaluationValue(value);		
	}	

}

