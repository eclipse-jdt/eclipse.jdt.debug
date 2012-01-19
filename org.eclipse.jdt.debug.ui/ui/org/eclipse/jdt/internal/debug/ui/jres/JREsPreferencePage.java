/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The Installed JREs preference page.
 * 
 * @since 3.0
 */
public class JREsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
				
	/**
	 * ID for the page
	 * 
	 * @since 3.5
	 */
	public static final String ID = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage"; //$NON-NLS-1$
	
	// JRE Block
	private InstalledJREsBlock fJREBlock;
	private Link fCompliance;
		
	/**
	 * Constructor
	 */
	public JREsPreferencePage() {
		super(JREMessages.JREsPreferencePage_1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Find & verify the default VM.
	 */
	private void initDefaultVM() {
		IVMInstall realDefault= JavaRuntime.getDefaultVMInstall();
		if (realDefault != null) {
			IVMInstall[] vms= fJREBlock.getJREs();
			for (int i = 0; i < vms.length; i++) {
				IVMInstall fakeVM= vms[i];
				if (fakeVM.equals(realDefault)) {
					verifyDefaultVM(fakeVM);
					break;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		
		noDefaultAndApplyButton();
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);
		
		SWTFactory.createWrapLabel(ancestor, JREMessages.JREsPreferencePage_2, 1, 300);
		SWTFactory.createVerticalSpacer(ancestor, 1);
		
		fJREBlock = new InstalledJREsBlock();
		fJREBlock.createControl(ancestor);
		Control control = fJREBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);
		
		fJREBlock.restoreColumnSettings(JDIDebugUIPlugin.getDefault().getDialogSettings(), IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);
					
		fCompliance = new Link(ancestor, SWT.NONE);
		fCompliance.setText(JREMessages.JREsPreferencePage_14);
		fCompliance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fCompliance.setVisible(false);
		fCompliance.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {openCompliancePreferencePage();}
		});
		PlatformUI.getWorkbench().getHelpSystem().setHelp(ancestor, IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);		
		initDefaultVM();
		fJREBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IVMInstall install = getCurrentDefaultVM();
				if (install == null) {
					setValid(false);
					setErrorMessage(JREMessages.JREsPreferencePage_13); 
				} else {
					//if we change the VM make sure the compliance level supports 
					//generated class files
					String compliance = getCurrentCompilerCompliance();
					if(!supportsCurrentCompliance(install, compliance)) {
						setMessage(NLS.bind(JREMessages.JREsPreferencePage_0, new String[] {compliance}), IMessageProvider.WARNING);
						fCompliance.setVisible(true);
					}
					else {
						setMessage(null);
						fCompliance.setVisible(false);
					}
					setValid(true);
					setErrorMessage(null);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		String compliance = getCurrentCompilerCompliance();
		if(!supportsCurrentCompliance(getCurrentDefaultVM(), compliance)) {
			setMessage(NLS.bind(JREMessages.JREsPreferencePage_0, new String[] {compliance}), IMessageProvider.WARNING);
			fCompliance.setVisible(true);
		}
		else {
			setMessage(null);
			fCompliance.setVisible(false);
		}
		return super.isValid();
	}

	/**
	 * Opens the <code>CompliancePreferencePage</code>
	 * @since 3.3
	 */
	private void openCompliancePreferencePage() {
		String compliancepage = "org.eclipse.jdt.ui.preferences.CompliancePreferencePage"; //$NON-NLS-1$
		IWorkbenchPreferenceContainer wpc = (IWorkbenchPreferenceContainer)getContainer();
		if (wpc != null) {
			wpc.openPage(compliancepage, null);
		} else {
			SWTFactory.showPreferencePage(compliancepage);
		}
	}
	
	/**
	 * @return the current compiler compliance level
	 * @since 3.3
	 */
	private String getCurrentCompilerCompliance() {
		IEclipsePreferences setting = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		if(getContainer() instanceof IWorkbenchPreferenceContainer) {
			IEclipsePreferences wcs = ((IWorkbenchPreferenceContainer)getContainer()).getWorkingCopyManager().getWorkingCopy(setting);
			return wcs.get(JavaCore.COMPILER_COMPLIANCE, (String) JavaCore.getDefaultOptions().get(JavaCore.COMPILER_COMPLIANCE));
		}
			return JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
		
	}
	
	/**
	 * Determines if the vm version will run the currently compiled code based on the compiler compliance lvl
	 * @param vm the vm install
	 * @param compliance the current compiler compliance level
	 * @return true if the selected vm will run the current code, false otherwise
	 * @since 3.3
	 */
	private boolean supportsCurrentCompliance(IVMInstall vm, String compliance) {
		if(vm instanceof AbstractVMInstall) {
			AbstractVMInstall install = (AbstractVMInstall) vm;
			String vmver = install.getJavaVersion();
			if(vmver == null) {
				//if we cannot get a version from the VM we must return true, and let the runtime
				//error sort it out
				return true;
			}
			int val = compliance.compareTo(vmver); 
			return  val < 0 || val == 0;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] {false};
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				IVMInstall defaultVM = getCurrentDefaultVM();
				IVMInstall[] vms = fJREBlock.getJREs();
				JREsUpdater updater = new JREsUpdater();
				if (!updater.updateJRESettings(vms, defaultVM)) {
					canceled[0] = true;
				}
			}
		});
		
		if(canceled[0]) {
			return false;
		}
		
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
				ErrorDialog.openError(getControl().getShell(), JREMessages.JREsPreferencePage_1, JREMessages.JREsPreferencePage_10, new Status(IStatus.ERROR, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, "JRE removed", null)); //   //$NON-NLS-1$
				return;
			}
		} else {
			fJREBlock.setCheckedJRE(null);
		}
	}
	
	private IVMInstall getCurrentDefaultVM() {
		return fJREBlock.getCheckedJRE();
	}
}
