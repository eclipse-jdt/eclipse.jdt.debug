package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IInterfaceType;
import org.eclipse.jdt.debug.eval.ast.model.IObject;
import org.eclipse.jdt.debug.eval.ast.model.IThread;
import org.eclipse.jdt.debug.eval.ast.model.IValue;
import org.eclipse.jdt.debug.eval.ast.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * The type of an object - provides access to static methods and fields.
 */
public class EvaluationInterfaceType extends EvaluationType implements IInterfaceType  {

	/**
	 * Cosntructs a new type that represents a the given
	 * class.
	 */
	public EvaluationInterfaceType(IJavaInterfaceType type) {
		super(type);
	}
	

	/**
	 * @see IClassType#getField(String)
	 */
	public IVariable getField(String name) throws CoreException {
		IJavaVariable jv = getJavaInterfaceType().getField(name);
		if (jv != null) {
			return new EvaluationVariable(jv);
		}
		return null;
	}

	/**
	 * @see IClassType#getName()
	 */
	public String getName() throws CoreException {
		return getJavaInterfaceType().getName();
	}
	
	/**
	 * Returns the underlying java debug model class type that
	 * this a proxy to.
	 * 
	 * @return the underlying java debug model class type that
	 *  this a proxy to
	 */
	protected IJavaInterfaceType getJavaInterfaceType() {
		return (IJavaInterfaceType)getJavaType();
	}

	public IObject getClassObject() throws CoreException {
		IJavaObject javaObject = getJavaInterfaceType().getClassObject();
		if (javaObject != null) {
			return new EvaluationObject(javaObject);
		}
		return null;
	}
}

