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

/**
 * @since 3.3
 */
public class EditVMInstallWizard extends VMInstallWizard {
	
	private AbstractVMInstallPage fEditPage;
	
	/**
	 * Constructs a wizard to edit the given vm.
	 * 
	 * @param vm vm to edit
	 * @param allVMs all VMs being edited
	 */
	public EditVMInstallWizard(VMStandin vm, IVMInstall[] allVMs) {
		super(vm, allVMs);
		setWindowTitle(JREMessages.EditVMInstallWizard_0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		fEditPage = getPage(getVMInstall().getVMInstallType());
		fEditPage.setSelection(new VMStandin(getVMInstall()));
		addPage(fEditPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (fEditPage.finish()) {
			return super.performFinish();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.jres.VMInstallWizard#getResult()
	 */
	@Override
	protected VMStandin getResult() {
		return fEditPage.getSelection();
	}

	
}
