/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


public class MacBundleWizard extends Wizard implements IExportWizard, BundleAttributes {
	
	IWorkbench fWorkbench;
	IStructuredSelection fSelection;
	BundleDescription fBundleDescription= new BundleDescription();

	public MacBundleWizard() {
		setDefaultPageImageDescriptor(createWizardImageDescriptor("exportapp_wiz.png")); //$NON-NLS-1$
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
	@Override
	public void addPages() {
		super.addPages();
		addPage(new BundleWizardPage1(fBundleDescription));
		addPage(new BundleWizardPage2(fBundleDescription));
		addPage(new BundleWizardPage3(fBundleDescription));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
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

	private static ImageDescriptor createWizardImageDescriptor(String name) {
		try {
			URL baseUrl= MacOSXUILaunchingPlugin.getDefault().getBundle().getEntry("/icons/full/wizban/"); //$NON-NLS-1$	
			if (baseUrl != null)
				return ImageDescriptor.createFromURL(new URL(baseUrl, name));
		} catch (MalformedURLException e) {
			// fall through
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}
}
