/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class MemberActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		if (name.equals("MemberActionFilter")) { //$NON-NLS-1$
			if (target instanceof IMember) {
				IMember member = (IMember) target;
				if (value.equals("isAbstract")) { //$NON-NLS-1$
					try {
						return Flags.isAbstract(member.getFlags());
					} catch (JavaModelException e) {
					}
				}
				if (value.equals("isRemote")) { //$NON-NLS-1$
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if(window != null) {
						IWorkbenchPage page = window.getActivePage();
						if(page != null) {
							IEditorPart part = page.getActiveEditor();
							if(part != null) {
								Object adapter = Platform.getAdapterManager().getAdapter(part.getEditorInput(), "org.eclipse.team.core.history.IFileRevision"); //$NON-NLS-1$
					    		return adapter != null;
							}
						}
					}
					//if we cannot get the editor input, assume it is not remote
					return false;
				}
				if(value.equals("isInterface")) { //$NON-NLS-1$
					IType type = null;
					if(member.getElementType() == IJavaElement.TYPE) {
						type = (IType) member;
					}
					else {
						type = member.getDeclaringType();
					}
					try {
						return type != null && type.isInterface();
					} 
					catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}  
				}
				if(value.equals("isConstructor")) { //$NON-NLS-1$
					IMethod method = null;
					if(member.getElementType() == IJavaElement.METHOD) {
						method = (IMethod) member;
						try{
							return method.isConstructor();
						} catch (JavaModelException e) {
							JDIDebugUIPlugin.log(e);
							return false;
						}  
					}
				}
				if(value.equals("isValidField")) { //$NON-NLS-1$
					try {
						int flags = member.getFlags();
						return (member.getElementType() == IJavaElement.FIELD) & (!Flags.isFinal(flags) & !(Flags.isStatic(flags) & Flags.isFinal(flags)));
					} 
					catch (JavaModelException e) {
						JDIDebugUIPlugin.log(e);
						return false;
					}
				}
				if(value.equals("isInstanceRetrievalAvailable")) { //$NON-NLS-1$
					IAdaptable adapt = DebugUITools.getDebugContext();
					if(adapt != null) {
						IDebugTarget adapter = (IDebugTarget) adapt.getAdapter(IDebugTarget.class);
						if(adapter != null && adapter instanceof IJavaDebugTarget) {
							IJavaDebugTarget dtarget = (IJavaDebugTarget) adapter;
							return dtarget.supportsInstanceRetrieval();
						}
					}
					return false;
				}
			}
		}
		return false;
	}
}
