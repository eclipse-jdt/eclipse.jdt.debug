package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Iterator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public abstract class OpenTypeAction extends ObjectActionDelegate {
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}
		Iterator enum= selection.iterator();
		try {
			while (enum.hasNext()) {
				Object element= enum.next();
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
	
	protected IType findTypeInWorkspace(String typeName) throws JavaModelException {
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
	private IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
		
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
	 * Use the source locators to determine the correct source element
	 */
	protected Object getSourceElement(Object e) {
		if (e instanceof IDebugElement) {
			IDebugElement de= (IDebugElement)e;
			ISourceLocator sourceLocator= de.getLaunch().getSourceLocator();
			IAdaptable element= (IAdaptable)de;
		
			IStackFrame stackFrame= (IStackFrame)element.getAdapter(IJavaStackFrame.class);
			if (stackFrame != null) {
				return sourceLocator.getSourceElement(stackFrame);
			} 
			IJavaSourceLocation[] locations= null;
			if (sourceLocator instanceof JavaUISourceLocator) {
				JavaUISourceLocator javaSourceLocator= (JavaUISourceLocator)sourceLocator;
				locations= javaSourceLocator.getSourceLocations();
			} else if (sourceLocator instanceof JavaSourceLocator) {
				JavaSourceLocator javaSourceLocator= (JavaSourceLocator)sourceLocator;
				locations= javaSourceLocator.getSourceLocations();
			}
			if (locations != null) {
				try {
					String typeName= getTypeNameToOpen(de);
					for (int i = 0; i < locations.length; i++) {
						IJavaSourceLocation location = locations[i];
						Object sourceElement= location.findSourceElement(typeName);
						if (sourceElement != null) {
							return sourceElement;
						}
					}
				} catch (CoreException ex) {
					JDIDebugUIPlugin.log(ex);
				}
			}
		}
		return null;
	}
}
