package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

public class JavaExpressionsViewFilterPreferenceAction extends JavaVariablesFilterPreferenceAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.JavaVariablesFilterPreferenceAction#getPreferencePrefix()
	 */
	protected String getPreferencePrefix() {
		return JDIDebugUIPlugin.EXPRESSIONS_VIEW_FILTER_PREFIX;
	}

}
