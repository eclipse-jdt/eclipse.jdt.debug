package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.SelectionDialog;

public class MainTab implements ILaunchConfigurationTab, IAddVMDialogRequestor {

	// The launch configuration dialog that owns this tab
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	// Flag that when true, prevents the owning dialog's status area from getting updated.
	// Used when multiple config attributes are getting updated at once.
	private boolean fBatchUpdate = false;
	
	// Listener for modify events in all text-based widgets
	private ModifyListener fModifyListener;

	// Main class UI widgets
	private Label fMainLabel;
	private Text fMainText;
	private Button fSearchButton;
	private Button fSearchExternalJarsCheckButton;
	
	// Program arguments UI widgets
	private Label fPrgmArgumentsLabel;
	private Text fPrgmArgumentsText;

	// JRE UI widgets
	private Label fJRELabel;
	private Combo fJRECombo;
	private Button fJREAddButton;

	// VM arguments UI widgets
	private Label fVMArgumentsLabel;
	private Text fVMArgumentsText;
	
	// Working directory UI widgets
	private Label fWorkingDirLabel;
	private Text fWorkingDirText;
	private Button fWorkingDirBrowseButton;
	
	// Collections used to populating the JRE Combo box
	private IVMInstallType[] fVMTypes;
	private List fVMStandins;
	
	// The launch config working copy providing the values shown on this tab
	private ILaunchConfigurationWorkingCopy fWorkingCopy;

	private static final String EMPTY_STRING = "";
	
	protected void setLaunchDialog(ILaunchConfigurationDialog dialog) {
		fLaunchConfigurationDialog = dialog;
	}
	
