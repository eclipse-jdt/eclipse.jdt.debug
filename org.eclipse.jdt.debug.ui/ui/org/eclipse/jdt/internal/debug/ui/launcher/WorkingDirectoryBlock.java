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
 * A control for setting the working directory associated with a launch
 * configuration.
 */
public class WorkingDirectoryBlock extends JavaLaunchConfigurationTab {
			
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
						
		Composite workingDirComp = new Composite(parent, SWT.NONE);
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.numColumns = 2;
		workingDirLayout.marginHeight = 0;
		workingDirLayout.marginWidth = 0;
		workingDirComp.setLayout(workingDirLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirComp.setLayoutData(gd);
		setControl(workingDirComp);
		
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
				
	}
		
	/**
	 * @see JavaLaunchConfigurationTab#updateWidgetsFromConfig(ILaunchConfiguration)
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updateWorkingDirectoryFromConfig(config);
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
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		
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
		return "Working Directory";
	}	
}

