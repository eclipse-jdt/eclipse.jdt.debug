/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * Base class for Java object browsers.  This class provices several useful classes
 * that reduce the work of creating Java object browsers.
 * 
 * @since 3.0
 */
public abstract class AbstractJavaObjectBrowser implements IJavaObjectBrowser {

	/**
	 * Return the instance of <code>IJavaObject</code> corresponding to the specified
	 * variable, or <code>null</code> if there is none.
	 * 
	 * @param variable the java variable in which to look for the <code>IJavaObject</code>
	 * @return an instance of <code>IJavaObject</code>, or <code>null</code>
	 * @throws DebugException
	 */
	protected IJavaObject getObjectValue(IJavaValue value) {
		if (!(value instanceof IJavaObject)) {
			return null;
		}
		return (IJavaObject) value;		
	}
	
	/**
	 * Return the java thread corresponding to the specified java value.
	 * 
	 * @param javaValue the java value 
	 * @return the java thread corresponding to the specified java value
	 */
	protected IJavaThread getJavaThread(IJavaValue javaValue) {
		if (javaValue == null) {
			return null;
		}
		IDebugTarget debugTarget = javaValue.getDebugTarget();
		if (!(debugTarget instanceof IJavaDebugTarget)) {
			return null;
		}
		return JDIModelPresentation.getEvaluationThread((IJavaDebugTarget)debugTarget);
	}
	
	/**
	 * Create and return an array of <code>IJavaVariable</code>s that corresponds to 
	 * the variable children of the specified java value.
	 */
	protected IJavaVariable[] convertArrayToPlaceholders(IJavaValue arrayValue) throws DebugException {
		IVariable[] vars =  arrayValue.getVariables();
		IJavaVariable[] javaVars = new IJavaVariable[vars.length];
		for (int i = 0; i < vars.length; i++) {
			IVariable var = vars[i];
			javaVars[i] = JDIDebugModel.createPlaceholderVariable(var.getName(), (IJavaValue)var.getValue());
		}
		return javaVars;															
	}
}
