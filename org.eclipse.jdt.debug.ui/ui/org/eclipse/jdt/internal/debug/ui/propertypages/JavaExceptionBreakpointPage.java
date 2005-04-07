/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public class JavaExceptionBreakpointPage extends JavaBreakpointPage {

	private Button fCaughtButton;
	private Button fUncaughtButton;
	
	private static final String fgExceptionBreakpointError= PropertyPageMessages.JavaExceptionBreakpointPage_2; //$NON-NLS-1$
	/**
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#doStore()
	 */
	protected void doStore() throws CoreException {
		super.doStore();
		IJavaExceptionBreakpoint breakpoint= (IJavaExceptionBreakpoint) getBreakpoint();
		boolean caught= fCaughtButton.getSelection();
		if (caught != breakpoint.isCaught()) {
			breakpoint.setCaught(caught);
		}
		boolean uncaught= fUncaughtButton.getSelection();
		if (uncaught != breakpoint.isUncaught()) {
			breakpoint.setUncaught(uncaught);
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#createTypeSpecificEditors(org.eclipse.swt.widgets.Composite)
	 */
	protected void createTypeSpecificEditors(Composite parent) {
		IJavaExceptionBreakpoint breakpoint= (IJavaExceptionBreakpoint) getBreakpoint();
		SelectionAdapter exceptionBreakpointValidator= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				validateExceptionBreakpoint();
			}
		};
		createLabel(parent, PropertyPageMessages.JavaExceptionBreakpointPage_3); //$NON-NLS-1$
		fEnabledButton.addSelectionListener(exceptionBreakpointValidator);
		fCaughtButton= createCheckButton(parent, PropertyPageMessages.JavaExceptionBreakpointPage_0); //$NON-NLS-1$
		try {
			fCaughtButton.setSelection(breakpoint.isCaught());
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		fCaughtButton.addSelectionListener(exceptionBreakpointValidator);
		fUncaughtButton= createCheckButton(parent, PropertyPageMessages.JavaExceptionBreakpointPage_1); //$NON-NLS-1$
		try {
			fUncaughtButton.setSelection(breakpoint.isUncaught());
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		fUncaughtButton.addSelectionListener(exceptionBreakpointValidator);
	}

	private void validateExceptionBreakpoint() {
		if (fEnabledButton.getSelection() && !(fCaughtButton.getSelection() || fUncaughtButton.getSelection())) {
			addErrorMessage(fgExceptionBreakpointError);
		} else {
			removeErrorMessage(fgExceptionBreakpointError);
		}
	}
	
}
