/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * @since 3.3
 */
public class AddVMInstallWizard extends VMInstallWizard {
	
	private IWizardPage fTypePage = null;
	
	private VMStandin fResult = null;

	/**
	 * Constructs a wizard to add a new VM install.
	 * 
	 * @param currentInstalls currently existing VMs, used for name validation
	 */
	public AddVMInstallWizard(IVMInstall[] currentInstalls) {
		super(null, currentInstalls);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(JREMessages.AddVMInstallWizard_0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		fTypePage = new VMTypePage();
		addPage(fTypePage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.VMInstallWizard#getResult()
	 */
	@Override
	protected VMStandin getResult() {
		return fResult;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.VMInstallWizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return currentPage != fTypePage && super.canFinish() && currentPage.isPageComplete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.VMInstallWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage instanceof AbstractVMInstallPage) {
			AbstractVMInstallPage page = (AbstractVMInstallPage) currentPage;
			boolean finish = page.finish();
			fResult = page.getSelection();
			return finish;
		}
		return false;
	}

	
	
}
