/*******************************************************************************
 * Copyright (c) 2000, 20010 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
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
		super(JREMessages.JREContainerWizardPage_JRE_System_Library_1); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		IPath path = fJREBlock.getPath();
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
	 * Initializes the JRE selection
	 */
	protected void initializeFromSelection() {
		if (getControl() != null) {
			if (fSelection == null) {
				fJREBlock.setPath(JavaRuntime.newDefaultJREContainerPath());
			} else {
				fJREBlock.setPath(fSelection.getPath());
				IStatus status = fJREBlock.getStatus();
				if (!status.isOK()) {
					setErrorMessage(status.getMessage());
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		fJREBlock = new JREsComboBlock(false);
		fJREBlock.setDefaultJREDescriptor(new BuildJREDescriptor());
		fJREBlock.setTitle(JREMessages.JREContainerWizardPage_3); 
		fJREBlock.createControl(composite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fJREBlock.getControl().setLayoutData(gd);
		setControl(composite);
		fJREBlock.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				IStatus status = fJREBlock.getStatus();
				if (status.isOK()) {
					setErrorMessage(null);
				} else {
					setErrorMessage(status.getMessage());
				}
			}
		});
		
		setTitle(JREMessages.JREContainerWizardPage_JRE_System_Library_1); 
		setMessage(JREMessages.JREContainerWizardPage_4); 
				
		initializeFromSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_WIZBAN_LIBRARY);
	}

}
