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
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.AbstractJavaVariablesContentProvider;

/**
 * Customized content provider for classes implementing the java.util.Map$Entry interface.
 * This content provider invokes the 'getKey()' & 'getValue()' methods on all such classes and presents the
 * results as the only children.
 * @since 3.0
 */
public class JavaUtilMapEntryVariablesContentProvider extends AbstractJavaVariablesContentProvider {

	private static final String GET_KEY_METHOD_SELECTOR = "getKey"; //$NON-NLS-1$
	
	private static final String GET_VALUE_METHOD_SELECTOR = "getValue";	//$NON-NLS-1$
	private static final String GET_METHOD_SIGNATURE = "()Ljava/lang/Object;"; //$NON-NLS-1$
	
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaVariablesContentProvider#getVariableChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaVariable)
	 */
	public IJavaVariable[] getVariableChildren(IDebugView view, IJavaVariable parent) throws DebugException {
		
		IJavaObject objectValue = getObjectValue(parent);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThreadFor(view);
		if (javaThread == null) {
			return null;	
		}
		
		IJavaValue keyValue = objectValue.sendMessage(GET_KEY_METHOD_SELECTOR,
													GET_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);
				
		IJavaValue valueValue = objectValue.sendMessage(GET_VALUE_METHOD_SELECTOR,
													GET_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);
		
		IJavaVariable[] javaVars = new IJavaVariable[2];
		javaVars[0] = JDIDebugModel.createPlaceholderVariable("key", keyValue); //$NON-NLS-1$
		javaVars[1] = JDIDebugModel.createPlaceholderVariable("value", valueValue); //$NON-NLS-1$
		return javaVars;				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaVariablesContentProvider#hasVariableChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaVariable)
	 */
	public boolean hasVariableChildren(IDebugView view, IJavaVariable parent) throws DebugException {
		return true;
	}

}
