/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Adds a method breakpoint on a single selected element of type IMethod 
 */
public class ManageMethodBreakpointActionDelegate extends AbstractManageBreakpointActionDelegate {
		
	protected IJavaBreakpoint getBreakpoint(IMember element) {
		if (element instanceof IMethod) {
			IMethod method= (IMethod)element;
			IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
			IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
			for (int i= 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint= breakpoints[i];
				if (breakpoint instanceof IJavaMethodBreakpoint) {
					IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
					IMember container = null;
					try {
						container= BreakpointUtils.getMember(methodBreakpoint);
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
						return null;
					}
					if (container == null) {
						try {
							if (method.getDeclaringType().getFullyQualifiedName().equals(methodBreakpoint.getTypeName())
									&& method.getElementName().equals(methodBreakpoint.getMethodName())
									&& method.getSignature().equals(methodBreakpoint.getMethodSignature())) {
								return methodBreakpoint;
							}
						} catch (CoreException e) {
							JDIDebugUIPlugin.log(e);
						}
					} else {
						if (container instanceof IMethod) {
							if (method.getDeclaringType().getFullyQualifiedName().equals(container.getDeclaringType().getFullyQualifiedName())) {
								if (method.isSimilar((IMethod)container)) {
									return methodBreakpoint;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected IMember[] getMembers(ISelection s) {
		if (s instanceof IStructuredSelection) {
			ArrayList members= new ArrayList();
			for (Iterator iter= ((IStructuredSelection)s).iterator(); iter.hasNext();) {
				Object o=  iter.next();
				if (o instanceof IMethod) {
					members.add(o);
				}
			}
			return (IMember[])members.toArray(new IMember[0]);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		updateForRun();
		report(null);
		try {
			IMember[] members = getMembers();
			if (members == null || members.length == 0) {
				ITextSelection selection= getTextSelection();
				if (selection != null) {
					CompilationUnit compilationUnit= parseCompilationUnit();
					if (compilationUnit != null) {
						BreakpointMethodLocator locator= new BreakpointMethodLocator(selection.getOffset());
						compilationUnit.accept(locator);
						String methodName= locator.getMethodName();
						if (methodName == null) {
							report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.CantAdd")); //$NON-NLS-1$
							return;
						}
						String typeName= locator.getTypeName();
						String methodSignature= locator.getMethodSignature();
						if (methodSignature == null) {
							report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.methodNonAvailable")); //$NON-NLS-1$
							return;
						}
						// check if this method breakpoint already exist. If yes, remove it.
						IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
						IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
						for (int i= 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint= breakpoints[i];
							if (breakpoint instanceof IJavaMethodBreakpoint) {
								IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
								if (typeName.equals(methodBreakpoint.getTypeName())
										&& methodName.equals(methodBreakpoint.getMethodName())
										&& methodSignature.equals(methodBreakpoint.getMethodSignature())) {
									breakpointManager.removeBreakpoint(methodBreakpoint, true);
									return;
								}
							}
						}
						// add the breakpoint
						JDIDebugModel.createMethodBreakpoint(getResource(), typeName, methodName, methodSignature, true, false, false, -1, -1, -1, 0, true, new HashMap(10));
					}
				}
			} else {
				// check if all elements support method breakpoint
				for (int i= 0, length= members.length; i < length; i++) {
					if (!enableForMember(members[i])) {
						report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.CantAdd")); //$NON-NLS-1$
						return;
					}
				}
				// add or remove the breakpoint
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				for (int i= 0, length= members.length; i < length; i++) {
					IMethod method= (IMethod)members[i];
					IJavaBreakpoint breakpoint= getBreakpoint(method);
					if (breakpoint == null) {
						// add breakpoint
						int start = -1;
						int end = -1;
						ISourceRange range = method.getNameRange();
						if (range != null) {
							start = range.getOffset();
							end = start + range.getLength();
						}
						Map attributes = new HashMap(10);
						BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
						String methodName = method.getElementName();
						if (method.isConstructor()) {
							methodName = "<init>"; //$NON-NLS-1$
						}
						IType type= method.getDeclaringType();
						String methodSignature= method.getSignature();
						if (!type.isBinary()) {
							//resolve the type names
							methodSignature= resolveMethodSignature(type, methodSignature);
							if (methodSignature == null) {
								IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Source method signature could not be resolved", null); //$NON-NLS-1$
								JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageMethodBreakpointActionDelegate.Add_Method_Breakpoint_Failed_2"), status); //$NON-NLS-1$
								return;
							}
						}
						JDIDebugModel.createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method), type.getFullyQualifiedName(), methodName, methodSignature, true, false, false, -1, start, end, 0, true, attributes);
					} else {
						// remove breakpoint
						try {
							breakpointManager.removeBreakpoint(breakpoint, true);
						} catch (CoreException x) {
							JDIDebugUIPlugin.log(x);
							MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_removing_breakpoint_8"), x.getMessage()); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (CoreException x) {
			JDIDebugUIPlugin.log(x);
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_creating_breakpoint_7"), x.getMessage()); //$NON-NLS-1$
		}
	}

	public static String resolveMethodSignature(IType type, String methodSignature) throws JavaModelException {
		String[] parameterTypes= Signature.getParameterTypes(methodSignature);
		int length= length= parameterTypes.length;
		String[] resolvedParameterTypes= new String[length];
		
		for (int i = 0; i < length; i++) {
			resolvedParameterTypes[i]= resolveType(type, parameterTypes[i]);
			if (resolvedParameterTypes[i] == null) {
				return null;
			}
		}
		
		String resolvedReturnType= resolveType(type, Signature.getReturnType(methodSignature));
		if (resolvedReturnType == null) {
			return null;
		}
				
		return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
	}
	
	private static String resolveType(IType type, String typeSignature) throws JavaModelException {
		int count= Signature.getArrayCount(typeSignature);
		String elementTypeSignature= Signature.getElementType(typeSignature);
		if (elementTypeSignature.length() == 1) {
			// no need to resolve primitive types
			return typeSignature;
		}
		String elementTypeName= Signature.toString(elementTypeSignature);
		String[][] resolvedElementTypeNames= type.resolveType(elementTypeName);
		if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
			// the type name cannot be resolved
			return null;
		}
		String resolvedElementTypeName= Signature.toQualifiedName(resolvedElementTypeNames[0]);
		String resolvedElementTypeSignature= Signature.createTypeSignature(resolvedElementTypeName, true).replace('.', '/');
		return Signature.createArraySignature(resolvedElementTypeSignature, count);
	}

	/**
	 * @see AbstractManageBreakpointActionDelegate#enableForMember(IMember)
	 */
	protected boolean enableForMember(IMember member) {
		try {
			return member instanceof IMethod && !Flags.isAbstract(member.getFlags());
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
		return false;
	}
}
