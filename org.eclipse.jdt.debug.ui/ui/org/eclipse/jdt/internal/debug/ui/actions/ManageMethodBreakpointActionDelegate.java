/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

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
								if (((IMethod)method).isSimilar((IMethod)container)) {
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
	
	protected IMember getMember(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) s;
			if (ss.size() == 1) {					
				Object o=  ss.getFirstElement();
				if (o instanceof IMethod) {
					return (IMethod) o;
				}
			}
		}
		return null;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		updateForRun();
		report(null);
		if (getBreakpoint() == null) {
			// add breakpoint
			try {
				IMember member = getMember();
				if (member == null) {
					IWorkbenchPage page= getPage();
					if (page != null) {
						ISelection selection= page.getSelection();
						if (selection instanceof ITextSelection) {
							IEditorInput editorInput = getTextEditor().getEditorInput();
							IResource resource;
							if (editorInput instanceof IFileEditorInput) {
								resource= ((IFileEditorInput)editorInput).getFile();
							} else {
								resource= ResourcesPlugin.getWorkspace().getRoot();
							}
							IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
							CompilationUnit compilationUnit= AST.parseCompilationUnit(document.get().toCharArray());
							BreakpointMethodLocator locator= new BreakpointMethodLocator(((ITextSelection)selection).getOffset());
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
							setBreakpoint(JDIDebugModel.createMethodBreakpoint(resource, typeName, methodName, methodSignature, true, false, false, -1, -1, -1, 0, true, new HashMap(10)));
						}
					}
				} else {
					if (!enableForMember(member)) {
						report(ActionMessages.getString("ManageMethodBreakpointActionDelegate.CantAdd")); //$NON-NLS-1$
						return;
					}
					IMethod method= (IMethod)member;
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
					
					
					setBreakpoint(JDIDebugModel.createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method), 
						type.getFullyQualifiedName(), methodName, methodSignature, true, false, false, -1, start, end, 0, true, attributes));
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x);
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_creating_breakpoint_7"), x.getMessage()); //$NON-NLS-1$
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x);
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_removing_breakpoint_8"), x.getMessage()); //$NON-NLS-1$
			}
		}
	}

	public static String resolveMethodSignature(IType type, String methodSignature) throws JavaModelException {
		String[] parameterTypes= Signature.getParameterTypes(methodSignature);
		
		StringBuffer resolvedSig= new StringBuffer("("); //$NON-NLS-1$
		for (int i = 0; i < parameterTypes.length; i++) {
			String parameterType = parameterTypes[i];
			if (parameterType.length() > 1) {
				if (!generateQualifiedName(type, resolvedSig, parameterType)) {
					return null;
				}
				resolvedSig.append(';');
			} else {
				resolvedSig.append(parameterType);
			}
		}
		resolvedSig.append(')');
		String returnType= Signature.getReturnType(methodSignature);
		if (returnType.length() > 1) {
			if (!generateQualifiedName(type, resolvedSig, returnType)) {
				return null;
			}
			resolvedSig.append(';');
		} else {
			resolvedSig.append(returnType);
		}
		methodSignature= resolvedSig.toString();
		return methodSignature;
	}

	protected static boolean generateQualifiedName(IType type, StringBuffer resolvedSig, String typeName) throws JavaModelException {
		int count= Signature.getArrayCount(typeName);
		typeName= Signature.getElementType(typeName.substring(1 + count, typeName.length() - 1));
		String[][] resolvedType= type.resolveType(typeName);
		if (resolvedType != null && resolvedType.length == 1) {
			String[] typeNames= resolvedType[0];
			String qualifiedName= Signature.toQualifiedName(typeNames);
			if (qualifiedName.startsWith(".")) { //$NON-NLS-1$
				// remove leading "."
				qualifiedName = qualifiedName.substring(1);
			}
			for (int j = 0; j < count; j++) {
				resolvedSig.append('[');
			}
			resolvedSig.append(Signature.C_RESOLVED);
			resolvedSig.append(qualifiedName.replace('.', '/'));	
			return true;
		} else {
			return false;
		}
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
	
	protected void setEnabledState(ITextEditor editor) {
		if (getAction() != null && getPage() != null) {
			IWorkbenchPart part = getPage().getActivePart();
			if (part == null) {
				getAction().setEnabled(false);
			} else {
				if (part == getPage().getActiveEditor()) {
					if (getPage().getActiveEditor() instanceof ITextEditor) {
						super.setEnabledState((ITextEditor)getPage().getActiveEditor());
					} else {
						getAction().setEnabled(false);
					}
				}
			}
		}	
	}
}
