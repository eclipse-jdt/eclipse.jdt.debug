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
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;


/**
 * Change to update a line breakpoint when a IType is moved or renamed.
 */
public class JavaLineBreakpointTypeChange extends JavaBreakpointTypeChange {
	
	private int fLineNumber;
	private int fCharStart;
	private int fCharEnd;
	
	public JavaLineBreakpointTypeChange(IJavaLineBreakpoint lineBreakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		super(lineBreakpoint, changedElement, argument, changeType);
		fLineNumber= lineBreakpoint.getLineNumber();
		fCharStart= lineBreakpoint.getCharStart();
		fCharEnd= lineBreakpoint.getCharEnd();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#getErrorMessageNoMoreExists()
	 */
	public String getErrorMessageNoMoreExists() {
		return MessageFormat.format(RefactoringMessages.JavaLineBreakpointTypeChange_0, new String[] {getDeclaringType().getElementName(), Integer.toString(fLineNumber)}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.JavaLineBreakpointTypeChange_1, new String[] {getDeclaringType().getElementName(), Integer.toString(fLineNumber)}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#performChange(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public Change performChange(IType newType, Object undoChangedElement, Object undoArgument, int changeType) throws CoreException {
		if (newType == null) {
			return new NullChange();
		}
		Map attributes= getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, newType);
		// create the new breakpoint
		IJavaLineBreakpoint newLineBreakpoint= JDIDebugModel.createLineBreakpoint(
				BreakpointUtils.getBreakpointResource(newType),
				newType.getFullyQualifiedName(),
				fLineNumber,
				fCharStart,
				fCharEnd,
				getHitCount(),
				true,
				attributes
				);
		// delete the old one
		getBreakpoint().delete();
		return new JavaLineBreakpointTypeChange(newLineBreakpoint, undoChangedElement, undoArgument, changeType);
	}

	public int getCharEnd() {
		return fCharEnd;
	}
	
	public int getCharStart() {
		return fCharStart;
	}
	
	/**
	 * Return the number of the line where the breakpoint is set
	 */
	public int getLineNumber() {
		return fLineNumber;
	}

}
