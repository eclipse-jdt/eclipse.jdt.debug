/*******************************************************************************
 *  Copyright (c) 2003, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Job used to verify the position of a breakpoint
 */
public class BreakpointLocationVerifierJob extends Job {

	/**
	 * The temporary breakpoint that has been set. Can be <code>null</code> if the callee was not able
	 * to check if a breakpoint was already set at this position.
	 */
	private final IJavaLineBreakpoint fBreakpoint;

	/**
	 * The number of the line where the breakpoint has been requested.
	 */
	private final int fLineNumber;

	/**
	 * The qualified type name of the class where the temporary breakpoint as been set.
	 * Can be <code>null</code> if fBreakpoint is null.
	 */
	private final String fTypeName;

	/**
	 * The type in which should be set the breakpoint.
	 */
	private final IType fType;

	/**
	 * The current IEditorPart
	 */
	private final IEditorPart fEditorPart;

	/**
	 * The parsed {@link CompilationUnit}
	 */
	CompilationUnit fCunit = null;

	/**
	 * The document context
	 */
	private IDocument fDocument = null;

	/**
	 * The status line to use to display errors
	 */
	private final IEditorStatusLine fStatusLine;

	/**
	 * If a best guess should be made at the breakpoint location
	 */
	private boolean fBestMatch = false;

	/**
	 * Constructor
	 */
	public BreakpointLocationVerifierJob(IDocument document, CompilationUnit cunit, IJavaLineBreakpoint breakpoint, int lineNumber, String typeName, IType type, IEditorPart editorPart, boolean bestmatch) {
		super(ActionMessages.BreakpointLocationVerifierJob_breakpoint_location);
		fCunit = cunit;
		fDocument = document;
		fBreakpoint = breakpoint;
		fLineNumber = lineNumber;
		fTypeName = typeName;
		fType = type;
		fEditorPart = editorPart;
		fBestMatch = bestmatch;
		fStatusLine = editorPart.getAdapter(IEditorStatusLine.class);
		setSystem(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(fCunit, fLineNumber, true, fBestMatch);
		fCunit.accept(locator);
		int lineNumber = locator.getLineLocation();
		String typeName = locator.getFullyQualifiedTypeName();
		if (typeName == null) {
			return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
		}
		try {
			switch (locator.getLocationType()) {
				case ValidBreakpointLocationLocator.LOCATION_LINE:
					return manageLineBreakpoint(typeName, lineNumber);
				case ValidBreakpointLocationLocator.LOCATION_METHOD:
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					new ToggleBreakpointAdapter().toggleMethodBreakpoints(fEditorPart, new TextSelection(locator.getMemberOffset(), 0));
					break;
				case ValidBreakpointLocationLocator.LOCATION_FIELD:
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					new ToggleBreakpointAdapter().toggleWatchpoints(fEditorPart, new TextSelection(locator.getMemberOffset(), 0));
					break;
				default:
					// cannot find a valid location
					report(ActionMessages.BreakpointLocationVerifierJob_not_valid_location);
					if (fBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					}
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null);

	}

	/**
	 * Determines the placement of the line breakpoint, and ensures that duplicates are not created
	 * and that notification is sent in the event of collisions
	 * @param typeName the fully qualified name of the type to add the line breakpoint to
	 * @param lineNumber the number we wish to put the breakpoint on
	 * @return the status of the line breakpoint placement
	 */
	public IStatus manageLineBreakpoint(String typeName, int lineNumber) {
		try {
			boolean differentLineNumber= lineNumber != fLineNumber;
			IJavaLineBreakpoint breakpoint= JDIDebugModel.lineBreakpointExists(fBreakpoint.getMarker().getResource(), typeName, lineNumber);
			boolean breakpointExist= breakpoint != null;
			if (fBreakpoint == null) {
				if (breakpointExist) {
					if (differentLineNumber) {
						// There is already a breakpoint on the valid line.
						report(NLS.bind(ActionMessages.BreakpointLocationVerifierJob_0, Integer.toString(lineNumber)));
						return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
					}
					// There is already a breakpoint on the valid line, but it's also the requested line.
					// Removing the existing breakpoint.
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpointRemoved, null);
				}
				createNewBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null);
			}
			if (differentLineNumber) {
				if (breakpointExist) {
					// there is already a breakpoint on the valid line.
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(fBreakpoint, true);
					report(NLS.bind(ActionMessages.BreakpointLocationVerifierJob_0, Integer.toString(lineNumber)));
					return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.BreakpointLocationVerifierJob_not_valid_location, null);
				}
				replaceBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.BreakpointLocationVerifierJob_breakpointMovedToValidPosition, null);
			}
			if (!typeName.equals(fTypeName)) {
				replaceBreakpoint(lineNumber, typeName);
				return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.WARNING, ActionMessages.BreakpointLocationVerifierJob_breakpointSetToRightType, null);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, ActionMessages.BreakpointLocationVerifierJob_breakpoint_set, null);
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
		Map<String, Object> newAttributes = new HashMap<>(10);
		int start = -1, end = -1;
		if (fType != null) {
			try {
				IRegion line = fDocument.getLineInformation(lineNumber - 1);
				start = line.getOffset();
				end = start + line.getLength();

			} catch (BadLocationException ble) {
				JDIDebugUIPlugin.log(ble);
			}
			BreakpointUtils.addJavaBreakpointAttributes(newAttributes, fType);
		}
		IJavaLineBreakpoint newBreakpoint = JDIDebugModel.createLineBreakpoint(fBreakpoint.getMarker().getResource(), typeName, lineNumber, start, end, 0, true, newAttributes);
		String condition = fBreakpoint.getCondition();
		if (condition != null && condition.length() != 0) {
			newBreakpoint.setCondition(fBreakpoint.getCondition());
			newBreakpoint.setConditionEnabled(fBreakpoint.isConditionEnabled());
			newBreakpoint.setConditionSuspendOnTrue(fBreakpoint.isConditionSuspendOnTrue());
		}
	}

	/**
	 * Reports any status to the current active workbench shell
	 * @param message the message to display
	 */
	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
			}
		});
	}
}
