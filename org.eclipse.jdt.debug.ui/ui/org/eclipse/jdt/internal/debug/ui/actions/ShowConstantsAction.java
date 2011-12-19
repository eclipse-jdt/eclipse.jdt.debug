/*******************************************************************************
 * Copyright (c) 2000, 20057 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;

/**
 * Shows static final variables (constants)
 */
public class ShowConstantsAction extends ToggleBooleanPreferenceAction {

	public ShowConstantsAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	@Override
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_SHOW_CONSTANTS; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ToggleBooleanPreferenceAction#getViewKey()
	 */
	@Override
	protected String getViewKey() {
		return getCompositeKey();
	}
	
}
