/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class LaunchConfigurationProjectMainTypeChange extends Change {
	
	
	private ILaunchConfiguration fLaunchConfiguration;
	private String fNewMainTypeName;
	private String fNewProjectName;
	private String fNewLaunchConfigurationName;
	private String fOldMainTypeName;
	private String fOldProjectName;
	private ILaunchConfigurationWorkingCopy fNewLaunchConfiguration;
    private String fNewConfigContainerName;

	/**
	 * Create a change for each launch configuration which needs to be updated for this IType rename.
	 */
	public static Change createChangesForTypeRename(IType type, String newName) throws CoreException {
		IType declaringType= type.getDeclaringType();
		String newFullyQualifiedName;
		if (declaringType == null) {
			IPackageFragment packageFragment = type.getPackageFragment();
			if (packageFragment.isDefaultPackage()) {
				newFullyQualifiedName= newName;
			} else {
				newFullyQualifiedName= packageFragment.getElementName() + '.' + newName;
			}
		} else {
			newFullyQualifiedName= declaringType.getFullyQualifiedName() + '$' + newName;
		}
		return createChangesForTypeChange(type, newFullyQualifiedName, null);
	}
	
	/**
	 * Create a change for each launch configuration which needs to be updated for this IType move.
	 */
	public static Change createChangesForTypeMove(IType type, IJavaElement destination) throws CoreException {
		IJavaProject projectDestination= destination.getJavaProject();
		String newProjectName;
		if (type.getJavaProject().equals(projectDestination)) {
			newProjectName= null;
		} else {
			newProjectName= projectDestination.getElementName();
		}
		String newFullyQualifiedName;
		if (destination instanceof IType) {
			newFullyQualifiedName= ((IType)destination).getFullyQualifiedName() + '$' + type.getElementName();
		} else if (destination instanceof IPackageFragment) {
			IPackageFragment destinationPackage= (IPackageFragment) destination;
			if (destinationPackage.isDefaultPackage()) {
				newFullyQualifiedName= type.getElementName();
			} else {
				newFullyQualifiedName= destination.getElementName() + '.' + type.getElementName();
			}
		} else {
			return null;
		}
		return createChangesForTypeChange(type, newFullyQualifiedName, newProjectName);
	}

	/**
	 * Create a change for each launch configuration which needs to be updated for this IJavaProject rename.
	 */
	public static Change createChangesForProjectRename(IJavaProject javaProject, String newProjectName) throws CoreException {
		String projectName= javaProject.getElementName();
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration[] configs= manager.getLaunchConfigurations(configurationType);
		List changes= createChangesForProjectRename(javaProject, configs, projectName, newProjectName);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(createChangesForProjectRename(javaProject, configs, projectName, newProjectName));
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); //$NON-NLS-1$
	}
	
	/**
	 * Create a change for each launch configuration which needs to be updated for this IPackageFragment rename.
	 */
	public static Change createChangesForPackageRename(IPackageFragment packageFragment, String newName) throws CoreException {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		List changes= createChangesForPackageRename(configs, packageFragment, newName);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(createChangesForPackageRename(configs, packageFragment, newName));
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); //$NON-NLS-1$
	}
	
	/**
	 * Create a change for each launch configuration which needs to be updated for this IPackageFragment move.
	 */
	public static Change createChangesForPackageMove(IPackageFragment packageFragment, IPackageFragmentRoot destination) throws CoreException {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		List changes= createChangesForPackageMove(configs, packageFragment, destination);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(createChangesForPackageMove(configs, packageFragment, destination));
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); //$NON-NLS-1$
	}

	/**
	 * Create a change for each launch configuration which needs to be updated for this IType change.
	 */
	private static Change createChangesForTypeChange(IType type, String newFullyQualifiedName, String newProjectName) throws CoreException {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		List changes= createChangesForTypeChange(configs, type, newFullyQualifiedName, newProjectName);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(createChangesForTypeChange(configs, type, newFullyQualifiedName, newProjectName));
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.LaunchConfigurationProjectMainTypeChange_7); //$NON-NLS-1$
	}

	/**
	 * Create a change for each launch configuration from the given list which needs
	 * to be updated for this IType change.
	 */
	private static List createChangesForTypeChange(ILaunchConfiguration[] configs, IType type, String newFullyQualifiedName, String newProjectName) throws CoreException {
		List changes= new ArrayList();
		String typeName= type.getFullyQualifiedName();
		String projectName= type.getJavaProject().getElementName();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String lcProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName.equals(lcProjectName)) {
				String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
				if (mainTypeName.startsWith(typeName)) {
					if (typeName.equals(mainTypeName)) {
						changes.add(new LaunchConfigurationProjectMainTypeChange(launchConfiguration, newFullyQualifiedName, newProjectName));
					} else {
						Change change= createChangesForOuterTypeChange(launchConfiguration, type, newFullyQualifiedName, newProjectName);
						if (change != null) {
							changes.add(change);
						}
					}
				}
			}
		}
		return changes;
	}
	
	/**
	 * Return a change for the given launch configuration if the launch configuration needs to
	 * be updated for this IType change. It specificaly look if the main type of the launch configuration
	 * is an inner type of the given IType.
	 */
	private static Change createChangesForOuterTypeChange(ILaunchConfiguration launchConfiguration, IType type, String newFullyQualifiedName, String newProjectName) throws CoreException {
		IType[] innerTypes= type.getTypes();
		String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		for (int i= 0; i < innerTypes.length; i++) {
			IType innerType= innerTypes[i];
			String innerTypeName= innerType.getFullyQualifiedName();
			if (mainTypeName.startsWith(innerTypeName)) {
				String newTypeName= newFullyQualifiedName + '$' + innerType.getElementName();
				// if it matches, check the type
				if (innerTypeName.equals(mainTypeName)) {
					return new LaunchConfigurationProjectMainTypeChange(launchConfiguration, newTypeName, newProjectName);
				}
				// if it's not the type, check the inner types
				return createChangesForOuterTypeChange(launchConfiguration, innerType, newTypeName, newProjectName);
			}
		}
		return null;
	}
	
	/**
	 * Create a change for each launch configuration from the given list which needs 
	 * to be updated for this IJavaProject rename.
	 */
	private static List createChangesForProjectRename(IJavaProject javaProject, ILaunchConfiguration[] configs, String projectName, String newProjectName) throws CoreException {
		List changes= new ArrayList();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String launchConfigurationProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName.equals(launchConfigurationProjectName)) {
				LaunchConfigurationProjectMainTypeChange change = new LaunchConfigurationProjectMainTypeChange(launchConfiguration, null, newProjectName);
                String newContainerName = computeNewContainerName(javaProject, launchConfiguration);
                if (newContainerName != null) {
                    change.setNewContainerName(newContainerName);
                }

				changes.add(change);
			}
		}
		return changes;
	}

    private void setNewContainerName(String newContainerName) {
        fNewConfigContainerName = newContainerName;
    }

    private static String computeNewContainerName(IJavaProject javaProject, ILaunchConfiguration launchConfiguration) {
        IPath currentLocation = launchConfiguration.getLocation();
        IProject project = javaProject.getProject();
        IPath projectLocation = project.getLocation();
        if (projectLocation.isPrefixOf(currentLocation)) {
            String projectFile = new File(projectLocation.toOSString()).getAbsolutePath();
            String configDir = new File(currentLocation.toOSString()).getParent();
            return new String(configDir.substring(projectFile.length()));
        }
        return null;
    }
    
	/**
	 * Create a change for each launch configuration from the given list which needs 
	 * to be updated for this IPackageFragment rename.
	 */
	private static List createChangesForPackageRename(ILaunchConfiguration[] configs, IPackageFragment packageFragment, String newName) throws CoreException {
		List changes= new ArrayList();
		String packageFragmentName= packageFragment.getElementName();
		String projectName= packageFragment.getJavaProject().getElementName();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String lcProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName.equals(lcProjectName)) {
				String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
				String packageName;
				int index= mainTypeName.lastIndexOf('.');
				if (index < 0) {
					packageName= ""; //$NON-NLS-1$
				} else {
					packageName= mainTypeName.substring(0, index);
				}
				if (packageFragmentName.equals(packageName)) {
					String newTypeName= newName + '.' + mainTypeName.substring(index + 1);
					changes.add(new LaunchConfigurationProjectMainTypeChange(launchConfiguration, newTypeName, null));
				}
			}
		}
		return changes;
	}
	
	/**
	 * Create a change for each launch configuration from the given list which needs 
	 * to be updated for this IPackageFragment move.
	 */
	private static List createChangesForPackageMove(ILaunchConfiguration[] configs, IPackageFragment packageFragment, IPackageFragmentRoot destination) throws CoreException {
		List changes= new ArrayList();
		String packageFragmentName= packageFragment.getElementName();
		String projectName= packageFragment.getJavaProject().getElementName();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String lcProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName.equals(lcProjectName)) {
				String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
				String packageName;
				int index= mainTypeName.lastIndexOf('.');
				if (index < 0) {
					packageName= ""; //$NON-NLS-1$
				} else {
					packageName= mainTypeName.substring(0, index);
				}
				if (packageFragmentName.equals(packageName)) {
					changes.add(new LaunchConfigurationProjectMainTypeChange(launchConfiguration, null, destination.getJavaProject().getElementName()));
				}
			}
		}
		return changes;
	}

	/**
	 * LaunchConfigurationProjectMainTypeChange constructor.
	 * @param launchConfiguration the launch configuration to modify
	 * @param newMainTypeName the name of the new main type, or <code>null</code> if not modified.
	 * @param newProjectName the name of the project, or <code>null</code> if not modified.
	 */
	private LaunchConfigurationProjectMainTypeChange(ILaunchConfiguration launchConfiguration, String newMainTypeName, String newProjectName) throws CoreException {
		fLaunchConfiguration= launchConfiguration;
		fNewLaunchConfiguration= launchConfiguration.getWorkingCopy();
		fNewMainTypeName= newMainTypeName;
        fNewProjectName= newProjectName;

		fOldMainTypeName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
		fOldProjectName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		if (fNewMainTypeName != null) {
			// generate the new configuration name
			String oldName= Signature.getSimpleName(fOldMainTypeName);
			String newName= Signature.getSimpleName(fNewMainTypeName);
			String launchConfigurationName= fLaunchConfiguration.getName();
			fNewLaunchConfigurationName= launchConfigurationName.replaceAll(oldName, newName);
			if (launchConfigurationName.equals(fNewLaunchConfigurationName) || DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(fNewLaunchConfigurationName)) {
				fNewLaunchConfigurationName= null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		if (fNewLaunchConfigurationName != null) {
			return MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_0, new String[] {fLaunchConfiguration.getName(), fNewLaunchConfigurationName}); //$NON-NLS-1$
		} 
		if (fNewProjectName == null) {
			return MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_1, new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
		}
		if (fNewMainTypeName == null) {
			return MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_2, new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
		}
		return MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_3, new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (fLaunchConfiguration.exists()) {
			String typeName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (fOldMainTypeName.equals(typeName)) {
				String projectName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				if (fOldProjectName.equals(projectName)) {
					return new RefactoringStatus();
				}
				return RefactoringStatus.createWarningStatus(MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_4, new String[] {fLaunchConfiguration.getName(), fOldProjectName})); //$NON-NLS-1$
			}
			return RefactoringStatus.createWarningStatus(MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_5, new String[] {fLaunchConfiguration.getName(), fOldMainTypeName})); //$NON-NLS-1$
		} 
		return RefactoringStatus.createFatalErrorStatus(MessageFormat.format(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_6, new String[] {fLaunchConfiguration.getName()})); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {        
        if (fNewConfigContainerName != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            IProject project = root.getProject(fNewProjectName);
            IContainer container = (IContainer) project.findMember(fNewConfigContainerName);
            fNewLaunchConfiguration.setContainer(container);
        }

		String oldMainTypeName;
		String oldProjectName;
		if (fNewMainTypeName != null) {
			oldMainTypeName= fOldMainTypeName;
			fNewLaunchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fNewMainTypeName);
		} else {
			oldMainTypeName= null;
		}
		if (fNewProjectName != null) {
			oldProjectName= fOldProjectName;
			fNewLaunchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fNewProjectName);
		} else {
			oldProjectName= null;
		}
		if (fNewLaunchConfigurationName != null) {
			fNewLaunchConfiguration.rename(fNewLaunchConfigurationName);
		}

		fNewLaunchConfiguration.doSave();

		// create the undo change
		return new LaunchConfigurationProjectMainTypeChange(fNewLaunchConfiguration, oldMainTypeName, oldProjectName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return fLaunchConfiguration;
	}
}
