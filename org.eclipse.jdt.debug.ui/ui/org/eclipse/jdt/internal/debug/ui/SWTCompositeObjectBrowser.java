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
import org.eclipse.jdt.debug.ui.AbstractJavaObjectBrowser;

/**
 * Customized object browser for classes extending org.eclipse.swt.widgets.Composite.
 * This object browser presents the layout and Control children as the only child
 * variables of such classes.
 * 
 * @since 3.0
 */
public class SWTCompositeObjectBrowser extends AbstractJavaObjectBrowser {

	private static final String GET_CHILDREN_METHOD_SELECTOR = "getChildren";	//$NON-NLS-1$
	private static final String GET_CHILDREN_METHOD_SIGNATURE = "()[Lorg/eclipse/swt/widgets/Control;"; //$NON-NLS-1$
	private static final String GET_LAYOUT_METHOD_SELECTOR = "getLayout";	//$NON-NLS-1$
	private static final String GET_LAYOUT_METHOD_SIGNATURE = "()Lorg/eclipse/swt/widgets/Layout;"; //$NON-NLS-1$
	private static final IJavaValue[] EMPTY_VALUE_ARRAY = new IJavaValue[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#getChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public IJavaVariable[] getChildren(IDebugView view, IJavaValue value) throws DebugException {
		IJavaObject objectValue = getObjectValue(value);
		if (objectValue == null) {
			return null;
		}
		IJavaThread javaThread = getJavaThread(view, value);		
		if (javaThread == null) {
			return null;	
		}

		IJavaValue layoutValue = objectValue.sendMessage(GET_LAYOUT_METHOD_SELECTOR,
									GET_LAYOUT_METHOD_SIGNATURE,
									EMPTY_VALUE_ARRAY,
									javaThread,
									false);

		IJavaValue childrenArrayValue = objectValue.sendMessage(GET_CHILDREN_METHOD_SELECTOR,
											GET_CHILDREN_METHOD_SIGNATURE,
											EMPTY_VALUE_ARRAY,
											javaThread,
											false);													
	 	IJavaVariable[] childrenPlaceholders = convertArrayToPlaceholders(childrenArrayValue);													
	 	
	 	int childrenLength = childrenPlaceholders.length;
	 	IJavaVariable[] placeholders = new IJavaVariable[childrenLength + 1];
	 	placeholders[0] = JDIDebugModel.createPlaceholderVariable("layout", layoutValue); //$NON-NLS-1$
	 	System.arraycopy(childrenPlaceholders, 0, placeholders, 1, childrenLength);
		return placeholders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.IJavaObjectBrowser#hasChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.jdt.debug.core.IJavaValue)
	 */
	public boolean hasChildren(IDebugView view, IJavaValue value) throws DebugException {
		// It's safe to always return true here, since a Composite will always have a
		// layout, even if it's null
		return true;
	}

}
