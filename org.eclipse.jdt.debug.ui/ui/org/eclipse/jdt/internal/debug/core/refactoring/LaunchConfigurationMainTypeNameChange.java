/*******************************************************************************
 * Copyright (c) 2003, 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.jdt.internal.corext.refactoring.CompositeChange;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class LaunchConfigurationMainTypeNameChange extends Change {
	
	public static Change createChangesFor(IType type, String newName) throws CoreException {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// generate the new type name
		if (newName.endsWith(".java")) { //$NON-NLS-1$
			newName= newName.substring(0, newName.length() - 5);
		}
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		String typeName= type.getFullyQualifiedName();
		String projectName= type.getJavaProject().getElementName();
		List changes= changesForITypeChange(configs, newName, typeName, projectName);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(changesForITypeChange(configs, newName, typeName, projectName));
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} else if (nbChanges == 1) {
			return (Change) changes.get(0);
		} else {
			return new CompositeChange(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.1"), (Change[])changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
		}
	}
	
	private static List changesForITypeChange(ILaunchConfiguration[] configs, String newName, String typeName, String projectName) throws CoreException {
		List changes= new ArrayList();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (typeName.equals(mainTypeName)) {
				String lcProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				
				if (projectName.equals(lcProjectName)) {
					int index= mainTypeName.lastIndexOf('.');
					String newTypeName;
					if (index == -1) {
						newTypeName= newName;
					} else {
						newTypeName= mainTypeName.substring(0, index + 1) + newName;
					}
					changes.add(new LaunchConfigurationMainTypeNameChange(launchConfiguration, newTypeName));
				}
			}
		}
		return changes;
	}

	public static Change createChangesFor(IPackageFragment packageFragment, String newName) throws CoreException {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		String packageFragmentName= packageFragment.getElementName();
		String projectName= packageFragment.getJavaProject().getElementName();
		List changes= changesForIPackageFragmentChange(configs, newName, packageFragmentName, projectName);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		changes.addAll(changesForIPackageFragmentChange(configs, newName, packageFragmentName, projectName));
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} else if (nbChanges == 1) {
			return (Change) changes.get(0);
		} else {
			return new CompositeChange(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.1"), (Change[])changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
		}
	}
	
	private static List changesForIPackageFragmentChange(ILaunchConfiguration[] configs, String newName, String packageFragmentName, String projectName) throws CoreException {
		List changes= new ArrayList();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String mainTypeName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (mainTypeName != null && mainTypeName.startsWith(packageFragmentName)) {
				String lcProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				if (projectName.equals(lcProjectName)) {
					String newTypeName= newName + mainTypeName.substring(packageFragmentName.length());
					changes.add(new LaunchConfigurationMainTypeNameChange(launchConfiguration, newTypeName));
				}
			}
		}
		return changes;
	}
	
	private ILaunchConfiguration fLaunchConfiguration;
	
	private String fOldTypeName;
	
	private String fNewTypeName;
	
	private String fNewLaunchConfigurationName;

	/**
	 * New launch configuration object if the launch configuration has been renamed, the original
	 * launch configuration object otherwise.
	 */
	protected ILaunchConfiguration fNewLaunchConfiguration;
	
	/**
	 * launch configuration listener used to get the new ILaunchConfiguration object when a launch
	 * configuration is renamed.
	 */
	private ILaunchConfigurationListener configurationListener= new ILaunchConfigurationListener() {
		public void launchConfigurationAdded(final ILaunchConfiguration launchConfiguration) {
			ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfiguration oldConfig= manager.getMovedFrom(launchConfiguration);
			if (oldConfig != null && oldConfig == fLaunchConfiguration) {
				fNewLaunchConfiguration= launchConfiguration;
			}
		}

		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		}

		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		}
	};

	
	/**
	 * LaunchConfigurationMainTypeNameChange constructor.
	 */
	public LaunchConfigurationMainTypeNameChange(ILaunchConfiguration launchConfiguration, String newTypeName) throws CoreException {
		fLaunchConfiguration= launchConfiguration;
		fNewLaunchConfiguration= launchConfiguration;
		fOldTypeName = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
		fNewTypeName= newTypeName;
		// generate the new configuration name
		String oldName= Signature.getSimpleName(fOldTypeName);
		String newName= Signature.getSimpleName(fNewTypeName);
		String launchConfigurationName= fLaunchConfiguration.getName();
		fNewLaunchConfigurationName= launchConfigurationName.replaceAll(oldName, newName);
		if (launchConfigurationName.equals(fNewLaunchConfigurationName) || DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(fNewLaunchConfigurationName)) {
			fNewLaunchConfigurationName= null;
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getName()
	 */
	public String getName() {
		if (fNewLaunchConfigurationName != null) {
			return MessageFormat.format(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.2"), new String[] {fLaunchConfiguration.getName(), fNewLaunchConfigurationName}); //$NON-NLS-1$
		} else {
			return MessageFormat.format(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.3"), new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getModifiedLanguageElement()
	 */
	public Object getModifiedElement() {
		return fLaunchConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#perform(org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchConfigurationListener(configurationListener);
		ILaunchConfigurationWorkingCopy copy = fLaunchConfiguration.getWorkingCopy();
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fNewTypeName);
		if (fNewLaunchConfigurationName != null) {
			copy.rename(fNewLaunchConfigurationName);
		}
		copy.doSave();
		launchManager.removeLaunchConfigurationListener(configurationListener);
		// create the undo change
		return new LaunchConfigurationMainTypeNameChange(fNewLaunchConfiguration, fOldTypeName);
	}

	public void initializeValidationData(IProgressMonitor pm) throws CoreException {
	}

	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		if (fLaunchConfiguration.exists()) {
			String typeName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (fOldTypeName.equals(typeName)) {
				return new RefactoringStatus();
			} else {
				return RefactoringStatus.createWarningStatus(MessageFormat.format(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.5"), new String[] {fLaunchConfiguration.getName(), fOldTypeName})); //$NON-NLS-1$
			}
		} else {
			return RefactoringStatus.createFatalErrorStatus(MessageFormat.format(RefactoringMessages.getString("LaunchConfigurationMainTypeNameChange.6"), new String[] {fLaunchConfiguration.getName()})); //$NON-NLS-1$
		}
	}
}
