package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter for viewers that display Java variables.  Filters out variables
 * according to the current preference settings.
 */
public class JavaVariablesViewerFilter extends ViewerFilter {

	/**
	 * Flag indicating whether the current preferences have been loaded.
	 */															
	private boolean fFilterPreferencesLoaded = false;

	/**
	 * A table containing the current preferences that relate to variable
	 * filtering.
	 */
	private boolean[][] fFilterGrid = new boolean[JDIDebugUIPlugin.fgModeModifierNames.length][JDIDebugUIPlugin.fgAccessModifierNames.length];																									

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IAdaptable) {
			IJavaVariable var = (IJavaVariable) ((IAdaptable) element).getAdapter(IJavaVariable.class);
			if (var != null) {

				// Never filter out the root of the viewer
				if (element.equals(viewer.getInput())) {
					return true;
				}
				
				// Compare the java modifiers on the variable to the current preferences
				return isVariableVisible(var);
			}
		}
		return true;
	}
	
	public void resetState() {
		fFilterPreferencesLoaded = false;
	}

	private boolean isVariableVisible(IJavaVariable var) {
		
		// Load the preferences if necessary
		if (!fFilterPreferencesLoaded) {
			loadFilterPreferences();
		}
		
		// Determine the column index in the table corresponding to the varaible
		int index = -1;		
		try {
			if (var.isPublic()) {
				index = 0;
			} else if (var.isPackagePrivate()) {
				index = 1;
			} else if (var.isProtected()) {
				index = 2;
			} else if (var.isPrivate()) {
				index = 3;
			} else if (var.isLocal()) {
				index = 4;
			}
		} catch (DebugException de) {
		}
		
		// If there was a problem, default to making the variable visible
		if (index == -1) {
			return true;
		}
		
		// Return whether the variable 
		try {
			return isVariableVisible(var, index);
		} catch (DebugException de) {
			return true;
		}
	}
	
	private boolean isVariableVisible(IJavaVariable var, int columnIndex) throws DebugException {
		boolean staticVar = var.isStatic();
		boolean finalVar = var.isFinal();
		
		// If the variable is static and the static flag is not set, 
		// this variable won't be visible
		if (staticVar) {
			if (!fFilterGrid[0][columnIndex]) {
				return false;
			}
		}
		
		// If the variable is final and the final flag is not set, 
		// this variable won't be visible
		if (finalVar) {
			if (!fFilterGrid[1][columnIndex]) {
				return false;
			}
		}
		
		// If this variable is 'normal' (not static and not final) and the normal flag
		// is not set, this variable won't be visible
		if (!staticVar && !finalVar) {
			if (!fFilterGrid[2][columnIndex]) {
				return false;
			}
		}
		
		// If this variable is synthetic and the synthetic flag is not set,
		// this variable won't be visible
		if (var.isSynthetic()) {
			if (!fFilterGrid[3][columnIndex]) {
				return false;
			}
		}
		
		// Otherwise the variable is visible
		return true;
	}

	private void loadFilterPreferences() {
		IPreferenceStore prefStore = getPreferenceStore();
		for (int i = 0; i < JDIDebugUIPlugin.fgModeModifierNames.length; i++) {
			for (int j = 0; j < JDIDebugUIPlugin.fgAccessModifierNames.length; j++) {
				String prefName = JDIDebugUIPlugin.generateVariableFilterPreferenceName(i, j);
				boolean value = prefStore.getBoolean(prefName);
				fFilterGrid[i][j] = value;
			}
		}
		fFilterPreferencesLoaded = true;
	}

	private IPreferenceStore getPreferenceStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}
	
}
