/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Editor for method entry/exit breakpoint.
 * 
 * @since 3.6
 */
public class MethodBreakpointEditor extends StandardJavaBreakpointEditor {
	
	// Method entry/exit editors
	private Button fEntry;
	private Button fExit;

	public static final int PROP_ENTRY = 0x1012;
	public static final int PROP_EXIT = 0x1013;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, 0, 0, 0);
		// add standard controls
		super.createStandardControls(composite);
		Composite watchComp = SWTFactory.createComposite(composite, parent.getFont(), 3, 1, 0, 0, 0);
		fEntry = createSusupendPropertyEditor(watchComp, processMnemonics(PropertyPageMessages.JavaLineBreakpointPage_10), PROP_ENTRY);
		fExit = createSusupendPropertyEditor(watchComp, processMnemonics(PropertyPageMessages.JavaLineBreakpointPage_11), PROP_EXIT); 
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#setBreakpoint(org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	@Override
	protected void setBreakpoint(IJavaBreakpoint breakpoint) throws CoreException {
		super.setBreakpoint(breakpoint);
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			IJavaMethodBreakpoint watchpoint = (IJavaMethodBreakpoint) breakpoint;
			fEntry.setEnabled(true);
			fExit.setEnabled(true);
			fEntry.setSelection(watchpoint.isEntry());
			fExit.setSelection(watchpoint.isExit());
		} else {
			fEntry.setEnabled(false);
			fExit.setEnabled(false);
			fEntry.setSelection(false);
			fExit.setSelection(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#doSave()
	 */
	@Override
	public void doSave() throws CoreException {
		super.doSave();
		IJavaBreakpoint breakpoint = getBreakpoint();
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			IJavaMethodBreakpoint watchpoint = (IJavaMethodBreakpoint) breakpoint;
			watchpoint.setEntry(fEntry.getSelection());
			watchpoint.setExit(fExit.getSelection());
		}
	}
}
