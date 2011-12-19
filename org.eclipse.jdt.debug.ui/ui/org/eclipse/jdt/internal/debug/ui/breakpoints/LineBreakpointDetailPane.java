/*******************************************************************************
 *  Copyright (c) 2010, 2011 IBM Corporation and others.
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
 * Detail pane for editing a line breakpoint.
 *
 * @since 3.6
 */
public class LineBreakpointDetailPane extends AbstractDetailPane {
	
	/**
	 * Identifier for this detail pane editor
	 */
	public static final String DETAIL_PANE_LINE_BREAKPOINT = JDIDebugUIPlugin.getUniqueIdentifier() + ".DETAIL_PANE_LINE_BREAKPOINT"; //$NON-NLS-1$
	
	public LineBreakpointDetailPane() {
		super(BreakpointMessages.BreakpointConditionDetailPane_0, BreakpointMessages.BreakpointConditionDetailPane_0, DETAIL_PANE_LINE_BREAKPOINT); 
		addAutosaveProperties(new int[]{
				JavaBreakpointConditionEditor.PROP_CONDITION_ENABLED,
				JavaBreakpointConditionEditor.PROP_CONDITION_SUSPEND_POLICY,
				StandardJavaBreakpointEditor.PROP_HIT_COUNT_ENABLED,
				StandardJavaBreakpointEditor.PROP_SUSPEND_POLICY});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractDetailPane#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractJavaBreakpointEditor createEditor(Composite parent) {
		return new CompositeBreakpointEditor(
			new AbstractJavaBreakpointEditor[] {new StandardJavaBreakpointEditor(), new JavaBreakpointConditionEditor(null)});
	}

}
