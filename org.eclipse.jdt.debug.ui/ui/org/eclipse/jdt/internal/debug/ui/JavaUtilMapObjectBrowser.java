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
import org.eclipse.jdt.debug.ui.AbstractJavaObjectBrowser;

/**
 * Customized object browser for classes implementing the java.util.Map interface.
 * This object browser invokes the 'entrySet()' method on all such classes and presents the
 * non-null results as the only children.
 * @since 3.0
 */
public class JavaUtilMapObjectBrowser extends AbstractJavaObjectBrowser {

	private static final String ENTRY_SET_METHOD_SELECTOR = "entrySet"; //$NON-NLS-1$
	private static final String ENTRY_SET_METHOD_SIGNATURE = "()Ljava/util/Set;"; //$NON-NLS-1$
	
	private static final String TO_ARRAY_METHOD_SELECTOR = "toArray"; //$NON-NLS-1$
	private static final String TO_ARRAY_METHOD_SIGNATURE = "()[Ljava/lang/Object;"; //$NON-NLS-1$
	
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#getChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public IJavaVariable[] getChildren(IDebugView view, IJavaValue value) throws DebugException {
		
		IJavaObject objectValue = getObjectValue(value);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThread(view, objectValue);
		if (javaThread == null) {
			return null;	
		}
		
		// Get the Set of entries from the Map
		IJavaObject entrySetValue = (IJavaObject)objectValue.sendMessage(ENTRY_SET_METHOD_SELECTOR,
													ENTRY_SET_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);
				
		// Get the entries in the Set as an array of Objects									
		IJavaValue toArrayValue = entrySetValue.sendMessage(TO_ARRAY_METHOD_SELECTOR,
													TO_ARRAY_METHOD_SIGNATURE,
													EMPTY_VALUE_ARRAY,
													javaThread,
													false);

		return convertArrayToPlaceholders(toArrayValue);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#hasChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public boolean hasChildren(IDebugView view, IJavaValue value) throws DebugException {
		return true;
	}

}
