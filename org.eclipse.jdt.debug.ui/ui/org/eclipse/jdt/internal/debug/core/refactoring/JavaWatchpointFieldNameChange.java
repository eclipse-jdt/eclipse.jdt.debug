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
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 */
public class JavaWatchpointFieldNameChange extends Change {
	
	private IJavaWatchpoint fWatchpoint;
	private String fNewName;
	private String fOldName;
	private IType fDeclaringType;

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
		return MessageFormat.format(RefactoringMessages.JavaWatchpointFieldNameChange_1, new String[] {fDeclaringType.getElementName(), fOldName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		if (!fWatchpoint.isRegistered()) {
			status.addFatalError(MessageFormat.format(RefactoringMessages.JavaWatchpointFieldNameChange_2, new String[] {fDeclaringType.getElementName(), fOldName})); //$NON-NLS-1$
		}
		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		IField newField= fDeclaringType.getField(fNewName);
		Map attributes= fWatchpoint.getMarker().getAttributes();
		boolean isAccess= fWatchpoint.isAccess();
		boolean isModification= fWatchpoint.isModification();
		boolean isEnable= fWatchpoint.isEnabled();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newField);
		IJavaWatchpoint newWatchpoint= JDIDebugModel.createWatchpoint(
				fWatchpoint.getMarker().getResource(),
				fWatchpoint.getTypeName(),
				fNewName,
				fWatchpoint.getLineNumber(),
				fWatchpoint.getCharStart(),
				fWatchpoint.getCharEnd(),
				fWatchpoint.getHitCount(),
				true,
				attributes);
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
