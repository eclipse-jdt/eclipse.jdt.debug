package org.eclipse.jdt.internal.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.model.IClassType;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IRuntimeContext;
import org.eclipse.jdt.debug.eval.model.IThread;
import org.eclipse.jdt.debug.eval.model.IVariable;
import org.eclipse.jdt.debug.eval.model.IVirtualMachine;
import org.eclipse.jdt.internal.debug.eval.model.*;

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
	public IVirtualMachine getVM() {
		return new EvaluationVM((IJavaDebugTarget)fThisObject.getDebugTarget());
	}

	/**
	 * @see IRuntimeContext#getThis()
	 */
	public IObject getThis() throws CoreException {
		return (IObject)EvaluationObject.createValue(fThisObject);
	}

	/**
	 * @see IRuntimeContext#getReceivingType()
	 */
	public IClassType getReceivingType() throws CoreException {
		return (IClassType)getThis().getType();
	}

	/**
	 * @see IRuntimeContext#getLocals()
	 */
	public IVariable[] getLocals() throws CoreException {
		return new IVariable[0];
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
	public IThread getThread() {
		return new EvaluationThread(fThread);
	}

	/**
	 * @see IRuntimeContext#isConstructor()
	 */
	public boolean isConstructor() throws CoreException {
		return false;
	}

}
