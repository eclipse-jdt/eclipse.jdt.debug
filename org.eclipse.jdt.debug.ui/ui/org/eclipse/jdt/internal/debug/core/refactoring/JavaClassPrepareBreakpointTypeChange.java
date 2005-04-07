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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;


/**
 * Change to update a class prepare breakpoint when a IType is moved or renamed.
 */
public class JavaClassPrepareBreakpointTypeChange extends JavaBreakpointTypeChange {

	private int fMemberType;
	
	public JavaClassPrepareBreakpointTypeChange(IJavaClassPrepareBreakpoint classPrepareBreakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		super(classPrepareBreakpoint, changedElement, argument, changeType);
		fMemberType= classPrepareBreakpoint.getMemberType();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#getErrorMessageNoMoreExists()
	 */
	public String getErrorMessageNoMoreExists() {
		return MessageFormat.format(RefactoringMessages.JavaClassPrepareBreakpointTypeChange_0, new String[] {getDeclaringType().getElementName()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.JavaClassPrepareBreakpointTypeChange_1, new String[] {getDeclaringType().getElementName()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#performChange(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public Change performChange(IType newType, Object undoChangedElement, Object undoArgument, int changeType) throws CoreException {
		Map attributes= getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newType);
		ISourceRange range= newType.getNameRange();
		int charStart= -1;
		int charEnd= -1;
		if (range != null) {
			charStart= range.getOffset();
			charEnd= charStart + range.getLength();
		}
		// create the new breakpoint
		IJavaClassPrepareBreakpoint newClassPrepareBreakpoint= JDIDebugModel.createClassPrepareBreakpoint(
				BreakpointUtils.getBreakpointResource(newType),
				newType.getFullyQualifiedName(),
				fMemberType,
				charStart,
				charEnd,
				true,
				attributes);
		// delete the old one
		getBreakpoint().delete();
		return new JavaClassPrepareBreakpointTypeChange(newClassPrepareBreakpoint, undoChangedElement, undoArgument, changeType);
	}

}
