/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
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
	@Override
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}
		Iterator<?> itr= selection.iterator();
		try {
			while (itr.hasNext()) {
				Object element= itr.next();
				if (openElement(action, element)) {
					return;
				}
			}
		} catch(CoreException e) {
			JDIDebugUIPlugin.statusDialog(e.getStatus());
		}
	}

	/**
	 * Open the selected element, return true, if further selections should not be checked.
	 *
	 * @param action
	 *            the action proxy that handles the presentation portion of the action
	 * @param element
	 *            the selected element.
	 * @return true, if no other openElement calls should be made. Used, when multiple element is selected, and the action works as trying until one
	 *         succeeds.
	 * @throws CoreException
	 *             if source element is not found.
	 */
	protected boolean openElement(IAction action, Object element) throws DebugException, CoreException {
		IType sourceElement = resolveSourceElement(element);
		if (sourceElement != null) {
			openInEditor(element, sourceElement);
		} else {
			IStatus status = new Status(IStatus.INFO, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, "Source not found", null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return false;
	}

	protected abstract IDebugElement getDebugElement(IAdaptable element);

	/**
	 * Returns the type to open based on the given selected debug element, or <code>null</code>.
	 *
	 * @param element selected debug element
	 * @return the type to open or <code>null</code> if none
	 */
	protected abstract IJavaType getTypeToOpen(IDebugElement element) throws CoreException;

	/**
	 * Resolves and returns the source element to open or <code>null</code> if none.
	 *
	 * @param e selected element to resolve a source element for
	 * @return the source element to open or <code>null</code> if none
	 */
	protected IType resolveSourceElement(Object e) throws CoreException {
		IType source = null;
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
					source = findTypeInWorkspace(type.getName(), false);
				}
			}
		}
		return source;
	}

	protected void openInEditor(@SuppressWarnings("unused") Object element, IType sourceElement) throws CoreException {
		if (isHierarchy()) {
			OpenTypeHierarchyUtil.open(sourceElement, getWorkbenchWindow());
		} else {
			JavaUI.openInEditor(sourceElement);
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
	 * @param findOnlyUniqueMatch
	 *            if <code>true</code>, this method only returns a type iff
	 *            there's only a single match in the workspace, and
	 *            <code>null</code> otherwise. If <code>false</code>, it returns
	 *            the first match.
	 * @return type or <code>null</code>
	 * @throws CoreException if search failed
	 */
	public static IType findTypeInWorkspace(String typeName, boolean findOnlyUniqueMatch) throws CoreException {
		int dot= typeName.lastIndexOf('.');
		char[][] qualifications;
		String simpleName;
		if (dot != -1) {
			qualifications= new char[][] { typeName.substring(0, dot).toCharArray() };
			simpleName= typeName.substring(dot + 1);
		} else {
			qualifications= null;
			simpleName= typeName;
		}
		char[][] typeNames= new char[][] { simpleName.toCharArray() };

		if (findOnlyUniqueMatch) {
			return findUniqueTypeInWorkspace(qualifications, typeNames);
		}
		return findAnyTypeInWorkspace(qualifications, typeNames);
	}

	private static IType findAnyTypeInWorkspace(char[][] qualifications,
			char[][] typeNames) throws JavaModelException {
		class ResultException extends RuntimeException {
			private static final long serialVersionUID= 1L;
			private final IType fType;
			public ResultException(IType type) {
				fType= type;
			}
		}
		TypeNameMatchRequestor requestor= new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				throw new ResultException(match.getType());
			}
		};
		try {
			new SearchEngine().searchAllTypeNames(qualifications, typeNames, SearchEngine.createWorkspaceScope(), requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (ResultException e) {
			return e.fType;
		}
		return null;
	}

	private static IType findUniqueTypeInWorkspace(char[][] qualifications,
			char[][] typeNames) throws JavaModelException {
		final IType[] result = { null };
		TypeNameMatchRequestor requestor= new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				if (result[0] == null) {
					result[0]= match.getType();
				} else {
					throw new OperationCanceledException();
				}
			}
		};
		try {
			new SearchEngine().searchAllTypeNames(qualifications, typeNames, SearchEngine.createWorkspaceScope(), requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (OperationCanceledException e) {
			return null;
		}
		return result[0];
	}


	protected void typeHierarchyError() {
		showErrorMessage(ActionMessages.ObjectActionDelegate_Unable_to_display_type_hierarchy__The_selected_source_element_is_not_contained_in_the_workspace__1);
	}
}
