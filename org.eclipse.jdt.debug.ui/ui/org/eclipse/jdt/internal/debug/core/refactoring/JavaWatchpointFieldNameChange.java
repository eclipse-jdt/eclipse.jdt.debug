/*******************************************************************************
 * Copyright (c) 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.corext.refactoring.base.Change;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;

/**
 */
public class JavaWatchpointFieldNameChange extends Change {
	
	private IJavaWatchpoint fWatchpoint;
	private String fNewName;
	private String fOldName;
	private IType fDeclaringType;
	private IBreakpointsListener fBreakpointlistener;

	public static Change createChange(IField field, String newName) throws CoreException {
		String typeName= field.getDeclaringType().getFullyQualifiedName();
		String fieldName= field.getElementName();
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
				if (typeName.equals(watchpoint.getTypeName()) && fieldName.equals(watchpoint.getFieldName())) {
					return new JavaWatchpointFieldNameChange(watchpoint, newName);
				}
			}
		}
		return null;
	}

	public JavaWatchpointFieldNameChange(IJavaWatchpoint watchpoint, String newName) throws CoreException {
		fWatchpoint= watchpoint;
		fNewName= newName;
		fOldName= fWatchpoint.getFieldName();
		fDeclaringType= BreakpointUtils.getType(watchpoint);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.getString("JavaWatchpointFieldNameChange.1"), new String[] {fDeclaringType.getElementName(), fOldName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) throws CoreException {
		fBreakpointlistener= new IBreakpointsListener() {
			public void breakpointsAdded(IBreakpoint[] breakpoints) {
			}
			public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
				for (int i= 0; i < breakpoints.length; i++) {
					if (breakpoints[i].equals(fWatchpoint)) {
						fWatchpoint= null;
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
					}
				}	
			}
			public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
			}
		};
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(fBreakpointlistener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fBreakpointlistener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		if (fWatchpoint == null) {
			status.addFatalError(MessageFormat.format(RefactoringMessages.getString("JavaWatchpointFieldNameChange.2"), new String[] {fDeclaringType.getElementName(), fOldName})); //$NON-NLS-1$
		}
		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fBreakpointlistener);
		IField newField= fDeclaringType.getField(fNewName);
		Map attributes= fWatchpoint.getMarker().getAttributes();
		boolean isAccess= fWatchpoint.isAccess();
		boolean isModification= fWatchpoint.isModification();
		boolean isEnable= fWatchpoint.isEnabled();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newField);
		IJavaWatchpoint newWatchpoint= JDIDebugModel.createWatchpoint(fWatchpoint.getMarker().getResource(), fWatchpoint.getTypeName(), fNewName, fWatchpoint.getLineNumber(), fWatchpoint.getCharStart(), fWatchpoint.getCharEnd(), fWatchpoint.getHitCount(), true, attributes);
		newWatchpoint.setAccess(isAccess);
		newWatchpoint.setModification(isModification);
		newWatchpoint.setEnabled(isEnable);
		fWatchpoint.delete();
		return new JavaWatchpointFieldNameChange(newWatchpoint, fOldName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return fWatchpoint;
	}

}
