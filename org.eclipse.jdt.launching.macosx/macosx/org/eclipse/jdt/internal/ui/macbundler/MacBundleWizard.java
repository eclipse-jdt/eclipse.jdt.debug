/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;

import java.io.IOException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.*;
import org.eclipse.ui.IExportWizard;


public class MacBundleWizard extends Wizard implements IExportWizard, BundleAttributes {
	
	IWorkbench fWorkbench;
	IStructuredSelection fSelection;
	BundleDescription fBundleDescription= new BundleDescription();
	

	public MacBundleWizard() {
		//setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.gif"));	//$NON-NLS-1$
		setWindowTitle(Util.getString("MacBundleWizard.title")); //$NON-NLS-1$	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fWorkbench= workbench;
		fSelection= selection;
	}
	
	IStructuredSelection getSelection() {
		return fSelection;
	}

	/*
	 * (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		addPage(new BundleWizardPage1(fBundleDescription));
		addPage(new BundleWizardPage2(fBundleDescription));
		addPage(new BundleWizardPage3(fBundleDescription));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {		
		try {
			BundleBuilder bb= new BundleBuilder();
			bb.createBundle(fBundleDescription, null);
		} catch (IOException e) {
			// NeedWork Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
