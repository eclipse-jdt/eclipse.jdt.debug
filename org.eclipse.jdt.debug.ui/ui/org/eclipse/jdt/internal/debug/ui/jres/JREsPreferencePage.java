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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * An experimental replacement for the Installed JREs preference page.
 * 
 * @since 3.0
 */
public class JREsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
							
	// JRE Block
	private InstalledJREsBlock fJREBlock;									
	
	// the VMs defined when this page was opened
	private VMDefinitionsContainer fOriginalVMs;
	
	// cache of VM install types
	private IVMInstallType[] fVMTypes;

	public JREsPreferencePage() {
		super();
		
		// only used when page is shown programatically
		setTitle(LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"));	 //$NON-NLS-1$
				
		
		setDescription(LauncherMessages.getString("vmPreferencePage.message")); //$NON-NLS-1$
		fVMTypes = JavaRuntime.getVMInstallTypes();
		fOriginalVMs = new VMDefinitionsContainer();
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		if (def != null) {
			fOriginalVMs.setDefaultVMInstallCompositeID(JavaRuntime.getCompositeIdFromVM(def));
		}
	
		for (int i = 0; i < fVMTypes.length; i++) {
			IVMInstall[] vms = fVMTypes[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				fOriginalVMs.addVM(vms[j]);
			}
		}
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
		
		// Retrieve all known VM installs from each vm install type
		List vms = new ArrayList();
		for (int i= 0; i < fVMTypes.length; i++) {
			IVMInstall[] vmInstalls= fVMTypes[i].getVMInstalls();
			for (int j = 0; j < vmInstalls.length; j++) {
				vms.add(new VMStandin(vmInstalls[j]));
			}
		}
		
		// Set the input of the main list control
		fJREBlock.setJREs((IVMInstall[])vms.toArray(new IVMInstall[vms.size()]));
		
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
				
		Label tableLabel = new Label(parent, SWT.NONE);
		tableLabel.setText(LauncherMessages.getString("VMPreferencePage.Installed_&JREs__1")); //$NON-NLS-1$
		data = new GridData();
		tableLabel.setLayoutData(data);
		tableLabel.setFont(font);
		
		fJREBlock = new InstalledJREsBlock();
		fJREBlock.createControl(parent);
		Control control = fJREBlock.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);
		control.setFont(font);
						
		WorkbenchHelp.setHelp(parent, IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);		
		populateVMList();
		return parent;
	}
			
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		// Create a VM definition container
		VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
		
		// Set the default VM Id on the container
		IVMInstall defaultVM = getCurrentDefaultVM();
		String defaultVMId = JavaRuntime.getCompositeIdFromVM(defaultVM);
		vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
		
		// Set the VMs on the container
		IVMInstall[] vms = fJREBlock.getJREs();
		for (int i = 0; i < vms.length; i++) {
			vmContainer.addVM(vms[i]);
		}
		
		// determine if a build is required
		boolean buildRequired = false;
		try {
			buildRequired = isBuildRequired(fOriginalVMs, vmContainer);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		} 
		boolean build = false;
		if (buildRequired) {
			// prompt the user to do a full build
			MessageDialog messageDialog = new MessageDialog(getShell(), LauncherMessages.getString("VMPreferencePage.JRE_Settings_Changed_1"), null,  //$NON-NLS-1$
			LauncherMessages.getString("VMPreferencePage.The_JRE_settings_have_changed._A_full_build_is_required_to_make_the_changes_effective._Do_the_full_build_now__2"), //$NON-NLS-1$
			MessageDialog.QUESTION, new String[] {LauncherMessages.getString("VMPreferencePage.&Yes_3"), LauncherMessages.getString("VMPreferencePage.&No_4"), LauncherMessages.getString("VMPreferencePage.&Cancel_1")}, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			int button = messageDialog.open();
			if (button == 2) {
				return false;
			}
			build = button == 0;			
		}
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		// do a build if required
		if (build) {
			buildWorkspace();
		}
		
		return super.performOk();
	}	
	
	private void saveVMDefinitions(final VMDefinitionsContainer container) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Generate XML for the VM defs and save it as the new value of the VM preference
				try {
					String vmDefXML = container.getAsXML();
					JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_VM_XML, vmDefXML);
					JavaRuntime.savePreferences();
				} catch (IOException ioe) {
					JDIDebugUIPlugin.log(ioe);
				}
			}
		});
	}
	
	protected IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	private void buildWorkspace() {
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException{
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// opearation canceled by user
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"), LauncherMessages.getString("VMPreferencePage.Build_failed._1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}			
	
	private IVMInstall getCurrentDefaultVM() {
		return fJREBlock.getCheckedJRE();
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
				ErrorDialog.openError(getControl().getShell(), LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"), LauncherMessages.getString("VMPreferencePage.Installed_JRE_location_no_longer_exists.__JRE_will_be_removed_2"), new Status(IStatus.ERROR, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, LauncherMessages.getString("VMPreferencePage.JRE_removed_3"), null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
		} else {
			fJREBlock.setCheckedJRE(null);
		}
	}
	
	/**
	 * @see IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(LauncherMessages.getString("vmPreferencePage.title")); //$NON-NLS-1$
		}
	}

	/**
	 * Returns whether a re-build is required based on the previous and current
	 * VM definitions.
	 * 
	 * @param prev VMs defined in the workspace
	 * @param curr VMs that will be defined in the workspace
	 * @return whether the new JRE definitions required the workspace to be
	 * built
	 */
	private boolean isBuildRequired(VMDefinitionsContainer prev, VMDefinitionsContainer curr) throws CoreException {
		String prevDef = prev.getDefaultVMInstallCompositeID();
		String currDef = curr.getDefaultVMInstallCompositeID();
		
		boolean defaultChanged = !isEqual(prevDef, currDef);
		
		IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
		
		//if the default VM changed, see if any projects reference it
		if (defaultChanged) {
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				IClasspathEntry[] entries = project.getRawClasspath();
				for (int j = 0; j < entries.length; j++) {
					IClasspathEntry entry = entries[j];
					switch (entry.getEntryKind()) {
						case IClasspathEntry.CPE_VARIABLE:
							IPath path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRELIB_VARIABLE)) {
								// a project references the default JRE via JRE_LIB
								return true;
							}
							break;
						case IClasspathEntry.CPE_CONTAINER:
							path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
								// a project references the default JRE via JRE_CONTAIER
								return true;
							}
							break;
					};
				}
			}
		}
		
		// otherwise, if a referenced VM is removed or there is a library
		// change in a referenced VM, a build is required 
		List futureVMs = curr.getVMList();
		for (int i = 0; i < projects.length; i++) {
			IJavaProject project = projects[i];
			IVMInstall prevVM = JavaRuntime.getVMInstall(project);
			if (prevVM != null) {
				int index  = futureVMs.indexOf(prevVM);
				if (index >= 0) {
					IVMInstall futureVM = (IVMInstall)futureVMs.get(index);
					// the VM still exists, see if the libraries changed
					LibraryLocation[] prevLibs = JavaRuntime.getLibraryLocations(prevVM);
					LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(futureVM);
					if (prevLibs.length == newLibs.length) {
						for (int j = 0; j < newLibs.length; j++) {
							LibraryLocation newLib = newLibs[j];
							LibraryLocation prevLib = prevLibs[j];
							String newPath = newLib.getSystemLibraryPath().toOSString();
							String prevPath = prevLib.getSystemLibraryPath().toOSString();
							if (!newPath.equalsIgnoreCase(prevPath)) {
								// different libs or ordering, a re-build is required
								return true;
							}
						} 
					} else {
						// different number of libraries, a re-build is required
						return true;
					}
				} else {
					// the VM no longer exists, a re-build will be required
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	private boolean isEqual(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return (a.equals(b));
	}
	
}
