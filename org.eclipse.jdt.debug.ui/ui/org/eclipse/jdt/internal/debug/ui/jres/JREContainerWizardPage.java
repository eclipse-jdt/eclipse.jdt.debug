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


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private JREsComboBlock fJREBlock;
		
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
		IPath path = new Path(JavaRuntime.JRE_CONTAINER);
		if (!fJREBlock.isDefaultJRE()) {
			IVMInstall vm = fJREBlock.getJRE();
			if (vm != null && !vm.equals(JavaRuntime.getDefaultVMInstall())) {
				path = path.append(vm.getVMInstallType().getId());
				path = path.append(vm.getName());
			}
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
					fJREBlock.setUseDefaultJRE();
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
							fJREBlock.setJRE(install);
							return;
						}
					}
				}
			}
			fJREBlock.setJRE(null);
		}
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		fJREBlock = new JREsComboBlock();
		fJREBlock.setDefaultJREDescriptor(new BuildJREDescriptor());
		fJREBlock.setTitle(JREMessages.getString("JREContainerWizardPage.3")); //$NON-NLS-1$
		fJREBlock.createControl(composite);
		setControl(composite);	
		
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
