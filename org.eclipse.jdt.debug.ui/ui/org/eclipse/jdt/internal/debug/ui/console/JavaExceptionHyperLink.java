/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.JavaBreakpointPropertiesAction;
import org.eclipse.jdt.internal.debug.ui.breakpoints.AddExceptionDialog;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * A hyperlink that creates an exception breakpoint.
 */
public class JavaExceptionHyperLink extends JavaStackTraceHyperlink {

	private String fExceptionName = null;
	
	/**
	 * Constructs a new hyper link
	 * 
	 * @param console the console the link is contained in
	 * @param exceptionName fully qualified name of the exception
	 */
	public JavaExceptionHyperLink(IConsole console, String exceptionName) {
		super(console);
		fExceptionName = exceptionName;
	}
	
	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
	 */
	public void linkActivated() {
		try {
			// check for an existing breakpoint
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof IJavaExceptionBreakpoint) {
					IJavaExceptionBreakpoint exceptionBreakpoint = (IJavaExceptionBreakpoint)breakpoint;
					if (fExceptionName.equals(exceptionBreakpoint.getTypeName())) {
						showProperties(exceptionBreakpoint);
						return;
					}
				}
			}
			// create a new exception breakpoint
			IJavaSourceLocation[] sourceLocations = getSourceLocations();
			for (int i = 0; i < sourceLocations.length; i++) {
				IJavaSourceLocation location = sourceLocations[i];
				Object sourceElement = location.findSourceElement(fExceptionName);
				IType type = null;
				if (sourceElement != null) {
					IResource res = null;
					if (sourceElement instanceof IJavaElement) {
						IJavaElement element = (IJavaElement)sourceElement;
						res = element.getResource();
						if (res == null) {
							res = element.getJavaProject().getResource();
						}
					}
					if (res == null) {
						res = ResourcesPlugin.getWorkspace().getRoot();
					}
					if (sourceElement instanceof ICompilationUnit) {
						type = ((ICompilationUnit)sourceElement).findPrimaryType();
					} else if (sourceElement instanceof IClassFile) {
						type = ((IClassFile)sourceElement).getType();
					}
					boolean checked = false;
					if (type != null) {
						checked = AddExceptionDialog.getExceptionType(type) == AddExceptionDialog.CHECKED_EXCEPTION;
					}
					IJavaExceptionBreakpoint breakpoint = JDIDebugModel.createExceptionBreakpoint(res, fExceptionName, true, true, checked, true, null);
					showProperties(breakpoint);
					return;
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(ConsoleMessages.getString("JavaStackTraceHyperlink.An_exception_occurred_while_following_link._3"), e); //$NON-NLS-1$
			return;
		}
	}

	/**
	 * Show the properties dialog for the given breakpoint.
	 * 
	 * @param exceptionBreakpoint
	 */
	private void showProperties(IJavaExceptionBreakpoint breakpoint) {
		JavaBreakpointPropertiesAction action= new JavaBreakpointPropertiesAction();
		action.selectionChanged(null, new StructuredSelection(breakpoint));
		action.run(null);		
	}	

}
