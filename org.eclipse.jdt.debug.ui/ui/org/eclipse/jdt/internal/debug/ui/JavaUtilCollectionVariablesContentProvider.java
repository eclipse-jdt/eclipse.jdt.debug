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
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.ui.AbstractJavaVariablesContentProvider;

/**
 * Customized content provider for classes implementing the java.util.Collection interface.
 * This content provider invokes the 'toArray()' method on all such classes and presents the
 * non-null results as the only children.
 */
public class JavaUtilCollectionVariablesContentProvider extends AbstractJavaVariablesContentProvider {

	private static final String TO_ARRAY_METHOD_SELECTOR = "toArray";	
	private static final String TO_ARRAY_METHOD_SIGNATURE = "()[Ljava/lang/Object;";
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	public IJavaVariable[] getVariableChildren(IDebugView view, IJavaVariable parent) throws DebugException {
		IJavaObject objectValue = getObjectValue(parent);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThreadFor(view);		
		if (javaThread == null) {
			return null;	
		}

		IJavaValue toArrayValue = objectValue.sendMessage(TO_ARRAY_METHOD_SELECTOR,
													TO_ARRAY_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);
													
		IVariable[] vars =  toArrayValue.getVariables();
		IJavaVariable[] javaVars = new IJavaVariable[vars.length];
		for (int i = 0; i < vars.length; i++) {
			javaVars[i] = (IJavaVariable) vars[i];
		}
		return javaVars;															
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaVariablesContentProvider#hasVariableChildren(org.eclipse.jdt.debug.core.IJavaVariable)
	 */
	public boolean hasVariableChildren(IDebugView view, IJavaVariable parent) throws DebugException {
		return true;
	}
	
}
