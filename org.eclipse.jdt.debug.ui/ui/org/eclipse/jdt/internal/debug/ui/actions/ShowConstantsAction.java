package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.Viewer;

/**
 * Shows static final variables (constants)
 */
public class ShowConstantsAction extends VariableFilterAction {

	public ShowConstantsAction() {
		super();
	}

	/**
	 * @see VariableFilterAction#getPreferenceKey()
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
