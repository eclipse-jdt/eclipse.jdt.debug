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
package org.eclipse.jdt.debug.ui.launchConfigurations;

 
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.jres.JREDescriptor;
import org.eclipse.jdt.internal.debug.ui.jres.JREsComboBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits the VM install 
 * launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class JavaJRETab extends JavaLaunchConfigurationTab {
	
	// JRE Block
	protected JREsComboBlock fJREBlock;
	
	// unknown JRE
	protected String fUnknownVMType;
	protected String fUnknownVMName;
	protected boolean fOkToClearUnknownVM = true;

	// Dynamic JRE UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;
	protected boolean fUseDynamicArea = true;
	
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected ILaunchConfiguration fLaunchConfiguration;
	
	// State
	protected boolean fIsInitializing = false;
	
	// Selection changed listener (checked JRE)
	private ISelectionChangedListener fCheckListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			handleSelectedJREChanged();
		}
	};
	
	// Constants
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fJREBlock != null) {
			fJREBlock.removeSelectionChangedListener(fCheckListener);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite topComp = new Composite(parent, SWT.NONE);
		setControl(topComp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_JRE_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		topLayout.marginHeight=0;
		topLayout.marginWidth=0;
		topComp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		topComp.setLayoutData(gd);
		topComp.setFont(font);
				
		fJREBlock = new JREsComboBlock();
		fJREBlock.setDefaultJREDescriptor(getDefaultJREDescriptor());
		fJREBlock.setSpecificJREDescriptor(getSpecificJREDescriptor());
		fJREBlock.createControl(topComp);
		Control control = fJREBlock.getControl();
		fJREBlock.addSelectionChangedListener(fCheckListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		control.setLayoutData(gd);
		
		Composite dynTabComp = new Composite(topComp, SWT.NONE);
		dynTabComp.setFont(font);
		
		setDynamicTabHolder(dynTabComp);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight= 0;
		tabHolderLayout.marginWidth= 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		gd = new GridData(GridData.FILL_BOTH);
		getDynamicTabHolder().setLayoutData(gd);
	}

	protected void setDynamicTabHolder(Composite tabHolder) {
		this.fDynamicTabHolder = tabHolder;
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTab(ILaunchConfigurationTab tab) {
		fDynamicTab = tab;
	}

	protected ILaunchConfigurationTab getDynamicTab() {
		return fDynamicTab;
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fIsInitializing = true;
		getControl().setRedraw(false);
		fOkToClearUnknownVM = false;
		if (getLaunchConfiguration() != null && !configuration.equals(getLaunchConfiguration())) {
			fUnknownVMName = null;
			fUnknownVMType = null;
		}
		setLaunchConfiguration(configuration);
		updateJREFromConfig(configuration);
		fJREBlock.setDefaultJREDescriptor(getDefaultJREDescriptor());
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(configuration);
		}		
		fOkToClearUnknownVM = true;
		getControl().setRedraw(true);
		fIsInitializing = false;
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fUnknownVMName == null) {
				
			IVMInstall vm = null;
			boolean vmExists = true;
			if (!fJREBlock.isDefaultJRE()) {
				vm = fJREBlock.getJRE();
				vmExists = vm != null;
			}
		
			// Set the name & type ID attribute values
			if (vmExists) {
				// A null vm means the default VM was selected, in which case we want
				// to set null attribute values.  Otherwise, retrieve the name & type ID.
				String vmName = null;
				String vmTypeID = null;
				if (vm != null) {
					vmName = vm.getName();
					vmTypeID = vm.getVMInstallType().getId();
				}				
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
			
				// Handle any attributes in the VM-specific area
				ILaunchConfigurationTab dynamicTab = getDynamicTab();
				if (dynamicTab == null) {
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
				} else {
					dynamicTab.performApply(configuration);
				}
			}
		}
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		
		setErrorMessage(null);
		setMessage(null);
		
		if (fUnknownVMName != null) {
			setErrorMessage(MessageFormat.format(LauncherMessages.JavaJRETab_Configuration_specifies_undefined_JRE____0__1, new String[]{fUnknownVMName}));			 //$NON-NLS-1$
			return false;
		}
		
		// Don't do any validation if the default VM was chosen
		IVMInstall vm = fJREBlock.getJRE();
		if (vm == null) {
			if (!fJREBlock.isDefaultJRE()) {
				setErrorMessage(LauncherMessages.JavaJRETab_JRE_not_specified_38); //$NON-NLS-1$
				return false;
			}			
		} else {
			File location = vm.getInstallLocation();
			if (location == null) {
				setErrorMessage(LauncherMessages.JavaJRETab_JRE_home_directory_not_specified_36); //$NON-NLS-1$
				return false;
			}
			if (!location.exists()) {
				setErrorMessage(LauncherMessages.JavaJRETab_JRE_home_directory_does_not_exist_37); //$NON-NLS-1$
				return false;
			}			
		}		

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.JavaJRETab__JRE_1; //$NON-NLS-1$
	}
	
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
	}	

	protected void updateJREFromConfig(ILaunchConfiguration config) {
		String vmName = null;
		String vmTypeID = null;
		try {
			vmTypeID = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			vmName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}
		selectJRE(vmTypeID, vmName);
	}	
	
	/**
	 * Notification that the user changed the selection in the JRE combo box.
	 */
	protected void handleSelectedJREChanged() {
		if (fOkToClearUnknownVM) {
			fUnknownVMName = null;
			fUnknownVMType = null;
		}
		
		loadDynamicJREArea();
		
		// always set the newly created area with defaults
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
		if (getDynamicTab() == null) {
			// remove any VM specfic args from the config
			if (wc == null) {
				if (getLaunchConfiguration().isWorkingCopy()) {
					wc = (ILaunchConfigurationWorkingCopy)getLaunchConfiguration();
				}
			}
			if (!fIsInitializing) {
				if (wc != null) {
					wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
				}
			}
		} else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						// get a fresh copy to work on
						wc = ((ILaunchConfigurationWorkingCopy)getLaunchConfiguration()).getOriginal().getWorkingCopy();
					} else {
							wc = getLaunchConfiguration().getWorkingCopy();
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.errorDialog(LauncherMessages.JavaJRETab_Unable_to_initialize_defaults_for_selected_JRE_1, e); //$NON-NLS-1$
					return;
				}
			}
			if (!fIsInitializing) {
				getDynamicTab().setDefaults(wc);
				getDynamicTab().initializeFrom(wc);
			}
		}
				
		updateLaunchConfigurationDialog();		
	}
	
	protected void selectJRE(String typeID, String vmName) {
		if (typeID == null) {
			fJREBlock.setUseDefaultJRE();
		} else {
			IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
			for (int i = 0; i < types.length; i++) {
				IVMInstallType type = types[i];
				if (type.getId().equals(typeID)) {
					IVMInstall[] installs = type.getVMInstalls();
					for (int j = 0; j < installs.length; j++) {
						IVMInstall install = installs[j];
						if (install.getName().equals(vmName)) {
							fJREBlock.setJRE(install);
							return;
						}
						
					}
					break;
				}
			}
			fUnknownVMName = vmName;
			fJREBlock.setJRE(null);
		}
	}
	
	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code>
	 * that is registered against the install type of the currently selected VM.
	 */
	protected ILaunchConfigurationTab getTabForCurrentJRE() {
		if (!fJREBlock.isDefaultJRE()) {
			IVMInstall vm = fJREBlock.getJRE();
			if (vm != null) {
				String vmInstallTypeID = vm.getVMInstallType().getId();
				return JDIDebugUIPlugin.getDefault().getVMInstallTypePage(vmInstallTypeID);
			}
		}		
		return null;
	}
	
	/**
	 * Show the contributed piece of UI that was registered for the install type
	 * of the currently selected VM.
	 */
	protected void loadDynamicJREArea() {
		
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getDynamicTabHolder().getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		
		if (isUseDynamicJREArea()) {
			// Retrieve the dynamic UI for the current JRE 
			setDynamicTab(getTabForCurrentJRE());
			if (getDynamicTab() == null) {
				return;
			}
			
			// Ask the dynamic UI to create its Control
			getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
			getDynamicTab().createControl(getDynamicTabHolder());
			getDynamicTabHolder().layout();	
		}
			
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * Overridden here so that any error message in the dynamic UI gets returned.
	 * 
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		ILaunchConfigurationTab tab = getDynamicTab();
		if ((super.getErrorMessage() != null) || (tab == null)) {
			return super.getErrorMessage();
		}
		return tab.getErrorMessage();
	}

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
	}
	
	/**
	 * Sets whether this tab will display the VM specific arguments area
	 * if a JRE supports VM specific arguments.
	 * 
	 * @param visible whether this tab will display the VM specific arguments area
	 * 	if a JRE supports VM specific arguments
	 */
	public void setVMSpecificArgumentsVisible(boolean visible) {
		fUseDynamicArea = visible;
	}
	
	protected boolean isUseDynamicJREArea() {
		return fUseDynamicArea;
	}

	protected JREDescriptor getDefaultJREDescriptor() {
		return new JREDescriptor() {

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDescription()
			 */
			public String getDescription() {
				IJavaProject project = getJavaProject();
				String name = LauncherMessages.JavaJRETab_7; //$NON-NLS-1$
				if (project == null) {
					IVMInstall vm = JavaRuntime.getDefaultVMInstall();
					if (vm != null) {
						name = vm.getName();
					}
					return MessageFormat.format(LauncherMessages.JavaJRETab_8, new String[]{name}); //$NON-NLS-1$
				}
				try {
					IVMInstall vm = JavaRuntime.getVMInstall(project);
					if (vm != null) {
						name = vm.getName();
					}
				} catch (CoreException e) {
				}
				return MessageFormat.format(LauncherMessages.JavaJRETab_9, new String[]{name}); //$NON-NLS-1$
			}
		};
	}
	
	protected JREDescriptor getSpecificJREDescriptor() {
		return null;
	}
	
	/**
	 * Returns the Java project associated with the current config being edited,
	 * or <code>null</code> if none.
	 * 
	 * @return java project or <code>null</code>
	 */
	protected IJavaProject getJavaProject() {
		if (getLaunchConfiguration() != null) {
			try {
				String name = getLaunchConfiguration().getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				if (name != null && name.length() > 0) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
					if (project.exists()) { 
						return JavaCore.create(project);
					}
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// update the default JRE descriptoin, in case it has changed
		// based on the selected project
		fJREBlock.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}	
}
