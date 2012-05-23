/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.wizard.Wizard;

/**
 * @since 3.3
 * 
 */
public abstract class VMInstallWizard extends Wizard {
	
	private VMStandin fEditVM;
	private String[] fExistingNames;
	
	/**
	 * Constructs a new wizard to add/edit a vm install.
	 * 
	 * @param editVM the VM being edited, or <code>null</code> if none
	 * @param currentInstalls current VM installs used to validate name changes
	 */
	public VMInstallWizard(VMStandin editVM, IVMInstall[] currentInstalls) {
		fEditVM = editVM;
		List<String> names = new ArrayList<String>(currentInstalls.length);
		for (int i = 0; i < currentInstalls.length; i++) {
			IVMInstall install = currentInstalls[i];
			if (!install.equals(editVM)) {
				names.add(install.getName());
			}
		}
		fExistingNames = names.toArray(new String[names.size()]);
	}
	
	/**
	 * Returns the VM to edit, or <code>null</code> if creating a VM
	 * 
	 * @return vm to edit or <code>null</code>
	 */
	protected VMStandin getVMInstall() {
		return fEditVM;
	}
	
	/**
	 * Returns the resulting VM after edit or creation or <code>null</code> if none.
	 * 
	 * @return resulting VM
	 */
	protected abstract VMStandin getResult();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return getResult() != null;
	}
	
	/**
	 * Returns a page to use for editing a VM install type
	 * 
	 * @param type
	 * @return
	 */
	public AbstractVMInstallPage getPage(IVMInstallType type) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.EXTENSION_POINT_VM_INSTALL_PAGES);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement element = infos[i];
			String id = element.getAttribute("vmInstallType"); //$NON-NLS-1$
			if (type.getId().equals(id)) {
				try {
					AbstractVMInstallPage page = (AbstractVMInstallPage) element.createExecutableExtension("class"); //$NON-NLS-1$
					page.setExistingNames(fExistingNames);
					return page;
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		StandardVMPage standardVMPage = new StandardVMPage();
		standardVMPage.setExistingNames(fExistingNames);
		return standardVMPage;
	}

}
