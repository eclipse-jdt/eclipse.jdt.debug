/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * Implements the property page for a Java exception breakpoint
 */
public class JavaExceptionBreakpointPage extends JavaBreakpointPage {

	//widgets
	private Button fCaughtButton;
	private Button fUncaughtButton;
	private Button fSuspendOnSubclasses;
	
	private static final String fgExceptionBreakpointError= PropertyPageMessages.JavaExceptionBreakpointPage_2; 
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
		// TODO remove cast once the API freeze is over and the method has been added to IJavaExceptionBreakpoint
		boolean suspend = fSuspendOnSubclasses.getSelection();
		if(suspend != ((JavaExceptionBreakpoint)breakpoint).isSuspendOnSubclasses()) {
			((JavaExceptionBreakpoint)breakpoint).setSuspendOnSubclasses(suspend);
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#createTypeSpecificEditors(org.eclipse.swt.widgets.Composite)
	 */
	protected void createTypeSpecificEditors(Composite parent) {
		setTitle(PropertyPageMessages.JavaExceptionBreakpointPage_5);
		IJavaExceptionBreakpoint breakpoint= (IJavaExceptionBreakpoint) getBreakpoint();
		SelectionAdapter exceptionBreakpointValidator= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				validateExceptionBreakpoint();
			}
		};
		createLabel(parent, PropertyPageMessages.JavaExceptionBreakpointPage_3); 
		fEnabledButton.addSelectionListener(exceptionBreakpointValidator);
		fCaughtButton= createCheckButton(parent, PropertyPageMessages.JavaExceptionBreakpointPage_0); 
		try {
			fCaughtButton.setSelection(breakpoint.isCaught());
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		fCaughtButton.addSelectionListener(exceptionBreakpointValidator);
		fUncaughtButton= createCheckButton(parent, PropertyPageMessages.JavaExceptionBreakpointPage_1); 
		try {
			fUncaughtButton.setSelection(breakpoint.isUncaught());
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		fUncaughtButton.addSelectionListener(exceptionBreakpointValidator);
		fSuspendOnSubclasses = createCheckButton(parent, PropertyPageMessages.JavaExceptionBreakpointPage_4);
		try {
			// TODO add back the API for isSuspendOnSubclasses and setSuspendOnSubclasses in IJavaExceptionBreakpoint, and remove the cast below
			fSuspendOnSubclasses.setSelection(((JavaExceptionBreakpoint)breakpoint).isSuspendOnSubclasses());
		}
		catch (CoreException ce) {JDIDebugPlugin.log(ce);}
	}

	/**
	 * validates the exception breakpoint
	 */
	private void validateExceptionBreakpoint() {
		if (fEnabledButton.getSelection() && !(fCaughtButton.getSelection() || fUncaughtButton.getSelection())) {
			addErrorMessage(fgExceptionBreakpointError);
		} else {
			removeErrorMessage(fgExceptionBreakpointError);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_EXCEPTION_BREAKPOINT_PROPERTY_PAGE);
	}
}
