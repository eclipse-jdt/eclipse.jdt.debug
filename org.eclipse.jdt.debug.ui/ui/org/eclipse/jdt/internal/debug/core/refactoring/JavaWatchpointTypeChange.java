/*******************************************************************************
 * Copyright (c) 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;


/**
 * Change to update a watchpoint when a IType is moved or renamed.
 */
public class JavaWatchpointTypeChange extends JavaLineBreakpointTypeChange {

	private String fFieldName;
	private boolean fIsAccess;
  	private boolean fIsModification;

	public JavaWatchpointTypeChange(IJavaWatchpoint watchpoint, IType changedType, Object argument, int changeType) throws CoreException {
		super(watchpoint, changedType, argument, changeType);
		fFieldName= watchpoint.getFieldName();
		fIsAccess= watchpoint.isAccess();
		fIsModification= watchpoint.isModification();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#getErrorMessageNoMoreExists()
	 */
	public String getErrorMessageNoMoreExists() {
		return MessageFormat.format(RefactoringMessages.getString("JavaWatchpointTypeChange.0"), new String[] {getDeclaringType().getElementName(), fFieldName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.getString("JavaWatchpointTypeChange.1"), new String[] {getDeclaringType().getElementName(), fFieldName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#performChange(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public Change performChange(IType newType, IType undoTypeChanged, Object undoArgument, int changeType) throws CoreException {
		IField newField= newType.getField(fFieldName);
		Map attributes= getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newField);
		// create the new breakpoint
		IJavaWatchpoint newWatchpoint= JDIDebugModel.createWatchpoint(
				newType.getResource(),
				newType.getFullyQualifiedName(),
				fFieldName,
				getLineNumber(),
				getCharStart(),
				getCharEnd(),
				getHitCount(),
				true,
				attributes);
		newWatchpoint.setAccess(fIsAccess);
		newWatchpoint.setModification(fIsModification);
		newWatchpoint.setEnabled(getEnable());
		// delete the old one
		getBreakpoint().delete();
		return new JavaWatchpointTypeChange(newWatchpoint, undoTypeChanged, undoArgument, changeType);
	}

}
