/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IViewSite;

/**
 * 
 */
public class DetailOptionsAction extends AbstractDisplayOptionsAction {
    
    public DetailOptionsAction() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractDisplayOptionsAction#getPreferenceInfo()
     */
    protected String[][] getPreferenceInfo() {
        return new String[][] {
                {IJDIPreferencesConstants.PREF_SHOW_DETAILS, JDIModelPresentation.SHOW_DETAILS}
        };
    }
    
	protected void applyPreference(String preference, String attribute, IDebugModelPresentation presentation) {
		String string = getStringPreferenceValue(getView().getSite().getId(), preference);
		presentation.setAttribute(attribute, string);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractDisplayOptionsAction#getDialog()
     */
    protected Dialog getDialog() {
        IViewSite viewSite = getView().getViewSite();
        return new DetailOptionsDialog(viewSite.getShell(), viewSite.getId());
    }
    
}
