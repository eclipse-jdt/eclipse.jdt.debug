/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
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
	private InstalledJREsBlock fJREBlock;
		
	/**
	 * Image
	 */
	private Image fImage;
	
	/**
	 * Constructs a new page.
	 */
	public JREContainerWizardPage() {
		super(JREMessages.getString("JREContainerWizardPage.JRE_System_Library_1")); //$NON-NLS-1$
	}

	/**
	 * @see IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		JREsUpdater updater = new JREsUpdater(getShell());
		IPath path = new Path(JavaRuntime.JRE_CONTAINER);
		IVMInstall vm = fJREBlock.getCheckedJRE();
		if (vm != null && !vm.equals(JavaRuntime.getDefaultVMInstall())) {
			path = path.append(vm.getVMInstallType().getId());
			path = path.append(vm.getName());
		}
		// update Installed JREs as required
		updater.updateJRESettings(fJREBlock.getJREs(), JavaRuntime.getDefaultVMInstall());
		// save table settings
		fJREBlock.saveColumnSettings(JDIDebugUIPlugin.getDefault().getDialogSettings(), getClass().getName());
		if (vm == null) {
			fSelection = null;	
		} else {
			fSelection = JavaCore.newContainerEntry(path);
		}		
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
				} else {
					fJREBlock.setCheckedJRE(JavaRuntime.getDefaultVMInstall());
					return;
				}
			}
			IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
			for (int i = 0; i < types.length; i++) {
				IVMInstallType type = types[i];
				if (type.getId().equals(typeId)) {
					IVMInstall[] installs = type.getVMInstalls();
					for (int j = 0; j < installs.length; j++) {
						IVMInstall install = installs[j];
						if (install.getName().equals(name)) {
							fJREBlock.setCheckedJRE(install);
							return;
						}
					}
					return;
				}
			}
		}
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		fJREBlock = new InstalledJREsBlock();
		fJREBlock.createControl(parent);
		setControl(fJREBlock.getControl());
		fJREBlock.restoreColumnSettings(JDIDebugUIPlugin.getDefault().getDialogSettings(), getClass().getName());
		
		// fill with JREs
		List standins = new ArrayList();
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				standins.add(new VMStandin(install));
			}
		}
		fJREBlock.setJREs((IVMInstall[])standins.toArray(new IVMInstall[standins.size()]));
		
		setTitle(JREMessages.getString("JREContainerWizardPage.JRE_System_Library_1")); //$NON-NLS-1$
		setMessage(JREMessages.getString("JREContainerWizardPage.Select_the_JRE_used_to_build_this_project._4")); //$NON-NLS-1$
				
		initializeFromSelection();
	}

	/**
	 * @see IDialogPage#getImage()
	 */
	public Image getImage() {
		if (fImage == null) {
			fImage = JavaDebugImages.DESC_WIZBAN_LIBRARY.createImage();
		}
		return fImage;
	}


	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fImage != null) {
			fImage.dispose();
		}
	}


}
