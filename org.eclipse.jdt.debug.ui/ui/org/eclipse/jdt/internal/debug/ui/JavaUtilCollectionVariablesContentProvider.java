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
import org.eclipse.jdt.debug.ui.AbstractJavaVariablesContentProvider;

/**
 * Customized content provider for classes implementing the java.util.Collection interface.
 * This content provider invokes the 'toArray()' method on all such classes and presents the
 * non-null results as the only children.
 * @since 3.0
 */
public class JavaUtilCollectionVariablesContentProvider extends AbstractJavaVariablesContentProvider {

	private static final String TO_ARRAY_METHOD_SELECTOR = "toArray";	//$NON-NLS-1$
	private static final String TO_ARRAY_METHOD_SIGNATURE = "()[Ljava/lang/Object;"; //$NON-NLS-1$
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	public IJavaVariable[] getVariableChildren(IDebugView view, IJavaValue value) throws DebugException {
		IJavaObject objectValue = getObjectValue(value);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThread(view, objectValue);		
		if (javaThread == null) {
			return null;	
		}

		IJavaValue toArrayValue = objectValue.sendMessage(TO_ARRAY_METHOD_SELECTOR,
													TO_ARRAY_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);
													
		return convertArrayToPlaceholders(toArrayValue);													
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaVariablesContentProvider#hasVariableChildren(org.eclipse.jdt.debug.core.IJavaVariable)
	 */
	public boolean hasVariableChildren(IDebugView view, IJavaValue value) throws DebugException {
		return true;
	}
	
}
