package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class JavaObjectRuntimeContext implements IRuntimeContext {
	
	/**
	 * <code>this</code> object or this context.
	 */
	private IJavaObject fThisObject;
	
	/**
	 * The project for this context.
	 */
	private IJavaProject fJavaProject;
	
	/**
	 * The thread for this context.
	 */
	private IJavaThread fThread;
	
	/**
	 * ObjectValueRuntimeContext constructor.
	 * 
	 * @param thisObject <code>this</code> object of this context.
	 * @param javaProject the project for this context.
	 * @param thread the thread for this context.
	 */
	public JavaObjectRuntimeContext(IJavaObject thisObject, IJavaProject javaProject, IJavaThread thread) {
		fThisObject= thisObject;
		fJavaProject= javaProject;
		fThread= thread;
	}

	/**
	 * @see IRuntimeContext#getVM()
	 */
	public IJavaDebugTarget getVM() {
		return (IJavaDebugTarget)fThisObject.getDebugTarget();
	}

	/**
	 * @see IRuntimeContext#getThis()
	 */
	public IJavaObject getThis() throws CoreException {
		return fThisObject;
	}

	/**
	 * @see IRuntimeContext#getReceivingType()
	 */
	public IJavaClassType getReceivingType() throws CoreException {
		return (IJavaClassType)getThis().getJavaType();
	}

	/**
	 * @see IRuntimeContext#getLocals()
	 */
	public IJavaVariable[] getLocals() throws CoreException {
		return new IJavaVariable[0];
	}

	/**
	 * @see IRuntimeContext#getProject()
	 */
	public IJavaProject getProject() {
		return fJavaProject;
	}

	/**
	 * @see IRuntimeContext#getThread()
	 */
	public IJavaThread getThread() {
		return fThread;
	}

	/**
	 * @see IRuntimeContext#isConstructor()
	 */
	public boolean isConstructor() throws CoreException {
		return false;
	}

}
