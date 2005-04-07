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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;


/**
 * Change to update a method breakpoint when a IType is moved or renamed.
 */
public class JavaMethodBreakpointTypeChange extends JavaLineBreakpointTypeChange {

	private String fMethodName;
	private String fMethodSignature;
	private boolean fIsEntry;
	private boolean fIsExit;
	private boolean fIsNativeOnly;
	private boolean fIsEnable;
	
	public JavaMethodBreakpointTypeChange(IJavaMethodBreakpoint methodBreakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		super(methodBreakpoint, changedElement, argument, changeType);
		fMethodName= methodBreakpoint.getMethodName();
		fMethodSignature= methodBreakpoint.getMethodSignature();
		fIsEntry= methodBreakpoint.isEntry();
		fIsExit= methodBreakpoint.isExit();
		fIsNativeOnly= methodBreakpoint.isNativeOnly();
		fIsEnable= methodBreakpoint.isEnabled();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#getErrorMessageNoMoreExists()
	 */
	public String getErrorMessageNoMoreExists() {
		return MessageFormat.format(RefactoringMessages.JavaMethodBreakpointTypeChange_0, new String[] {getDeclaringType().getElementName(), fMethodName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.JavaMethodBreakpointTypeChange_1, new String[] {getDeclaringType().getElementName(), fMethodName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.JavaBreakpointTypeNameChange#performChange(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public Change performChange(IType newType, Object undoChangedElement, Object undoArgument, int changeType) throws CoreException {
		String[] parameterTypes= Signature.getParameterTypes(fMethodSignature);
		for (int i= 0; i < parameterTypes.length; i++) {
			parameterTypes[i]= parameterTypes[i].replace('/', '.');
		}
		IMethod method= newType.getMethod(fMethodName, parameterTypes);
		IMethod[] methods= newType.findMethods(method);
		int lineNumber= getLineNumber();
		int start = getCharStart();
		int end = getCharEnd();
		if (methods.length == 1) {
			method= methods[0];
			ISourceRange range = method.getNameRange();
			if (range != null) {
				start = range.getOffset();
				end = start + range.getLength();
				lineNumber= -1;
			}
		}
		Map attributes= getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
		IJavaMethodBreakpoint newMethodBreakpoint= JDIDebugModel.createMethodBreakpoint(
				BreakpointUtils.getBreakpointResource(newType),
				newType.getFullyQualifiedName(),
				fMethodName,
				fMethodSignature,
				fIsEntry,
				fIsExit,
				fIsNativeOnly,
				lineNumber,
				start,
				end,
				getHitCount(),
				true,
				attributes);
		newMethodBreakpoint.setEnabled(fIsEnable);
		getBreakpoint().delete();
		return new JavaMethodBreakpointTypeChange(newMethodBreakpoint, undoChangedElement, undoArgument, changeType);
	}

}
