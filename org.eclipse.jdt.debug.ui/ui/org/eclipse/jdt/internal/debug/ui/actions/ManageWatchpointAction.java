package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public class ManageWatchpointAction extends ManageBreakpointAction {
	
	/**
	 * Cache of fields that have already been searched for and found
	 * key: The variable to be search on (IJavaVariable)
	 * value: The field that was found (IField)
	 */
	private Map fFoundFields= new HashMap();
	
	public ManageWatchpointAction() {
		super();
		fAddText= ActionMessages.getString("ManageWatchpointAction.Add_&Watchpoint_1"); //$NON-NLS-1$
		fAddDescription= ActionMessages.getString("ManageWatchpointAction.Add_a_watchpoint_2"); //$NON-NLS-1$
		
		fRemoveText= ActionMessages.getString("ManageWatchpointAction.Remove_&Watchpoint_4"); //$NON-NLS-1$
		fRemoveDescription= ActionMessages.getString("ManageWatchpointAction.Remove_a_field_watchpoint_5"); //$NON-NLS-1$
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart part) {
		setAction(action);
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getBreakpoint() == null) {
			try {
				IMember element= getElement();
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
				setBreakpoint(JDIDebugModel.createWatchpoint(BreakpointUtils.getBreakpointResource(type),type.getFullyQualifiedName(), element.getElementName(), -1, start, end, 0, true, attributes));
			} catch (JavaModelException exception) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_adding_watchpoint_7"), "The selected field is not visible in the currently selected debug context. A stack frame or suspended thread which contains the declaring type of this field must be selected."); //$NON-NLS-1$
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_adding_watchpoint_7"), x.getMessage()); //$NON-NLS-1$
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageWatchpointAction.Problems_removing_watchpoint_8"), x.getMessage()); //$NON-NLS-1$
			}
		}
		update();
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
	
	protected IMember getElement(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) s;
			if (ss.size() == 1) {					
				Object o=  ss.getFirstElement();
				if (o instanceof IField) {
					return (IField) o;
				} else if (o instanceof IJavaFieldVariable) {
					return getField((IJavaFieldVariable) o);
				}
			}
		}
		return null;
	}
	
	/**
	 * Return the associated IField (Java model) for the given
	 * IJavaFieldVariable (JDI model)
	 */
	public IField getField(IJavaFieldVariable variable) {
		IField field= (IField)fFoundFields.get(variable);
		if (field != null) {
			return field;
		}
		List types= searchForDeclaringType(variable);
		Iterator iter= types.iterator();
		while (iter.hasNext()) {
			try {
				field= ((IType)iter.next()).getField(variable.getName());
			} catch (DebugException exception) {
			}
			if (field != null) {
				// Return the first java model field that is found which
				// matches the JDI model field.
				fFoundFields.put(variable, field);
				return field;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of matching types (IType - Java model) that correspond to the 
	 * declaring type (ReferenceType - JDI model) of the given variable.
	 */
	protected List searchForDeclaringType(IJavaFieldVariable variable) {
		List types= new ArrayList();
		ILaunch launch = variable.getDebugTarget().getLaunch();
		if (launch == null) {
			return types;
		}
		
		
		ILaunchConfiguration configuration= launch.getLaunchConfiguration();
		IJavaProject javaProject = null;
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (configuration == null) {
			// Old-style launcher support
			Object element = launch.getElement();
			if (element instanceof IJavaElement) {
				javaProject = ((IJavaElement)element).getJavaProject();
			}
		} else {
			// Launch configuration support
			try {
				String projectName= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
				if (projectName != null) {
					javaProject= JavaCore.create(workspace.getRoot().getProject(projectName));
				}
			} catch (CoreException e) {
			}
		}
		if (javaProject == null) {
			return types;
		}

		SearchEngine engine= new SearchEngine();
		IJavaSearchScope scope= engine.createJavaSearchScope(new IJavaProject[] {javaProject}, true);
		String declaringType= null;
		try {
			declaringType= variable.getDeclaringType().getName();
		} catch (DebugException exception) {
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
		} catch (JavaModelException e) {
			return types;
		}
		Iterator iter= typeRefsFound.iterator();
		TypeInfo typeInfo= null;
		while (iter.hasNext()) {
			typeInfo= (TypeInfo)iter.next();
			try {
				types.add(typeInfo.resolveType(scope));
			} catch (JavaModelException jme) {
			}
		}
		return types;
	}
	
	/**
	 * Returns the package name of the given fully qualified type name.
	 * The package name is assumed to be the dot-separated prefix of the 
	 * type name.
	 */
	protected char[] getPackage(String fullyQualifiedName) {
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
	protected char[] getTypeName(String fullyQualifiedName) {
		int index= fullyQualifiedName.lastIndexOf('.');
		String typeName= fullyQualifiedName.substring(index + 1);
		int lastInnerClass= typeName.lastIndexOf('$');
		if (lastInnerClass != -1) {
			typeName= typeName.substring(lastInnerClass + 1);
		}
		return typeName.toCharArray();
	}
	
}

