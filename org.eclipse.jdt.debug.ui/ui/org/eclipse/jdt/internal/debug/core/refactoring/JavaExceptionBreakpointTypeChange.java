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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;


/**
 * Change to update an exception breakpoint when a IType is moved or renamed.
 */
public class JavaExceptionBreakpointTypeChange extends JavaBreakpointTypeChange {	

	private boolean fIsCaught;
	private boolean fIsUncaught;
	private boolean fIsChecked;
	
	public JavaExceptionBreakpointTypeChange(IJavaExceptionBreakpoint classPrepareBreakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		super(classPrepareBreakpoint, changedElement, argument, changeType);
		fIsCaught= classPrepareBreakpoint.isCaught();
		fIsUncaught= classPrepareBreakpoint.isUncaught();
		fIsChecked= classPrepareBreakpoint.isChecked();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#getErrorMessageNoMoreExists()
	 */
	public String getErrorMessageNoMoreExists() {
		return MessageFormat.format(RefactoringMessages.JavaExceptionBreakpointTypeChange_0, new String[] {getDeclaringType().getElementName()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.JavaExceptionBreakpointTypeChange_1, new String[] {getDeclaringType().getElementName()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#performChange(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public Change performChange(IType newType, Object undoChangedElement, Object undoArgument, int changeType) throws CoreException {
		Map attributes= getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newType);
		// create the new breakpoint
		IJavaExceptionBreakpoint newExceptionBreakpoint= JDIDebugModel.createExceptionBreakpoint(
				BreakpointUtils.getBreakpointResource(newType),
				newType.getFullyQualifiedName(),
				fIsCaught,
				fIsUncaught,
				fIsChecked,
				true,
				attributes);
		// delete the old one
		getBreakpoint().delete();
		return new JavaExceptionBreakpointTypeChange(newExceptionBreakpoint, undoChangedElement, undoArgument, changeType);
	}

}
