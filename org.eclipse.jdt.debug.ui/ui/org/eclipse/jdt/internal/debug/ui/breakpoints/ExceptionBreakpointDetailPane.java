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
 * Exception breakpoint detail pane.
 *
 * @since 3.6
 */
public class ExceptionBreakpointDetailPane extends AbstractDetailPane {

	/**
	 * Identifier for this detail pane editor
	 */
	public static final String DETAIL_PANE_EXCEPTION_BREAKPOINT = JDIDebugUIPlugin.getUniqueIdentifier() + ".DETAIL_PANE_EXCEPTION_BREAKPOINT"; //$NON-NLS-1$

	public ExceptionBreakpointDetailPane() {
		super(BreakpointMessages.ExceptionBreakpointDetailPane_0, BreakpointMessages.ExceptionBreakpointDetailPane_0, DETAIL_PANE_EXCEPTION_BREAKPOINT);
		addAutosaveProperties(new int[]{
				StandardJavaBreakpointEditor.PROP_HIT_COUNT_ENABLED,
				StandardJavaBreakpointEditor.PROP_SUSPEND_POLICY,
				StandardJavaBreakpointEditor.PROP_TRIGGER_POINT,
				ExceptionBreakpointEditor.PROP_CAUGHT,
				ExceptionBreakpointEditor.PROP_UNCAUGHT,
				ExceptionBreakpointEditor.PROP_SUBCLASSES
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractDetailPane#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractJavaBreakpointEditor createEditor(Composite parent) {
		return new ExceptionBreakpointEditor();
	}

}
