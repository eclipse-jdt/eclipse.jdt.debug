package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.eval.ast.model.IType;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A type used for evaluation - a proxy to a java
 * type in the java debug model.
 */
public class EvaluationType extends EvaluationElement implements IType {
	
	/**
	 * The underlying java debug model type
	 */
	private IJavaType fType;
	
	/**
	 * Constructs a type to be used for an evaluation
	 * based on the given underlying java debug model
	 * type
	 * 
	 * @param type java debug model type
	 * @return a type to use for evalaution
	 */
	public EvaluationType(IJavaType type) {
		setJavaType(type);
	}

	/**
	 * @see IType#getSignature()
	 */
	public String getSignature() throws CoreException {
		return getJavaType().getSignature();
	}

	/**
	 * @see IType#getName()
	 */
	public String getName() throws CoreException {
		return getJavaType().getName();
	}

	/**
	 * Sets the type that this evaluation type
	 * is a proxy to.
	 * 
	 * @param type the type that this evaluation type
	 *  is a proxy to
	 */
	private void setJavaType(IJavaType type) {
		fType = type;
	}
	
	/**
	 * Returns the type that this evaluation type
	 * is a proxy to.
	 * 
	 * @param type the type that this evaluation type
	 *  is a proxy to
	 */
	protected IJavaType getJavaType() {
		return fType;
	}	
	
	/**
	 * @see EvaluationElement#getUnderlyingModelObject()
	 */
	protected Object getUnderlyingModelObject() {
		return getJavaType();
	}
	
	/**
	 * Returns an equivalent evaluation type for the given
	 * java debug model type.
	 * 
	 * @param type a java debug model type
	 * @return an equivalent evaluation type for the given
	 *  java debug model type
	 */
	protected static IType createType(IJavaType type) {
		if (type instanceof IJavaClassType) {
			return new EvaluationClassType((IJavaClassType)type);
		}
		if (type instanceof IJavaInterfaceType) {
			return new EvaluationInterfaceType((IJavaInterfaceType)type);
		}
		if (type instanceof IJavaArrayType) {
			return new EvaluationArrayType((IJavaArrayType)type);
		}
		return new EvaluationType(type);		
	}

}

