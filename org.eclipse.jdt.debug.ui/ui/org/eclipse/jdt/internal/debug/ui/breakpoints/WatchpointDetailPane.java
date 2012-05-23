/*******************************************************************************
 *  Copyright (c) 2009, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.swt.widgets.Composite;

/**
 * Watchpoint detail pane. Suspend on access or modification.
 * 
 * @since 3.6
 */
public class WatchpointDetailPane extends AbstractDetailPane {
	
	/**
	 * Identifier for this detail pane editor
	 */
	public static final String DETAIL_PANE_WATCHPOINT = JDIDebugUIPlugin.getUniqueIdentifier() + ".DETAIL_PANE_WATCHPOINT"; //$NON-NLS-1$

	public WatchpointDetailPane() {
		super(BreakpointMessages.WatchpointDetailPane_0, BreakpointMessages.WatchpointDetailPane_0, DETAIL_PANE_WATCHPOINT);
		addAutosaveProperties(new int[]{
				StandardJavaBreakpointEditor.PROP_HIT_COUNT_ENABLED,
				StandardJavaBreakpointEditor.PROP_SUSPEND_POLICY,
				WatchpointEditor.PROP_ACCESS,
				WatchpointEditor.PROP_MODIFICATION
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractDetailPane#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractJavaBreakpointEditor createEditor(Composite parent) {
		return new WatchpointEditor();
	}

}
