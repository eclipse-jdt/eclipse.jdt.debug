/*******************************************************************************
 *  Copyright (c) 2009, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.swt.widgets.Composite;

/**
 * Suspend policy and hit count detail pane.
 *
 * @since 3.6
 */
public class StandardBreakpointDetailPane extends AbstractDetailPane {

	/**
	 * Identifier for this detail pane editor
	 */
	public static final String DETAIL_PANE_STANDARD = JDIDebugUIPlugin.getUniqueIdentifier() + ".DETAIL_PANE_STANDARD"; //$NON-NLS-1$

	public StandardBreakpointDetailPane() {
		super(BreakpointMessages.StandardBreakpointDetailPane_0, BreakpointMessages.StandardBreakpointDetailPane_0, DETAIL_PANE_STANDARD);
		addAutosaveProperties(new int[]{
				StandardJavaBreakpointEditor.PROP_HIT_COUNT_ENABLED,
				StandardJavaBreakpointEditor.PROP_SUSPEND_POLICY, StandardJavaBreakpointEditor.PROP_TRIGGER_POINT, });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractDetailPane#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractJavaBreakpointEditor createEditor(Composite parent) {
		return new StandardJavaBreakpointEditor();
	}

}
