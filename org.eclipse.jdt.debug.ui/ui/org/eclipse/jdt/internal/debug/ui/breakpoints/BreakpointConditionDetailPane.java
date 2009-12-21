/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.jdt.debug.ui.breakpoints.JavaBreakpointConditionEditor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.swt.widgets.Composite;

/**
 * Detail pane for editing a breakpoint condition and its suspend policy
 *
 * @since 3.6
 */
public class BreakpointConditionDetailPane extends AbstractDetailPane {
	
	/**
	 * Identifier for this detail pane editor
	 */
	public static final String DETAIL_PANE_CONDITION = JDIDebugUIPlugin.getUniqueIdentifier() + ".DETAIL_PANE_CONDITION"; //$NON-NLS-1$
	
	public BreakpointConditionDetailPane() {
		super(BreakpointMessages.BreakpointConditionDetailPane_0, BreakpointMessages.BreakpointConditionDetailPane_0, DETAIL_PANE_CONDITION); 
		addAutosaveProperties(new int[]{
				JavaBreakpointConditionEditor.PROP_CONDITION_ENABLED,
				JavaBreakpointConditionEditor.PROP_CONDITION_SUSPEND_POLICY});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractDetailPane#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	protected AbstractJavaBreakpointEditor createEditor(Composite parent) {
		return new JavaBreakpointConditionEditor();
	}

}
