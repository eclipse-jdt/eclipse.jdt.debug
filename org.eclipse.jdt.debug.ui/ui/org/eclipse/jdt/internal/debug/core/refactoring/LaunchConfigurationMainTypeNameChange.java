/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.CompositeChange;
import org.eclipse.jdt.internal.corext.refactoring.base.Change;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.jdt.internal.corext.refactoring.base.IChange;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class LaunchConfigurationMainTypeNameChange extends Change {
	
	public static IChange createChangesFor(IType type, String newName) throws CoreException {
		List changes= new ArrayList();
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		// Java application launch configurations
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		String typeName= type.getFullyQualifiedName();
		createNeededChanges(configs, newName, typeName, changes);
		// Java applet launch configurations
		configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configs= manager.getLaunchConfigurations(configurationType);
		createNeededChanges(configs, newName, typeName, changes);
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} else if (nbChanges == 1) {
			return (IChange) changes.get(0);
		} else {
			return new CompositeChange(RefractoringMessages.getString("LaunchConfigurationMainTypeNameChange.1"), (IChange[])changes.toArray(new IChange[changes.size()])); //$NON-NLS-1$
		}
	}
	
	private static void createNeededChanges(ILaunchConfiguration[] configs, String newName, String typeName, List changes) throws CoreException {
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String mainType= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			if (typeName.equals(mainType)) {
				changes.add(new LaunchConfigurationMainTypeNameChange(launchConfiguration, newName));
			}
		}
	}

	private ILaunchConfiguration fLaunchConfiguration;
	
	private String fOldName;
	
	private String fNewTypeName;
	
	private String fNewLaunchConfigurationName;
	
	private LaunchConfigurationMainTypeNameChange fUndo;

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
	public LaunchConfigurationMainTypeNameChange(ILaunchConfiguration launchConfiguration, String newName) throws CoreException {
		fLaunchConfiguration= launchConfiguration;
		fNewLaunchConfiguration= launchConfiguration;
		// generate the new type name
		if (newName.endsWith(".java")) { //$NON-NLS-1$
			newName= newName.substring(0, newName.length() - 5);
		}
		String current = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
		int index = current.lastIndexOf('.');
		if (index == -1) {
			fNewTypeName = newName;
			fOldName= current;
		} else {
			fNewTypeName = current.substring(0, index + 1) + newName;
			fOldName= current.substring(index + 1);
		}
		// generate the new configuration name
		String launchConfigurationName= fLaunchConfiguration.getName();
		fNewLaunchConfigurationName= launchConfigurationName.replaceAll(fOldName, newName);
		if (launchConfigurationName.equals(fNewLaunchConfigurationName) || DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(fNewLaunchConfigurationName)) {
			fNewLaunchConfigurationName= null;
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getName()
	 */
	public String getName() {
		if (fNewLaunchConfigurationName != null) {
			return MessageFormat.format(RefractoringMessages.getString("LaunchConfigurationMainTypeNameChange.2"), new String[] {fLaunchConfiguration.getName(), fNewLaunchConfigurationName}); //$NON-NLS-1$
		} else {
			return MessageFormat.format(RefractoringMessages.getString("LaunchConfigurationMainTypeNameChange.3"), new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getModifiedLanguageElement()
	 */
	public Object getModifiedLanguageElement() {
		return fLaunchConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#perform(org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void perform(ChangeContext context, IProgressMonitor pm) throws JavaModelException, ChangeAbortException {
		try {
			// update the configuration
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
			fUndo = new LaunchConfigurationMainTypeNameChange(fNewLaunchConfiguration, fOldName);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getUndoChange()
	 */
	public IChange getUndoChange() {
		return fUndo;
	}
}
