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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.corext.util.TypeInfoRequestor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ManageWatchpointActionDelegate extends AbstractManageBreakpointActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		updateForRun();
		report(null);
		try {
			IMember[] elements= getMembers();
			if (elements == null || elements.length == 0) {
				ITextSelection textSelection= getTextSelection();
				if (textSelection != null) {
					CompilationUnit compilationUnit= parseCompilationUnit();
					if (compilationUnit != null) {
						BreakpointFieldLocator locator= new BreakpointFieldLocator(textSelection.getOffset());
						compilationUnit.accept(locator);
						String fieldName= locator.getFieldName();
						if (fieldName == null) {
							report(ActionMessages.getString("ManageWatchpointActionDelegate.CantAdd")); //$NON-NLS-1$
							return;
						}
						String typeName= locator.getTypeName();
						// check if the watchpoint already exists. If yes, remove it
						IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
						IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
						for (int i= 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint= breakpoints[i];
							if (breakpoint instanceof IJavaWatchpoint) {
								IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
								if (typeName.equals(watchpoint.getTypeName()) && fieldName.equals(watchpoint.getFieldName())) {
									breakpointManager.removeBreakpoint(watchpoint, true);
									return;
								}
							}
						}
						// add the watchpoint
						JDIDebugModel.createWatchpoint(getResource(), typeName, fieldName, -1, -1, -1, 0, true, new HashMap(10));
					}
				}
			} else {
				// check if all elements support watchpoint
				for (int i= 0, length= elements.length; i < length; i++) {
					if (!enableForMember(elements[i])) {
						report(ActionMessages.getString("ManageWatchpointActionDelegate.CantAdd")); //$NON-NLS-1$
						return;
					}
				}
				// add or remove watchpoint
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				for (int i= 0, length= elements.length; i < length; i++) {
					IField element= (IField)elements[i];
					IJavaBreakpoint breakpoint= getBreakpoint(element);
					if (breakpoint == null) {
						IType type = element.getDeclaringType();
						int start = -1;
						int end = -1;
						ISourceRange range = element.getNameRange();
						if (range != null) {
							start = range.getOffset();
							end = start + range.getLength();
						}
						Map attributes = new HashMap(10);
						BreakpointUtils.addJavaBreakpointAttributes(attributes, element);
						JDIDebugModel.createWatchpoint(BreakpointUtils.getBreakpointResource(type), type.getFullyQualifiedName(), element.getElementName(), -1, start, end, 0, true, attributes);
					} else {
						// remove breakpoint
						try {
							breakpointManager.removeBreakpoint(breakpoint, true);
						} catch (CoreException x) {
							JDIDebugUIPlugin.log(x);
							MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_removing_watchpoint_8"), x.getMessage()); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_adding_watchpoint_7"), ActionMessages.getString("ManageWatchpointAction.The_selected_field_is_not_visible_in_the_currently_selected_debug_context._A_stack_frame_or_suspended_thread_which_contains_the_declaring_type_of_this_field_must_be_selected_1")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException x) {
			JDIDebugUIPlugin.log(x);
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_adding_watchpoint_7"), x.getMessage()); //$NON-NLS-1$
		}
	}
	
	protected IJavaBreakpoint getBreakpoint(IMember selectedField) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaWatchpoint) {
				try {
					if (equalFields(selectedField, (IJavaWatchpoint)breakpoint))
						return (IJavaBreakpoint)breakpoint;
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}

	/**
	 * Compare two fields. The default <code>equals()</code>
	 * method for <code>IField</code> doesn't give the comparison desired.
	 */
	private boolean equalFields(IMember breakpointField, IJavaWatchpoint watchpoint) throws CoreException {
		return (breakpointField.getElementName().equals(watchpoint.getFieldName()) &&
		breakpointField.getDeclaringType().getFullyQualifiedName().equals(watchpoint.getTypeName()));
	}
	
	protected IMember[] getMembers(ISelection s) {
		if (s instanceof IStructuredSelection) {
			ArrayList members= new ArrayList();
			for (Iterator iter= ((IStructuredSelection) s).iterator(); iter.hasNext();) {
				Object o=  iter.next();
				if (o instanceof IField) {
					members.add(o);
				} else if (o instanceof IJavaFieldVariable) {
					IField field= getField((IJavaFieldVariable) o);
					if (field != null) {
						members.add(field);
					}
				}
			}
			return (IMember[])members.toArray(new IMember[0]);
		} 
		
		return null;
	}
	
	/**
	 * Return the associated IField (Java model) for the given
	 * IJavaFieldVariable (JDI model)
	 */
	private IField getField(IJavaFieldVariable variable) {
		String varName= null;
		try {
			varName= variable.getName();
		} catch (DebugException x) {
			JDIDebugUIPlugin.log(x);
			return null;
		}
		IField field;
		List types= searchForDeclaringType(variable);
		Iterator iter= types.iterator();
		while (iter.hasNext()) {
			IType type= (IType)iter.next();
			field= type.getField(varName);
			if (field.exists()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of matching types (IType - Java model) that correspond to the 
	 * declaring type (ReferenceType - JDI model) of the given variable.
	 */
	protected static List searchForDeclaringType(IJavaFieldVariable variable) {
		List types= new ArrayList();
		ILaunch launch = variable.getDebugTarget().getLaunch();
		if (launch == null) {
			return types;
		}
		
		ILaunchConfiguration configuration= launch.getLaunchConfiguration();
		IJavaProject[] javaProjects = null;
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (configuration != null) {
			// Launch configuration support
			try {
				String projectName= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
				if (projectName.length() != 0) {
					javaProjects= new IJavaProject[] {JavaCore.create(workspace.getRoot().getProject(projectName))};
				} else {
					IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
					IProject project;
					List projectList= new ArrayList();
					for (int i= 0, numProjects= projects.length; i < numProjects; i++) {
						project= projects[i];
						if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
							projectList.add(JavaCore.create(project));
						}
					}
					javaProjects= new IJavaProject[projectList.size()];
					projectList.toArray(javaProjects);
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		if (javaProjects == null) {
			return types;
		}

		SearchEngine engine= new SearchEngine();
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(javaProjects, true);
		String declaringType= null;
		try {
			declaringType= variable.getDeclaringType().getName();
		} catch (DebugException x) {
			JDIDebugUIPlugin.log(x);
			return types;
		}
		ArrayList typeRefsFound= new ArrayList(3);
		ITypeNameRequestor requestor= new TypeInfoRequestor(typeRefsFound);
		try {
			engine.searchAllTypeNames(workspace, 
				getPackage(declaringType), 
				getTypeName(declaringType), 
				IJavaSearchConstants.EXACT_MATCH, 
				true, 
				IJavaSearchConstants.CLASS, 
				scope, 
				requestor, 
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		} catch (JavaModelException x) {
			JDIDebugUIPlugin.log(x);
			return types;
		}
		Iterator iter= typeRefsFound.iterator();
		TypeInfo typeInfo= null;
		while (iter.hasNext()) {
			typeInfo= (TypeInfo)iter.next();
			try {
				types.add(typeInfo.resolveType(scope));
			} catch (JavaModelException jme) {
				JDIDebugUIPlugin.log(jme);
			}
		}
		return types;
	}
	
	/**
	 * Returns the package name of the given fully qualified type name.
	 * The package name is assumed to be the dot-separated prefix of the 
	 * type name.
	 */
	protected static char[] getPackage(String fullyQualifiedName) {
		int index= fullyQualifiedName.lastIndexOf('.');
		if (index == -1) {
			return new char[0];
		}
		return fullyQualifiedName.substring(0, index).toCharArray();
	}
	
	/**
	 * Returns a simple type name from the given fully qualified type name.
	 * The type name is assumed to be the last contiguous segment of the 
	 * fullyQualifiedName not containing a '.' or '$'
	 */
	protected static char[] getTypeName(String fullyQualifiedName) {
		int index= fullyQualifiedName.lastIndexOf('.');
		String typeName= fullyQualifiedName.substring(index + 1);
		int lastInnerClass= typeName.lastIndexOf('$');
		if (lastInnerClass != -1) {
			typeName= typeName.substring(lastInnerClass + 1);
		}
		return typeName.toCharArray();
	}
	
	/**
	 * @see AbstractManageBreakpointActionDelegate#enableForMember(IMember)
	 */
	protected boolean enableForMember(IMember member) {
		return member instanceof IField;
	}
}

