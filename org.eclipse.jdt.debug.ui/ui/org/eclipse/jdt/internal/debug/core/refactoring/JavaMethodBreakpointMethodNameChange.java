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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Change object to used to update a java method breakpoint.
 */
public class JavaMethodBreakpointMethodNameChange extends Change {
	
	private IJavaMethodBreakpoint fMethodBreakpoint;
	private String fNewMethodName;
	private String fOldMethodName;
	private IType fDeclaringType;
	
	/**
	 * Return the change object to used to update the corresponding java method breakpoint.
	 * Return <code>null</code> if there is no breakpoint for this method.
	 */
	public static Change createChange(IMethod method, String newName) throws CoreException {
		IType declaringType= method.getDeclaringType();
		String typeName= declaringType.getFullyQualifiedName();
		String methodName= method.getElementName();
		String methodSignature= ToggleBreakpointAdapter.resolveMethodSignature(declaringType, method.getSignature());
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
				if (typeName.equals(methodBreakpoint.getTypeName()) && methodName.equals(methodBreakpoint.getMethodName()) && methodSignature.equals(methodBreakpoint.getMethodSignature())) {
					return new JavaMethodBreakpointMethodNameChange(methodBreakpoint, newName);
				}
			}
		}
		return null;
	}
	
	protected JavaMethodBreakpointMethodNameChange(IJavaMethodBreakpoint methodBreakpoint, String newName) throws CoreException {
		fMethodBreakpoint= methodBreakpoint;
		fNewMethodName= newName;
		fOldMethodName= fMethodBreakpoint.getMethodName();
		fDeclaringType= BreakpointUtils.getType(methodBreakpoint);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.JavaMethodBreakpointMethodNameChange_0, new String[] {fDeclaringType.getElementName(), fOldMethodName}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		if (!fMethodBreakpoint.isRegistered()) {
			status.addFatalError(MessageFormat.format(RefactoringMessages.JavaMethodBreakpointMethodNameChange_1, new String[] {fDeclaringType.getElementName(), fOldMethodName})); //$NON-NLS-1$
		}
		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		String methodSignature= fMethodBreakpoint.getMethodSignature();
		String[] parameterTypes= Signature.getParameterTypes(methodSignature);
		for (int i= 0; i < parameterTypes.length; i++) {
			parameterTypes[i]= parameterTypes[i].replace('/', '.');
		}
		IMethod method= fDeclaringType.getMethod(fNewMethodName, parameterTypes);
		Map attributes= fMethodBreakpoint.getMarker().getAttributes();
		BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
		boolean isEnable= fMethodBreakpoint.isEnabled();
		IJavaMethodBreakpoint newMethodBreakpoint= JDIDebugModel.createMethodBreakpoint(
				fMethodBreakpoint.getMarker().getResource(),
				fMethodBreakpoint.getTypeName(),
				fNewMethodName,
				methodSignature,
				fMethodBreakpoint.isEntry(),
				fMethodBreakpoint.isExit(),
				fMethodBreakpoint.isNativeOnly(),
				fMethodBreakpoint.getLineNumber(),
				fMethodBreakpoint.getCharStart(),
				fMethodBreakpoint.getCharEnd(),
				fMethodBreakpoint.getHitCount(),
				true,
				attributes);
		newMethodBreakpoint.setEnabled(isEnable);
		fMethodBreakpoint.delete();
		return new JavaMethodBreakpointMethodNameChange(newMethodBreakpoint, fOldMethodName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return fMethodBreakpoint;
	}

}
