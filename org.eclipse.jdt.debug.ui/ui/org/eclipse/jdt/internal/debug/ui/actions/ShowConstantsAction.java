/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.Viewer;

/**
 * Shows static final variables (constants)
 */
public class ShowConstantsAction extends ViewFilterAction {

	public ShowConstantsAction() {
		super();
	}

	/**
	 * @see ViewFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_SHOW_CONSTANTS; 
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IJavaVariable) {
			IJavaVariable variable = (IJavaVariable)element;
			try {
				if (!getValue()) {
					// when not on, filter static finals
					return !(variable.isStatic() && variable.isFinal());
				}				
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			} 
		}
		return true;
	}	
}
