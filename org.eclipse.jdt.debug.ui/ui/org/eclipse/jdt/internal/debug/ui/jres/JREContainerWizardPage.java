/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * Constructs a new page.
	 */
	public JREContainerWizardPage() {
		super(JREMessages.JREContainerWizardPage_JRE_System_Library_1); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		IPath path = new Path(JavaRuntime.JRE_CONTAINER);
		if (!fJREBlock.isDefaultJRE()) {
			IVMInstall vm = fJREBlock.getJRE();
			if (vm != null) {
				path = path.append(vm.getVMInstallType().getId());
				path = path.append(vm.getName());
			}
		}
		fSelection = JavaCore.newContainerEntry(path);		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
	 */
	public IClasspathEntry getSelection() {
		return fSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse.jdt.core.IClasspathEntry)
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
			if (fSelection == null) {
				fJREBlock.setUseDefaultJRE();
				return;
			}
			IPath path = fSelection.getPath();
			if (path.segmentCount() > 1) {
				typeId = path.segment(1);
				name = path.segment(2);
			} else {
				fJREBlock.setUseDefaultJRE();
				return;
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
			fJREBlock.setUseDefaultJRE();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		composite.setFont(parent.getFont());
		fJREBlock = new JREsComboBlock();
		fJREBlock.setDefaultJREDescriptor(new BuildJREDescriptor());
		fJREBlock.setTitle(JREMessages.JREContainerWizardPage_3); //$NON-NLS-1$
		fJREBlock.createControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJREBlock.getControl().setLayoutData(gd);
		setControl(composite);	
		
		setTitle(JREMessages.JREContainerWizardPage_JRE_System_Library_1); //$NON-NLS-1$
		setMessage(JREMessages.JREContainerWizardPage_Select_the_JRE_used_to_build_this_project__4); //$NON-NLS-1$
				
		initializeFromSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_WIZBAN_LIBRARY);
	}

}
