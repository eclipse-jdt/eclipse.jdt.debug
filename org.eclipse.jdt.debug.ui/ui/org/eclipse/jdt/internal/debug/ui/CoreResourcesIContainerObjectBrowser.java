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
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.ui.AbstractJavaObjectBrowser;

/**
 * Customized object browser for classes implementing org.eclipse.core.resources.IContainer.
 * This object browser presents IResource children as the only child variables of such classes.
 * 
 * @since 3.0
 */
public class CoreResourcesIContainerObjectBrowser extends AbstractJavaObjectBrowser {

	private static final String MEMBERS_METHOD_SELECTOR = "members";	//$NON-NLS-1$
	private static final String MEMBERS_METHOD_SIGNATURE = "()[Lorg/eclipse/core/resources/IResource;"; //$NON-NLS-1$
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#getChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public IJavaVariable[] getChildren(IJavaValue value) throws DebugException {
		IJavaObject objectValue = getObjectValue(value);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThread(value);		
		if (javaThread == null) {
			return null;	
		}

		IJavaValue membersArrayValue = objectValue.sendMessage(MEMBERS_METHOD_SELECTOR,
									MEMBERS_METHOD_SIGNATURE,
									EMPTY_VALUE_ARRAY,
									javaThread,
									false);

		return convertArrayToPlaceholders(membersArrayValue);													
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#hasChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public boolean hasChildren(IJavaValue value) {
		return true;
	}
}