	protected ILaunchConfigurationDialog getLaunchDialog() {
		return fLaunchConfigurationDialog;
	}
	
	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}
	
	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp);
		
		Composite mainComp = new Composite(comp, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);
		
		fMainLabel = new Label(mainComp, SWT.NONE);
		fMainLabel.setText("Main class");
		gd = new GridData();
		gd.horizontalSpan = 3;
		fMainLabel.setLayoutData(gd);

		fMainText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fMainText.setLayoutData(gd);
		fMainText.setEditable(false);
		fMainText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromMain();
			}
		});
		
		fSearchButton = new Button(mainComp, SWT.PUSH);
		fSearchButton.setText("Search");
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
			}
		});
		
		fSearchExternalJarsCheckButton = new Button(mainComp, SWT.CHECK);
		fSearchExternalJarsCheckButton.setText("Ext. jars");
		fSearchExternalJarsCheckButton.setToolTipText("Include external jars when searching for a main class");
				
		fPrgmArgumentsLabel = new Label(comp, SWT.NONE);
		fPrgmArgumentsLabel.setText("Program arguments");
						
		fPrgmArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fPrgmArgumentsText.setLayoutData(gd);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromPgmArgs();
			}
		});
		
		Composite workingDirComp = new Composite(comp, SWT.NONE);
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.numColumns = 2;
		workingDirLayout.marginHeight = 0;
		workingDirLayout.marginWidth = 0;
		workingDirComp.setLayout(workingDirLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirComp.setLayoutData(gd);
		
		fWorkingDirLabel = new Label(workingDirComp, SWT.NONE);
		fWorkingDirLabel.setText("Working directory");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fWorkingDirLabel.setLayoutData(gd);
		
		fWorkingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkingDirText.setLayoutData(gd);
		fWorkingDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromWorkingDirectory();
			}
		});
		
		fWorkingDirBrowseButton = new Button(workingDirComp, SWT.PUSH);
		fWorkingDirBrowseButton.setText("Browse");
		fWorkingDirBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkingDirBrowseButtonSelected();
			}
		});
				
		
		createVerticalSpacer(comp);
				
		Composite jreComp = new Composite(comp, SWT.NONE);
		GridLayout jreLayout = new GridLayout();
		jreLayout.numColumns = 2;
		jreLayout.marginHeight = 0;
		jreLayout.marginWidth = 0;
		jreComp.setLayout(jreLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		jreComp.setLayoutData(gd);
		
		fJRELabel = new Label(jreComp, SWT.NONE);
		fJRELabel.setText("JRE");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fJRELabel.setLayoutData(gd);
		
		fJRECombo = new Combo(jreComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJRECombo.setLayoutData(gd);
		initializeJREComboBox();
		fJRECombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromJRE();
			}
		});
		
		fJREAddButton = new Button(jreComp, SWT.PUSH);
		fJREAddButton.setText("Add...");
		fJREAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleJREAddButtonSelected();
			}
		});
		
		fVMArgumentsLabel = new Label(comp, SWT.NONE);
		fVMArgumentsLabel.setText("VM arguments");
		
		fVMArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fVMArgumentsText.setLayoutData(gd);	
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromVMArgs();
			}
		});	
				
		return comp;
	}
	
	/**
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
		if (launchConfiguration.equals(getWorkingCopy())) {
			return;
		}
		
		setBatchUpdate(true);
		updateWidgetsFromConfig(launchConfiguration);
		setBatchUpdate(false);

		setWorkingCopy(launchConfiguration);
	}
	
	/**
	 * Set values for all UI widgets in this tab using values kept in the specified
	 * launch configuration.
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updateMainTypeFromConfig(config);
		updatePgmArgsFromConfig(config);
		updateJREFromConfig(config);
		updateVMArgsFromConfig(config);
		updateWorkingDirectoryFromConfig(config);
	}
	
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
		try {
			String mainType = config.getAttribute(JavaDebugUI.MAIN_TYPE_ATTR, EMPTY_STRING);
			fMainText.setText(mainType);
		} catch (CoreException ce) {			
		}		
	}

	protected void updatePgmArgsFromConfig(ILaunchConfiguration config) {
		try {
			String pgmArgs = config.getAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, EMPTY_STRING);
			fPrgmArgumentsText.setText(pgmArgs);
		} catch (CoreException ce) {			
		}
	}
	
	protected void updateJREFromConfig(ILaunchConfiguration config) {
		try {
			String vmID = config.getAttribute(JavaDebugUI.VM_INSTALL_ATTR, EMPTY_STRING);
			if (vmID.length() > 0) {
				selectJREComboBoxEntry(vmID);
			} else {
				clearJREComboBoxEntry();
			}
		} catch (CoreException ce) {			
		}
	}
		
	protected void updateVMArgsFromConfig(ILaunchConfiguration config) {
		try {
			String vmArgs = config.getAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, EMPTY_STRING);
			fVMArgumentsText.setText(vmArgs);
		} catch (CoreException ce) {			
		}
	}
	
	protected void updateWorkingDirectoryFromConfig(ILaunchConfiguration config) {
		try {
			String workingDir = config.getAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, EMPTY_STRING);
			fWorkingDirText.setText(workingDir);
		} catch (CoreException ce) {			
		}		
	}

	protected void updateConfigFromMain() {
		if (getWorkingCopy() != null) {
			getWorkingCopy().setAttribute(JavaDebugUI.MAIN_TYPE_ATTR, (String)fMainText.getData(JavaDebugUI.MAIN_TYPE_ATTR));
			refreshStatus();
		}
	}
	
	protected void updateConfigFromPgmArgs() {
		if (getWorkingCopy() != null) {
			String pgmArgs = fPrgmArgumentsText.getText();
			getWorkingCopy().setAttribute(JavaDebugUI.PROGRAM_ARGUMENTS_ATTR, pgmArgs);
			refreshStatus();
		}
	}
	
	protected void updateConfigFromJRE() {
		if (getWorkingCopy() != null) {
			int vmIndex = fJRECombo.getSelectionIndex();
			if (vmIndex > 0) {
				VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
				String vmID = vmStandin.getId();
				getWorkingCopy().setAttribute(JavaDebugUI.VM_INSTALL_ATTR, vmID);
				String vmTypeID = vmStandin.getVMInstallType().getId();
				getWorkingCopy().setAttribute(JavaDebugUI.VM_INSTALL_TYPE_ATTR, vmTypeID);
				refreshStatus();
			}
		}
	}
	
	protected void updateConfigFromVMArgs() {
		if (getWorkingCopy() != null) {
			String vmArgs = fVMArgumentsText.getText();
			getWorkingCopy().setAttribute(JavaDebugUI.VM_ARGUMENTS_ATTR, vmArgs);
			refreshStatus();
		}
	}
	
	protected void updateConfigFromWorkingDirectory() {
		if (getWorkingCopy() != null) {
			String workingDir = fWorkingDirText.getText();
			getWorkingCopy().setAttribute(JavaDebugUI.WORKING_DIRECTORY_ATTR, workingDir);
			refreshStatus();
		}
	}
	
	protected void refreshStatus() {
		if (!isBatchUpdate()) {
			getLaunchDialog().refreshStatus();
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	protected void setBatchUpdate(boolean update) {
		fBatchUpdate = update;
	}
	
	protected boolean isBatchUpdate() {
		return fBatchUpdate;
	}

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		Label spacer = new Label(comp, SWT.NONE);
	}
	
	/**
	 * Load the JRE related collections, and use these to set the values on the combo box
	 */
	protected void initializeJREComboBox() {
		fVMTypes= JavaRuntime.getVMInstallTypes();
		fVMStandins= createFakeVMInstalls(fVMTypes);
		populateJREComboBox();		
	}
	
	private List createFakeVMInstalls(IVMInstallType[] vmTypes) {
		ArrayList vms= new ArrayList();
		for (int i= 0; i < vmTypes.length; i++) {
			IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
			for (int j= 0; j < vmInstalls.length; j++) 
				vms.add(new VMStandin(vmInstalls[j]));
		}
		return vms;
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
		VMStandin selectedVMStandin = null;
		int index = -1;
		for (int i = 0; i < fVMStandins.size(); i++) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(i);
			if (vmStandin.getId().equals(vmID)) {
				index = i;
				selectedVMStandin = vmStandin;
				break;
			}
		}
		if (index > -1) {
			fJRECombo.select(index);
			fJRECombo.setData(JavaDebugUI.VM_INSTALL_TYPE_ATTR, selectedVMStandin.getVMInstallType().getId());
		}
	}
	
	/**
	 * Convenience method to remove any selection in the JRE combo box
	 */
	protected void clearJREComboBoxEntry() {
		//fJRECombo.clearSelection();
		fJRECombo.deselectAll();
	}
	
	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {
		Shell shell = getShell();
		IWorkbenchWindow workbenchWindow = JDIDebugUIPlugin.getActiveWorkbenchWindow();
		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		int constraints = IJavaElementSearchConstants.CONSIDER_BINARIES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS;
		}
		
		SelectionDialog dialog = JavaUI.createMainTypeDialog(shell, 
															 workbenchWindow, 
															 searchScope, 
															 constraints, 
															 false, 
															 "");
		dialog.setTitle("Choose a main type");
		dialog.setMessage("Choose a main type");
		if (dialog.open() == dialog.CANCEL) {
			return;
		}
		
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}
		
		IType type = (IType)results[0];
		StringBuffer buffer = new StringBuffer(type.getFullyQualifiedName());
		buffer.append(" (");
		buffer.append(type.getJavaProject().getProject().getName());
		buffer.append(')');
		
		// The order of these two statements is significant.  We must save the 
		// type's handle id first, since setting the text will trigger a modify
		// event, which results in updating the working config with the
		// type's handle identifier
		fMainText.setData(JavaDebugUI.MAIN_TYPE_ATTR, type.getHandleIdentifier());
		fMainText.setText(buffer.toString());
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
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Select a working directory for the launch configuration");
		String currentWorkingDir = fWorkingDirText.getText();
		if (!currentWorkingDir.trim().equals("")) {
			File path = new File(currentWorkingDir);
			if (path.exists()) {
				dialog.setFilterPath(currentWorkingDir);
			}			
		}
		
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			fWorkingDirText.setText(selectedDirectory);
		}		
	}
	
	/**
	 * Convenience method to get the shell.  It is important that the shell be the one 
	 * associated with the launch configuration dialog, and not the active workbench
	 * window.
	 */
	private Shell getShell() {
		return fMainLabel.getShell();
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
		fVMStandins.add(vm);
		populateJREComboBox();
		selectJREComboBoxEntry(vm.getId());
	}

}

