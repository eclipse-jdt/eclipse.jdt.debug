package org.eclipse.jdt.internal.debug.ui.launcher;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * Extension to allow a user to associate a JRE with a Java project.
 */
public class JREContainerWizardPage extends WizardPage implements IClasspathContainerPage {
	
	/**
	 * The classpath entry to be created.
	 */
	private IClasspathEntry fSelection;
	
	/**
	 * JRE control
	 */
	private JavaJRETab fJRETab;
	
	/**
	 * Fake launch config used with the control.
	 */
	private ILaunchConfigurationWorkingCopy fConfig;

	/**
	 * Constructs a new page.
	 */
	public JREContainerWizardPage() {
		super(LauncherMessages.getString("JREContainerWizardPage.JRE_System_Library_1")); //$NON-NLS-1$
	}

	/**
	 * @see IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		// retrieve selected JRE
		fJRETab.performApply(fConfig);
		String typeId = null;
		String name = null;
		try {
			typeId = fConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			name = fConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JREContainerWizardPage.Unable_to_set_JRE_for_project._2"), e); //$NON-NLS-1$
			return false;
		}
		
		IPath path = new Path(JavaRuntime.JRE_CONTAINER);
		if (typeId != null) {
			path = path.append(typeId);
			path = path.append(name);
		}
		
		fSelection = JavaCore.newContainerEntry(path);
		
		return true;
	}

	/**
	 * @see IClasspathContainerPage#getSelection()
	 */
	public IClasspathEntry getSelection() {
		return fSelection;
	}

	/**
	 * @see IClasspathContainerPage#setSelection(IClasspathEntry)
	 */
	public void setSelection(IClasspathEntry containerEntry) {
		fSelection = containerEntry;
		initializeFromSelection();
		fSelection = null;
	}

	/**
	 * Initlaizes the JRE selection
	 */
	protected void initializeFromSelection() {
		if (getControl() != null) {
			String typeId = null;
			String name = null;		
			if (fSelection != null) {
				IPath path = fSelection.getPath();
				if (path.segmentCount() > 1) {
					typeId = path.segment(1);
					name = path.segment(2);
				}
			}
			fConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, typeId);
			fConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, name);
			fJRETab.initializeFrom(fConfig);
		}
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		fJRETab = new JavaJRETab();
		fJRETab.createControl(parent);
		setControl(fJRETab.getControl());
		setTitle(LauncherMessages.getString("JREContainerWizardPage.JRE_System_Library_1")); //$NON-NLS-1$
		setMessage(LauncherMessages.getString("JREContainerWizardPage.Select_the_JRE_used_to_build_this_project._4")); //$NON-NLS-1$
		
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		try {
			fConfig = type.newInstance(null, "TEMP_CONFIG"); //$NON-NLS-1$
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JREContainerWizardPage.Unable_to_retrieve_existing_JREs_6"), e); //$NON-NLS-1$
			return;
		}
		
		initializeFromSelection();
	}

}
