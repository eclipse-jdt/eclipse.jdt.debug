package org.eclipse.jdt.internal.debug.ui.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JavaJRETab extends JavaLaunchConfigurationTab implements IAddVMDialogRequestor {

	// UI widgets
	protected Label fJRELabel;
	protected Combo fJRECombo;
	protected Button fJREAddButton;

	// Collections used to populating the JRE Combo box
	protected IVMInstallType[] fVMTypes;
	protected java.util.List fVMStandins;	

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
		
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NONE);
		setControl(topComp);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		topComp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		topComp.setLayoutData(gd);
		
		createVerticalSpacer(topComp, 2);
		
		fJRELabel = new Label(topComp, SWT.NONE);
		fJRELabel.setText(LauncherMessages.getString("JavaJRETab.Run_with_JRE__1")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fJRELabel.setLayoutData(gd);
		
		fJRECombo = new Combo(topComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJRECombo.setLayoutData(gd);
		initializeJREComboBox();
		fJRECombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fJREAddButton = createPushButton(topComp, LauncherMessages.getString("JavaJRETab.New_1"), null);  //$NON-NLS-1$
		fJREAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleJREAddButtonSelected();
			}
		});				
	}

	/**
	 * Initialize defaults based on the given java element.
	 */
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {		
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeDefaults(javaElement, config);
		} else {
			initializeHardCodedDefaults(config);	
		}
	}
	
	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {					
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateJREFromConfig(configuration);
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			String vmID = vmStandin.getId();
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vmID);
			String vmTypeID = vmStandin.getVMInstallType().getId();
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
		}		
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return isValid();
	}

	/**
	 * @see ILaunchConfigurationTab#isValid()
	 */
	public boolean isValid() {
		
		setErrorMessage(null);
		setMessage(null);
		
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			IVMInstall vm = vmStandin.convertToRealVM();
			File location = vm.getInstallLocation();
			if (location == null) {
				setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_home_directory_not_specified_36")); //$NON-NLS-1$
				return false;
			}
			if (!location.exists()) {
				setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_home_directory_does_not_exist_37")); //$NON-NLS-1$
				return false;
			}			
		} else {
			setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_not_specified_38")); //$NON-NLS-1$
			return false;
		}		
		
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaJRETab.&JRE_1"); //$NON-NLS-1$
	}

	/**
	 * @see IAddVMDialogRequestor#isDuplicateName(IVMInstallType, String)
	 */
	public boolean isDuplicateName(IVMInstallType type, String name) {
		for (int i= 0; i < fVMStandins.size(); i++) {
			IVMInstall vm= (IVMInstall)fVMStandins.get(i);
			if (vm.getVMInstallType() == type) {
				if (vm.getName().equals(name))
					return true;
			}
		}
		return false;
	}

	/**
	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
		((VMStandin)vm).convertToRealVM();		
		try {
			JavaRuntime.saveVMConfiguration();
		} catch(CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		fVMStandins.add(vm);
		populateJREComboBox();
		selectJREComboBoxEntry(vm.getId());
	}

	protected void updateJREFromConfig(ILaunchConfiguration config) {
		String vmID = null;
		try {
			vmID = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, EMPTY_STRING);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}
		if (vmID == null) {
			clearJREComboBoxEntry();
		} else {
			selectJREComboBoxEntry(vmID);
		}
	}	
	
	/**
	 * Load the JRE related collections, and use these to set the values on the combo box
	 */
	protected void initializeJREComboBox() {
		fVMTypes= JavaRuntime.getVMInstallTypes();
		fVMStandins= createFakeVMInstalls(fVMTypes);
		populateJREComboBox();		
	}
	
	/**
	 * Show a dialog that lets the user add a new JRE definition
	 */
	protected void handleJREAddButtonSelected() {
		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, null);
		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.editJRE.title")); //$NON-NLS-1$
		if (dialog.open() != dialog.OK) {
			return;
		}
	}	
	
	/**
	 * Set the available items on the JRE combo box
	 */
	protected void populateJREComboBox() {
		String[] vmNames = new String[fVMStandins.size()];
		Iterator iterator = fVMStandins.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			VMStandin standin = (VMStandin)iterator.next();
			String vmName = standin.getName();
			vmNames[index] = vmName;
			index++;
		}
		fJRECombo.setItems(vmNames);
	}	
	
	/**
	 * Cause the VM with the specified ID to be selected in the JRE combo box.
	 * This relies on the fact that the items set on the combo box are done so in 
	 * the same order as they in the <code>fVMStandins</code> list.
	 */
	protected void selectJREComboBoxEntry(String vmID) {
		int index = -1;
		for (int i = 0; i < fVMStandins.size(); i++) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(i);
			if (vmStandin.getId().equals(vmID)) {
				index = i;
				break;
			}
		}
		if (index > -1) {
			fJRECombo.select(index);
		} else {
			clearJREComboBoxEntry();
		}
	}	
	
	/**
	 * Convenience method to remove any selection in the JRE combo box
	 */
	protected void clearJREComboBoxEntry() {
		fJRECombo.deselectAll();
	}	
	
	private java.util.List createFakeVMInstalls(IVMInstallType[] vmTypes) {
		ArrayList vms= new ArrayList();
		for (int i= 0; i < vmTypes.length; i++) {
			IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
			for (int j= 0; j < vmInstalls.length; j++) 
				vms.add(new VMStandin(vmInstalls[j]));
		}
		return vms;
	}	
	
}
