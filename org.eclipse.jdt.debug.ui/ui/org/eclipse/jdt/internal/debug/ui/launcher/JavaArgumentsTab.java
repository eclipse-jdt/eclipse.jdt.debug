package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This tab appears for local java launch configurations and allows the user to edit
 * program arguments, VM arguments, and the working directory attributes.
 */
public class JavaArgumentsTab extends JavaLaunchConfigurationTab {
		
	// Program arguments UI widgets
	protected Label fPrgmArgumentsLabel;
	protected Text fPrgmArgumentsText;

	// VM arguments UI widgets
	protected Label fVMArgumentsLabel;
	protected Text fVMArgumentsText;
	
	// Working directory UI widgets
	protected Label fWorkingDirLabel;
	protected Text fWorkingDirText;
	protected Button fWorkingDirBrowseButton;
	protected Button fUseDefaultWorkingDirButton;
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
		
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp);
				
		Composite workingDirComp = new Composite(comp, SWT.NONE);
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.numColumns = 2;
		workingDirLayout.marginHeight = 0;
		workingDirLayout.marginWidth = 0;
		workingDirComp.setLayout(workingDirLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirComp.setLayoutData(gd);
		
		fWorkingDirLabel = new Label(workingDirComp, SWT.NONE);
		fWorkingDirLabel.setText(LauncherMessages.getString("JavaArgumentsTab.Wor&king_directory__2")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fWorkingDirLabel.setLayoutData(gd);
		
		fWorkingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkingDirText.setLayoutData(gd);
		fWorkingDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fWorkingDirBrowseButton = createPushButton(workingDirComp, LauncherMessages.getString("JavaArgumentsTab.&Browse_3"), null); //$NON-NLS-1$
		fWorkingDirBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkingDirBrowseButtonSelected();
			}
		});
		
		fUseDefaultWorkingDirButton = new Button(workingDirComp,SWT.CHECK);
		fUseDefaultWorkingDirButton.setText(LauncherMessages.getString("JavaArgumentsTab.Use_de&fault_working_directory_4")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fUseDefaultWorkingDirButton.setLayoutData(gd);
		fUseDefaultWorkingDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleUseDefaultWorkingDirButtonSelected();
			}
		});
		
		createVerticalSpacer(comp);
				
		fPrgmArgumentsLabel = new Label(comp, SWT.NONE);
		fPrgmArgumentsLabel.setText(LauncherMessages.getString("JavaArgumentsTab.&Program_arguments__5")); //$NON-NLS-1$
						
		fPrgmArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		fPrgmArgumentsText.setLayoutData(gd);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fVMArgumentsLabel = new Label(comp, SWT.NONE);
		fVMArgumentsLabel.setText(LauncherMessages.getString("JavaArgumentsTab.VM_ar&guments__6")); //$NON-NLS-1$
		
		fVMArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		fVMArgumentsText.setLayoutData(gd);	
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});	
		
	}
		
	/**
	 * @see JavaLaunchConfigurationTab#updateWidgetsFromConfig(ILaunchConfiguration)
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updatePgmArgsFromConfig(config);
		updateVMArgsFromConfig(config);
		updateWorkingDirectoryFromConfig(config);
	}
	
	protected void updatePgmArgsFromConfig(ILaunchConfiguration config) {
		try {
			String pgmArgs = EMPTY_STRING;
			if (config != null) {
				pgmArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, EMPTY_STRING);
			}
			fPrgmArgumentsText.setText(pgmArgs);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}
	}
	
	protected void updateVMArgsFromConfig(ILaunchConfiguration config) {
		try {
			String vmArgs = EMPTY_STRING;
			if (config != null) {
				vmArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, EMPTY_STRING);
			}
			fVMArgumentsText.setText(vmArgs);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
	}
	
	protected void updateWorkingDirectoryFromConfig(ILaunchConfiguration config) {
		try {
			String workingDir = EMPTY_STRING;
			if (config != null) {
				workingDir = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, EMPTY_STRING);
			}
			fWorkingDirText.setText(workingDir);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);					
		}		
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}
	
	/**
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LauncherMessages.getString("JavaArgumentsTab.Select_a_&working_directory_for_the_launch_configuration__7")); //$NON-NLS-1$
		String currentWorkingDir = fWorkingDirText.getText();
		if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
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
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		if (fUseDefaultWorkingDirButton.getSelection()) {
			fWorkingDirText.setText(getDefaultWorkingDir());
			fWorkingDirText.setEnabled(false);
			fWorkingDirBrowseButton.setEnabled(false);
		} else {
			fWorkingDirBrowseButton.setEnabled(true);
			fWorkingDirText.setEnabled(true);
		}
	}
	
	/**
	 * Returns the default working directory
	 */
	protected String getDefaultWorkingDir() {
		return System.getProperty("user.dir"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {
		
		setErrorMessage(null);
		setMessage(null);
		
		String workingDirPath = fWorkingDirText.getText().trim();
		if (workingDirPath.length() > 0) {
			File dir = new File(workingDirPath);
			if (!dir.exists()) {
				setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Working_directory_does_not_exist_10")); //$NON-NLS-1$
				return false;
			}
			if (!dir.isDirectory()) {
				setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Working_directory_is_not_a_directory_11")); //$NON-NLS-1$
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fPrgmArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			fVMArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "")); //$NON-NLS-1$
			
			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
			if (wd.trim().length() == 0) {
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				fUseDefaultWorkingDirButton.setSelection(false);
				fWorkingDirText.setText(wd);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getAttributeValueFrom(fPrgmArgumentsText));
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, getAttributeValueFrom(fVMArgumentsText));
		String wd = null;
		if (!fUseDefaultWorkingDirButton.getSelection()) {
			wd = getAttributeValueFrom(fWorkingDirText);
		} 
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, wd);
	}

	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaArgumentsTab.&Arguments_16"); //$NON-NLS-1$
	}	
}

