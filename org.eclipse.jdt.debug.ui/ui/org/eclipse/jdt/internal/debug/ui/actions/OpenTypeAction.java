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


import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

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
				doAction(element);
			}
		} catch(DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
	
	protected abstract IDebugElement getDebugElement(IAdaptable element);
	
	protected abstract String getTypeNameToOpen(IDebugElement element) throws DebugException;
	
	protected void doAction(Object e) throws DebugException {
		IAdaptable element= (IAdaptable) e;
		IDebugElement dbgElement= getDebugElement(element);
		if (dbgElement != null) {
			Object sourceElement= getSourceElement(dbgElement);
			if (sourceElement == null) {
				try {
					//resort to looking through the workspace projects for the
					//type as the source locators failed.
					String typeName= getTypeNameToOpen(dbgElement);
					sourceElement= findTypeInWorkspace(typeName);
				} catch (CoreException x) {
					JDIDebugUIPlugin.log(x);
				}
			}
			if (sourceElement != null) {
				openInEditor(sourceElement);
			}
		}
	}

	protected void openInEditor(Object sourceElement) {
		try {
			IEditorPart part= EditorUtility.openInEditor(sourceElement);
			if (part != null && sourceElement instanceof IJavaElement) {
				EditorUtility.revealInEditor(part, ((IJavaElement)sourceElement));
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
	}
	
	/**
	 * Searches for and returns a type with the given name in the workspace,
	 * or <code>null</code> if none.
	 * 
	 * @param typeName fully qualified type name
	 * @return type or <code>null</code>
	 * @throws JavaModelException
	 */
	public static IType findTypeInWorkspace(String typeName) throws JavaModelException {
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
	private static IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
		
		String pathStr= fullyQualifiedName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement jelement= jproject.findElement(new Path(pathStr));
		if (jelement == null) {
			// try to find it as inner type
			String qualifier= Signature.getQualifier(fullyQualifiedName);
			if (qualifier.length() > 0) {
				IType type= findType(jproject, qualifier); // recursive!
				if (type != null) {
					IType res= type.getType(Signature.getSimpleName(fullyQualifiedName));
					if (res.exists()) {
						return res;
					}
				}
			}
		} else if (jelement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(fullyQualifiedName);
			return ((ICompilationUnit) jelement).getType(simpleName);
		} else if (jelement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) jelement).getType();
		}
		return null;
	}
	
	/**
	 * Use the source locator to determine the correct source element
	 */
	protected Object getSourceElement(Object e) {
		if (e instanceof IDebugElement) {
			IDebugElement de= (IDebugElement)e;
			String typeName = null;
			try {
				typeName = getTypeNameToOpen(de);
				if (typeName != null) {
					List list = ToggleBreakpointAdapter.searchForTypes(typeName, de.getLaunch());
					if (!list.isEmpty()) {
						return list.get(0);
					}
				}
			} catch (CoreException ex) {
				JDIDebugUIPlugin.errorDialog(ActionMessages.OpenTypeAction_2, ex.getStatus()); //$NON-NLS-1$
			}	
		}
		return null;
	}
	
	/**
	 * Returns the source element for the given type using the specified
	 * source locator, or <code>null</code> if none.
	 * 
	 * @param typeName fully qualified type name
	 * @param locator source locator
	 * @return the source element for the given type using the specified
	 * source locator, or <code>null</code> if none
	 */
	public static Object findSourceElement(String typeName, ISourceLocator sourceLocator) {
		
		if (sourceLocator instanceof ISourceLookupDirector) {
			ISourceLookupDirector director = (ISourceLookupDirector)sourceLocator;
			String fileName = typeName.replace('.', File.separatorChar);
			fileName = fileName + ".java"; //$NON-NLS-1$
			Object object = director.getSourceElement(fileName);
			if (object != null) {
				// return the java element adapter if it exists
				if (object instanceof IAdaptable) {
					IJavaElement element = (IJavaElement) ((IAdaptable)object).getAdapter(IJavaElement.class);
					if (element != null) {
						return element;
					}
				}
				return object;
			}
		}
		// still support deprecated source locators for 'open type'
		IJavaSourceLocation[] locations= null;
		if (sourceLocator instanceof JavaUISourceLocator) {
			JavaUISourceLocator javaSourceLocator= (JavaUISourceLocator)sourceLocator;
			locations= javaSourceLocator.getSourceLocations();
		} else if (sourceLocator instanceof JavaSourceLocator) {
			JavaSourceLocator javaSourceLocator= (JavaSourceLocator)sourceLocator;
			locations= javaSourceLocator.getSourceLocations();
		}
		if (locations != null) {
			for (int i = 0; i < locations.length; i++) {
				IJavaSourceLocation location = locations[i];
				Object sourceElement = null;
				try {
					sourceElement = location.findSourceElement(typeName);
				} catch (CoreException e) {
				}
				if (sourceElement != null) {
					return sourceElement;
				}
			}
		}
		return null;		
	}
}
