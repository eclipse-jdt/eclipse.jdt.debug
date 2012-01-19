/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 * 
 * provides methods to create refactoring changes
 */
public class JDTDebugRefactoringUtil {
	
	/**
	 * Take a list of Changes, and return a unique Change, a CompositeChange, or null.
	 */
	public static Change createChangeFromList(List<Change> changes, String changeLabel) {
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} 
		else if (nbChanges == 1) {
			return changes.get(0);
		} 
		else {
			return new CompositeChange(changeLabel, changes.toArray(new Change[changes.size()]));
		}
	}
	
	/**
	 * Returns the new container name for the given project and launch configuration
	 * @param javaProject the java to get the new container name for
	 * @param launchConfiguration the associated launch configuration
	 * @return the new container name
	 * @since 3.2
	 */
	protected static String computeNewContainerName(ILaunchConfiguration launchConfiguration) {
        IFile file = launchConfiguration.getFile();
        if (file != null) {
            return file.getParent().getProjectRelativePath().toString();
        }
        return null;
    }
	
	/**
	 * Returns a change for the given launch configuration if the launch configuration needs to
	 * be updated for this IType change. It specifically looks to see if the main type of the launch configuration
	 * is an inner type of the given IType.
	 * @param config the launch configuration
	 * @param type the type to check for
	 * @param newfqname the new fully qualified name
	 * @param pname the new project name
	 * @return the <code>Change</code> for this outer type
	 * @throws CoreException
	 * @since 3.2
	 */
	protected static Change createChangesForOuterTypeChange(ILaunchConfiguration config, IType type, String newfqname, String pname) throws CoreException {
		IType[] innerTypes = type.getTypes();
		if(innerTypes.length == 0) {
			return null;
		}
		Change change = null;
		String mtname = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		for (int i= 0; i < innerTypes.length; i++) {
			String newTypeName = newfqname + '$' + innerTypes[i].getElementName();
			// if it matches, check the type
			if (innerTypes[i].getFullyQualifiedName().equals(mtname)) {
				return new LaunchConfigurationProjectMainTypeChange(config, newTypeName, pname);
			}
			// if it's not the type, check the inner types
			change = createChangesForOuterTypeChange(config, innerTypes[i], newTypeName, pname);
		}
		return change;
	}
	
	/**
	 * Provides a public mechanism for creating the <code>Change</code> for moving a package
	 * @param packageFragment the fragment to move
	 * @param destination the destination to move it to
	 * @return the <code>Change</code> for moving the package
	 * @throws CoreException
	 * @since 3.2
	 */
	public static Change createChangesForPackageMove(IPackageFragment pfragment, IPackageFragmentRoot destination) throws CoreException {
		List<Change> changes = new ArrayList<Change>();
		ILaunchConfiguration[] configs = getJavaTypeLaunchConfigurations(pfragment.getJavaProject().getElementName());
		String mtname = null;
		for (int i= 0; i < configs.length; i++) {
			mtname = configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if(mtname != null) {
				if(mtname.lastIndexOf(pfragment.getElementName()) > -1) {
					changes.add(new LaunchConfigurationProjectMainTypeChange(configs[i], null, destination.getJavaProject().getElementName()));
				}
			}
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); 
	}
	
	/**
	 * Provides a public mechanism for creating the <code>Change</code> for renaming a package
	 * @param packageFragment the fragment to rename
	 * @param newName the new name for the fragment
	 * @return the Change for the renaming
	 * @throws CoreException
	 * @since 3.2
	 */
	public static Change createChangesForPackageRename(IPackageFragment pfragment, String newname) throws CoreException {
		List<Change> changes = new ArrayList<Change>();
		ILaunchConfiguration[] configs = getJavaTypeLaunchConfigurations(pfragment.getJavaProject().getElementName());
		String mtname;
		for (int i= 0; i < configs.length; i++) {
			mtname = configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if(mtname != null) {
				String pkname = ""; //$NON-NLS-1$
				int index = mtname.lastIndexOf('.');
				if(index > 0) {
					pkname = mtname.substring(0, index);
				}
				if (pfragment.getElementName().equals(pkname)) {
					String ntname = newname + '.' + mtname.substring(index + 1);
					changes.add(new LaunchConfigurationProjectMainTypeChange(configs[i], ntname, null));
				}
			}
			else {
				changes.add(new LaunchConfigurationProjectMainTypeChange(configs[i], null, null));
			}
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); 
	}
	
	/**
	 * Provides a public mechanism for creating the <code>Change</code> for renaming a project
	 * @param javaProject the project to rename
	 * @param newProjectName the new name for the project
	 * @return the Change for the project rename
	 * @throws CoreException
	 * @since 3.2
	 */
	public static Change createChangesForProjectRename(IJavaProject project, String newname) throws CoreException {
		List<Change> changes = new ArrayList<Change>();
		ILaunchConfiguration[] configs = getJavaTypeLaunchConfigurations(project.getElementName());
		LaunchConfigurationProjectMainTypeChange change = null;
		for (int i= 0; i < configs.length; i++) {
			change = new LaunchConfigurationProjectMainTypeChange(configs[i], null, newname);
            String newcname = computeNewContainerName(configs[i]);
            if (newcname != null) {
                change.setNewContainerName(newcname);
            }
			changes.add(change);
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); 
	}
	
	/**
	 * Creates a <code>Change</code> for a type change
	 * @param type the type that is changing
	 * @param newfqname the new fully qualified name
	 * @param pname the project name
	 * @return the <code>Change</code> for changing the specified type
	 * @throws CoreException
	 * @since 3.2
	 */
	protected static Change createChangesForTypeChange(IType type, String newfqname, String pname) throws CoreException {
		List<Change> changes = new ArrayList<Change>();
		String typename = type.getFullyQualifiedName();
		ILaunchConfiguration[] configs = getJavaTypeLaunchConfigurations(type.getJavaProject().getElementName());
		String mtname;
		for (int i= 0; i < configs.length; i++) {
			mtname = configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (typename.equals(mtname)) {
				changes.add(new LaunchConfigurationProjectMainTypeChange(configs[i], newfqname, pname));
			} 
			else {
				Change change = createChangesForOuterTypeChange(configs[i], type, newfqname, pname);
				if (change != null) {
					changes.add(change);
				}
			}
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); 
	}
	
	/**
	 * Provides a public mechanism for creating the <code>Change</code> for moving a type
	 * @param type the type being moved
	 * @param destination the destination to move the type to
	 * @return the <code>Change</code> for the type move
	 * @throws CoreException
	 * @since 3.2
	 */
	public static Change createChangesForTypeMove(IType type, IJavaElement destination) throws CoreException {
		IJavaProject pdestination = destination.getJavaProject();
		String newpname = null;
		if (!type.getJavaProject().equals(pdestination)) {
			newpname = pdestination.getElementName();
		}
		String newfqname = type.getElementName();
		if (destination instanceof IType) {
			newfqname = ((IType)destination).getFullyQualifiedName() + '$' + type.getElementName();
		} 
		else if (destination instanceof IPackageFragment) {
			if (!((IPackageFragment) destination).isDefaultPackage()) {
				newfqname = destination.getElementName() + '.' + type.getElementName();
			}
		} 
		return createChangesForTypeChange(type, newfqname, newpname);
	}
	
	/**
	 * Provides a public mechanism for creating the <code>Change</code> for renaming a type
	 * @param type the type to rename
	 * @param newname the new name for the type
	 * @return the <code>Change</code> for the type rename
	 * @throws CoreException
	 * @since 3.2
	 */
	public static Change createChangesForTypeRename(IType type, String newname) throws CoreException {
		IType dtype = type.getDeclaringType();
		String newfqname = newname;
		if (dtype == null) {
			IPackageFragment packageFragment = type.getPackageFragment();
			if (!packageFragment.isDefaultPackage()) {
				newfqname = packageFragment.getElementName() + '.' + newname;
			}
		} 
		else {
			newfqname = dtype.getFullyQualifiedName() + '$' + newname;
		}
		return createChangesForTypeChange(type, newfqname, null);
	}
	
	/**
	 * Returns a listing of configurations that have a specific project name attribute in them
	 * @param pname the project attribute to compare against
	 * @return the list of java type launch configurations that have the specified project attribute
	 * @since 3.2
	 */
	protected static ILaunchConfiguration[] getJavaTypeLaunchConfigurations(String pname) {
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			ArrayList<ILaunchConfiguration> list = new ArrayList<ILaunchConfiguration>();
			String attrib;
			for(int i = 0; i < configs.length; i++) {
				attrib = configs[i].getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				if(attrib != null) {
					if(attrib.equals(pname)) {
						list.add(configs[i]);
					}
				}
			}
			return list.toArray(new ILaunchConfiguration[list.size()]);
		}
		catch(CoreException e) {JDIDebugPlugin.log(e);}
		return new ILaunchConfiguration[0];
	}

}
