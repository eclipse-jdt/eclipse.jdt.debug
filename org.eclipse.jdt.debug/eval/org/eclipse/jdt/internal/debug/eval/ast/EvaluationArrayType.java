package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IArray;
import org.eclipse.jdt.debug.eval.ast.model.IArrayType;
import org.eclipse.jdt.debug.eval.ast.model.IType;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * The type of an array - a proxy to a Java debug model
 * array type
 */
public class EvaluationArrayType extends EvaluationType implements IArrayType {

	/**
	 * Constructs a proxy to the given Java debug model array
	 * type.
	 * 
	 * @param type Java debug model array type
	 * @return a proxy to the Java debug model array type
	 */
	public EvaluationArrayType(IJavaArrayType type) {
		super(type);
	}

	/**
	 * Returns the underlying Java debug model array type
	 * 
	 * @return the underlying Java debug model array type
	 */
	protected IJavaArrayType getJavaArrayType() {
		return (IJavaArrayType)getJavaType();
	}
	
	/**
	 * @see IArrayType#getComponentType()
	 */
	public IType getComponentType() throws CoreException {
		IJavaType jt = getJavaArrayType().getComponentType();
		return EvaluationType.createType(jt);
	}

	/**
	 * @see IArrayType#newArray(int)
	 */
	public IArray newArray(int length) throws CoreException {
		IJavaArray ja = getJavaArrayType().newInstance(length);
		return (IArray)EvaluationValue.createValue(ja);
	}

}

