/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Job used to verify the position of a breakpoint
 */
public class BreakpointLocationVerifierJob extends Job {

	/**
	 * The document which contains the code source.
	 */
	private IDocument fDocument;
	
	/**
	 * The offset in the document where the breakpoint has been requested
	 */
	private int fOffset;

	/**
	 * The temporary breakpoint that has been set. Can be <code>null</code> if the callee was not able
	 * to check if a breakpoint was already set at this position.
	 */	
	private IJavaLineBreakpoint fBreakpoint;
	
	/**
	 * The number of the line where the breakpoint has been requested.
	 */
	private int fLineNumber;
	
	/**
	 * The qualified type name of the class where the temporary breakpoint as been set.
	 * Can be <code>null</code> if fBreakpoint is null.
	 */	
	private String fTypeName;
	
	/**
	 * The type in which should be set the breakpoint.
	 */
	private IType fType;

	/**
	 * The resource in which should be set the breakpoint.
	 */
	private IResource fResource;
	
	/**
	 * The status line to use to display errors
	 */
	private IEditorStatusLine fStatusLine;
	
	public BreakpointLocationVerifierJob(IDocument document, int offset, IJavaLineBreakpoint breakpoint, int lineNumber, String typeName, IType type, IResource resource, IEditorStatusLine statusLine) {
		super(ActionMessages.getString("BreakpointLocationVerifierJob.breakpoint_location")); //$NON-NLS-1$
		fDocument= document;
		fOffset= offset;
		fBreakpoint= breakpoint;
		fLineNumber= lineNumber;
		fTypeName= typeName;
		fType= type;
		fResource= resource;
		fStatusLine= statusLine;
	}
	
	public IStatus run(IProgressMonitor monitor) {
		CompilationUnit compilationUnit= AST.parseCompilationUnit(fDocument.get().toCharArray());
		ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, fOffset);
		compilationUnit.accept(locator);
		int lineNumber= locator.getValidLocation();		
		String typeName= locator.getFullyQualifiedTypeName();
		
		try {
			if (lineNumber == -1) {
				// cannot found a valid line
				report(ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location")); //$NON-NLS-1$
				if (fBreakpoint != null) {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
				}
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location"), null); //$NON-NLS-1$
			}
			boolean differentLineNumber= lineNumber != fLineNumber;
			IJavaLineBreakpoint breakpoint= JDIDebugModel.lineBreakpointExists(typeName, lineNumber);
			boolean breakpointExist= breakpoint != null;
			if (fBreakpoint == null) {
				if (breakpointExist) {
					if (differentLineNumber) {
						// There is already a breakpoint on the valid line.
						report(ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location")); //$NON-NLS-1$
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location"), null); //$NON-NLS-1$
					} else {
						// There is already a breakpoint on the valid line, but it's also the requested line.
						// Removing the existing breakpoint.
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.getString("BreakpointLocationVerifierJob.breakpointRemoved"), null); //$NON-NLS-1$
					}
				}
				createNewBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.getString("BreakpointLocationVerifierJob.breakpoint_set"), null); //$NON-NLS-1$
			} else {
				if (differentLineNumber) {
					if (breakpointExist) {
						// there is already a breakpoint on the valid line.
						report(ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location")); //$NON-NLS-1$
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.getString("BreakpointLocationVerifierJob.not_valid_location"), null); //$NON-NLS-1$
					}
					replaceBreakpoint(lineNumber, typeName);
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.getString("BreakpointLocationVerifierJob.breakpointMovedToValidPosition"), null); //$NON-NLS-1$
				}
				if (!typeName.equals(fTypeName)) {
					replaceBreakpoint(lineNumber, typeName);
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.getString("BreakpointLocationVerifierJob.breakpointSetToRightType"), null); //$NON-NLS-1$
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.getString("BreakpointLocationVerifierJob.breakpoint_set"), null); //$NON-NLS-1$
	}
	
	/**
	 * Remove the temporary breakpoint and create a new breakpoint at the right position.
	 */
	private void replaceBreakpoint(int lineNumber, String typeName) throws CoreException {
		createNewBreakpoint(lineNumber, typeName);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
	}

	/**
	 * Create a new breakpoint at the right position.
	 */
	private void createNewBreakpoint(int lineNumber, String typeName) throws CoreException {
		Map newAttributes = new HashMap(10);
		if (fType != null) {
			try {
				IRegion line= fDocument.getLineInformation(lineNumber - 1);
				int start= line.getOffset();
				int end= start + line.getLength() - 1;
				BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(newAttributes, fType, start, end);
			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
		}
		JDIDebugModel.createLineBreakpoint(fResource, typeName, lineNumber, -1, -1, 0, true, newAttributes);
	}

	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
				if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
					Display.getCurrent().beep();
				}
			}
		});
	}
}