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


import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * An experimental replacement for the Installed JREs preference page.
 * 
 * @since 3.0
 */
public class JREsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
							
	// JRE Block
	private InstalledJREsBlock fJREBlock;									
		
	public JREsPreferencePage() {
		super();
		
		// only used when page is shown programatically
		setTitle(JREMessages.getString("JREsPreferencePage.1"));	 //$NON-NLS-1$
		
		setDescription(JREMessages.getString("JREsPreferencePage.2")); //$NON-NLS-1$
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Set the VM list that will be input for the main list control.
	 */
	private void populateVMList() {
		
		// force auto-dection to occurr before populating the VM list.
		JavaRuntime.getDefaultVMInstall();
		
		fJREBlock.fillWithWorkspaceJREs();
		
		// Set up the default VM
		initDefaultVM();
	}
	
	/**
	 * Find & verify the default VM.
	 */
	private void initDefaultVM() {
		IVMInstall realDefault= JavaRuntime.getDefaultVMInstall();
		if (realDefault != null) {
			IVMInstall[] vms= fJREBlock.getJREs();
			for (int i = 0; i < vms.length; i++) {
				IVMInstall fakeVM= (IVMInstall)vms[i];
				if (fakeVM.equals(realDefault)) {
					verifyDefaultVM(fakeVM);
					break;
				}
			}
		}
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {
		Font font= ancestor.getFont();
		initializeDialogUnits(ancestor);
		
		noDefaultAndApplyButton();
		
		Composite parent= new Composite(ancestor, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);		
		
		GridData data;
					
		fJREBlock = new InstalledJREsBlock();
		fJREBlock.createControl(parent);
		Control control = fJREBlock.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);
		control.setFont(font);
		
		fJREBlock.restoreColumnSettings(JDIDebugUIPlugin.getDefault().getDialogSettings(), IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);
						
		WorkbenchHelp.setHelp(parent, IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);		
		populateVMList();
		return parent;
	}
			
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		IVMInstall defaultVM = getCurrentDefaultVM();
		IVMInstall[] vms = fJREBlock.getJREs();
		JREsUpdater updater = new JREsUpdater(getShell());
		updater.updateJRESettings(vms, defaultVM);
		
		// save column widths
		IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
		fJREBlock.saveColumnSettings(settings, IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);
		
		return super.performOk();
	}	
	
	protected IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Verify that the specified VM can be a valid default VM.  This amounts to verifying
	 * that all of the VM's library locations exist on the file system.  If this fails,
	 * remove the VM from the table and try to set another default.
	 */
	private void verifyDefaultVM(IVMInstall vm) {
		if (vm != null) {
			
			// Verify that all of the specified VM's library locations actually exist
			LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vm);
			boolean exist = true;
			for (int i = 0; i < locations.length; i++) {
				exist = exist && new File(locations[i].getSystemLibraryPath().toOSString()).exists();
			}
			
			// If all library locations exist, check the corresponding entry in the list,
			// otherwise remove the VM
			if (exist) {
				fJREBlock.setCheckedJRE(vm);
			} else {
				fJREBlock.removeJREs(new IVMInstall[]{vm});
				IVMInstall def = JavaRuntime.getDefaultVMInstall();
				if (def == null) {
					fJREBlock.setCheckedJRE(null);
				} else {
					fJREBlock.setCheckedJRE(def);
				}
				ErrorDialog.openError(getControl().getShell(), JREMessages.getString("JREsPreferencePage.1"), JREMessages.getString("JREsPreferencePage.10"), new Status(IStatus.ERROR, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, JREMessages.getString("JREsPreferencePage.11"), null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
		} else {
			fJREBlock.setCheckedJRE(null);
		}
	}
	
	private IVMInstall getCurrentDefaultVM() {
		return fJREBlock.getCheckedJRE();
	}	
	
	/**
	 * @see IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(JREMessages.getString("JREsPreferencePage.12")); //$NON-NLS-1$
		}
	}
	
}
