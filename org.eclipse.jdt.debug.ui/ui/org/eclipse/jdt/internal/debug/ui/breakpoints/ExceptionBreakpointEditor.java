/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.6
 */
public class ExceptionBreakpointEditor extends StandardJavaBreakpointEditor {
	
	/**
     * Property id's
     */
    public static final int PROP_CAUGHT = 0x1020;
    public static final int PROP_UNCAUGHT = 0x1021;
    public static final int PROP_SUBCLASSES = 0x1022;
	
	// editors
	private Button fCaught;
	private Button fUncaught;
	private Button fSubclasses;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
		// add standard controls
		super.createStandardControls(composite);
		Composite watchComp = SWTFactory.createComposite(composite, parent.getFont(), 3, 1, GridData.FILL_BOTH, 5, 5);
		Label label = SWTFactory.createLabel(watchComp, PropertyPageMessages.ExceptionBreakpointEditor_0, 1);
		GridData gd = (GridData) label.getLayoutData();
		gd.horizontalAlignment = SWT.LEFT;
		gd.grabExcessHorizontalSpace = false;
		fCaught = createSusupendPropertyEditor(watchComp, PropertyPageMessages.ExceptionBreakpointEditor_1, PROP_CAUGHT);
		fUncaught = createSusupendPropertyEditor(watchComp, PropertyPageMessages.ExceptionBreakpointEditor_2, PROP_UNCAUGHT);
		new Label(watchComp, SWT.None);
		fSubclasses = createSusupendPropertyEditor(watchComp, PropertyPageMessages.ExceptionBreakpointEditor_3, PROP_SUBCLASSES);
		gd = (GridData) fSubclasses.getLayoutData();
		gd.horizontalSpan = 2;
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#setBreakpoint(org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	protected void setBreakpoint(IJavaBreakpoint breakpoint) throws CoreException {
		super.setBreakpoint(breakpoint);
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			IJavaExceptionBreakpoint ex = (IJavaExceptionBreakpoint) breakpoint;
			fCaught.setEnabled(true);
			fUncaught.setEnabled(true);
			fSubclasses.setEnabled(true);
			fCaught.setSelection(ex.isCaught());
			fUncaught.setSelection(ex.isUncaught());
			fSubclasses.setSelection(((JavaExceptionBreakpoint)ex).isSuspendOnSubclasses());
		} else {
			fCaught.setEnabled(false);
			fUncaught.setEnabled(false);
			fSubclasses.setEnabled(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#doSave()
	 */
	public void doSave() throws CoreException {
		super.doSave();
		IJavaBreakpoint breakpoint = getBreakpoint();
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			IJavaExceptionBreakpoint ex = (IJavaExceptionBreakpoint) breakpoint;
			ex.setCaught(fCaught.getSelection());
			ex.setUncaught(fUncaught.getSelection());
			((JavaExceptionBreakpoint)ex).setSuspendOnSubclasses(fSubclasses.getSelection());
		}
	}
}
