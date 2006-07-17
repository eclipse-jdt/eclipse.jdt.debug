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
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.Iterator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class OpenTypeAction extends ObjectActionDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}
		Iterator itr= selection.iterator();
		try {
			while (itr.hasNext()) {
				Object element= itr.next();
				Object sourceElement = resolveSourceElement(element);
				if (sourceElement != null) {
					openInEditor(sourceElement);
				} else {
					IStatus status = new Status(IStatus.INFO, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, ActionMessages.OpenTypeAction_0, null);
					throw new CoreException(status);
				}
			}
		} catch(CoreException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.OpenTypeAction_1, e.getStatus());
		}
	}
	
	protected abstract IDebugElement getDebugElement(IAdaptable element);
	
	/**
	 * Returns the type to open based on the given selected debug element, or <code>null</code>.
	 * 
	 * @param element selected debug element
	 * @return the type to open or <code>null</code> if none
	 * @throws DebugException
	 */
	protected abstract IJavaType getTypeToOpen(IDebugElement element) throws CoreException;
	
	/**
	 * Resolves and returns the source element to open or <code>null</code> if none.
	 * 
	 * @param e selected element to resolve a source element for
	 * @return the source element to open or <code>null</code> if none
	 * @throws CoreException
	 */
	protected Object resolveSourceElement(Object e) throws CoreException {
		Object source = null;
		IAdaptable element= (IAdaptable) e;
		IDebugElement dbgElement= getDebugElement(element);
		if (dbgElement != null) {
			IJavaType type = getTypeToOpen(dbgElement);
			while (type instanceof IJavaArrayType) {
				type = ((IJavaArrayType)type).getComponentType();
			}
			if (type != null) {
				source = JavaDebugUtils.resolveType(type);
				if (source == null) {
					//resort to looking through the workspace projects for the
					//type as the source locators failed.
					source = findTypeInWorkspace(type.getName());
				}
			}
		}
		return source;
	}

	protected void openInEditor(Object sourceElement) throws CoreException {
		if (isHierarchy()) {
			if (sourceElement instanceof IJavaElement) {
				OpenTypeHierarchyUtil.open((IJavaElement)sourceElement, getWorkbenchWindow());
			} else {
				typeHierarchyError();
			}
		} else {
			if(sourceElement instanceof IJavaElement) {
				JavaUI.openInEditor((IJavaElement) sourceElement);
			}
			else {
				showErrorMessage(ActionMessages.OpenTypeAction_2);
			}
		}
	}
	
	/**
	 * Returns whether a type hierarchy should be opened.
	 * 
	 * @return whether a type hierarchy should be opened
	 */
	protected boolean isHierarchy() {
		return false;
	}
	
	/**
	 * Searches for and returns a type with the given name in the workspace,
	 * or <code>null</code> if none.
	 * 
	 * @param typeName fully qualified type name
	 * @return type or <code>null</code>
	 * @throws JavaModelException
	 */
	public static IType findTypeInWorkspace(String typeName) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IJavaProject[] projects= JavaCore.create(root).getJavaProjects();
		for (int i= 0; i < projects.length; i++) {
			IType type= findType(projects[i], typeName);
			if (type != null) {
				return type;
			}
		}
		return null;
	}
	
	/** 
	 * Finds a type by its qualified type name (dot separated).
	 * @param jproject The Java project to search in
	 * @param fullyQualifiedName The fully qualified name (type name with enclosing type names and package (all separated by dots))
	 * @return The type found, or <code>null<code> if no type found
	 * The method does not find inner types. Waiting for a Java Core solution
	 */	
	private static IType findType(IJavaProject jproject, String fullyQualifiedName) throws CoreException {
		IJavaElement jelement= JavaDebugUtils.findElement(fullyQualifiedName, jproject);
		if (jelement instanceof ICompilationUnit) {
			String simpleName= Signature.getSimpleName(fullyQualifiedName);
			return ((ICompilationUnit) jelement).getType(simpleName);
		} else if (jelement instanceof IClassFile) {
			return ((IClassFile) jelement).getType();
		}
		return null;
	}
	
	protected void typeHierarchyError() {
		showErrorMessage(ActionMessages.ObjectActionDelegate_Unable_to_display_type_hierarchy__The_selected_source_element_is_not_contained_in_the_workspace__1); 
	}	
}
