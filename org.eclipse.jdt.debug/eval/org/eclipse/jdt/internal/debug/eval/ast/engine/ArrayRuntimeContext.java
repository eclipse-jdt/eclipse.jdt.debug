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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;

/**
 * Context for evaluation of an expression in the receiver context of an array.
 */
public class ArrayRuntimeContext extends AbstractRuntimeContext {

	/**
	 * Name used for temp variable referring to array (replaces 'this'). The
	 * same length as "this" so it does not affect code assist.
	 */
	public static String ARRAY_THIS_VARIABLE = "_a_t"; //$NON-NLS-1$

	private IJavaArray fArray = null;
	private IJavaReferenceType fReceivingType = null;
	private IJavaThread fThread = null;
	private IJavaVariable fLocalArray = null;

	public ArrayRuntimeContext(IJavaArray arrayObject, IJavaThread thread,
			IJavaProject project) {
		super(project);
		fArray = arrayObject;
		fThread = thread;
		fLocalArray = new JDIPlaceholderVariable(ARRAY_THIS_VARIABLE,
				arrayObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getVM()
	 */
	public IJavaDebugTarget getVM() {
		return (IJavaDebugTarget) fArray.getDebugTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getThis()
	 */
	public IJavaObject getThis() throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#
	 * getReceivingType()
	 */
	public IJavaReferenceType getReceivingType() throws CoreException {
		if (fReceivingType == null) {
			IJavaType[] javaTypes = getVM().getJavaTypes("java.lang.Object"); //$NON-NLS-1$
			if (javaTypes.length > 0) {
				fReceivingType = (IJavaReferenceType) javaTypes[0];
			} else {
				IStatus status = new Status(IStatus.ERROR,
						JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugPlugin.INTERNAL_ERROR,
						EvaluationEngineMessages.ArrayRuntimeContext_0, null);
				throw new CoreException(status);
			}
		}
		return fReceivingType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getLocals
	 * ()
	 */
	public IJavaVariable[] getLocals() throws CoreException {
		return new IJavaVariable[] { fLocalArray };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getThread
	 * ()
	 */
	public IJavaThread getThread() {
		return fThread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#isConstructor
	 * ()
	 */
	public boolean isConstructor() throws CoreException {
		return false;
	}

}
