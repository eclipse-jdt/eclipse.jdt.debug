package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
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
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * A control for setting the working directory associated with a launch
 * configuration.
 */
public class WorkingDirectoryBlock extends JavaLaunchConfigurationTab {
			
	// Working directory UI widgets
	protected Label fWorkingDirLabel;
	
	// Local directory
	protected Button fLocalDirButton;
	protected Text fWorkingDirText;
	protected Button fWorkingDirBrowseButton;
	
	
	// Workspace directory
	protected Button fWorkspaceDirButton;
	protected Text fWorkspaceDirText;
	protected Button fWorkspaceDirBrowseButton;
		
	// use default button
	protected Button fUseDefaultWorkingDirButton;	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
						
		Composite workingDirComp = new Composite(parent, SWT.NONE);
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.numColumns = 3;
		workingDirLayout.marginHeight = 0;
		workingDirLayout.marginWidth = 0;
		workingDirComp.setLayout(workingDirLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirComp.setLayoutData(gd);
		setControl(workingDirComp);
		
		fWorkingDirLabel = new Label(workingDirComp, SWT.NONE);
		fWorkingDirLabel.setText(LauncherMessages.getString("JavaArgumentsTab.Wor&king_directory__2")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fWorkingDirLabel.setLayoutData(gd);
		
		fLocalDirButton = createRadioButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.&Local_directory__1")); //$NON-NLS-1$
		fLocalDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});
		
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
		
		fWorkspaceDirButton = createRadioButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.Works&pace__2")); //$NON-NLS-1$
		fWorkspaceDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});		
		
		fWorkspaceDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkspaceDirText.setLayoutData(gd);
		fWorkspaceDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fWorkspaceDirBrowseButton = createPushButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.B&rowse..._3"), null); //$NON-NLS-1$
		fWorkspaceDirBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkspaceDirBrowseButtonSelected();
			}
		});		
				
		fUseDefaultWorkingDirButton = new Button(workingDirComp,SWT.CHECK);
		fUseDefaultWorkingDirButton.setText(LauncherMessages.getString("JavaArgumentsTab.Use_de&fault_working_directory_4")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fUseDefaultWorkingDirButton.setLayoutData(gd);
		fUseDefaultWorkingDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleUseDefaultWorkingDirButtonSelected();
			}
		});
				
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
	 * Show a dialog that lets the user select a working directory from 
	 * the workspace
	 */
	protected void handleWorkspaceDirBrowseButtonSelected() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
																	   ResourcesPlugin.getWorkspace().getRoot(),
																	   false,
																	   LauncherMessages.getString("WorkingDirectoryBlock.Select_a_&workspace_relative_working_directory__4")); //$NON-NLS-1$
		
		IContainer currentContainer = getContainer();
		if (currentContainer != null) {
			IPath path = currentContainer.getFullPath();
			dialog.setInitialSelections(new Object[] {path});
		}
		
		
		dialog.open();
		Object[] results = dialog.getResult();		
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.makeRelative().toString();
			fWorkspaceDirText.setText(containerName);
		}			
	}
	
	/**
	 * Returns the selected workspace container,or <code>null</code>
	 */
	protected IContainer getContainer() {
		IResource res = getResource();
		if (res instanceof IContainer) {
			return (IContainer)res;
		}
		return null;
	}
	
	/**
	 * Returns the selected workspace resource, or <code>null</code>
	 */
	protected IResource getResource() {
		IPath path = new Path(fWorkspaceDirText.getText());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(path);
	}	
	
	/**
	 * The "local directory" or "workspace directory" button has been selected.
	 */
	protected void handleLocationButtonSelected() {
		if (!isDefaultWorkingDirectory()) {
			boolean local = isLocalWorkingDirectory();
			fWorkingDirText.setEnabled(local);
			fWorkingDirBrowseButton.setEnabled(local);
			fWorkspaceDirText.setEnabled(!local);
			fWorkspaceDirBrowseButton.setEnabled(!local);
		}
		updateLaunchConfigurationDialog();
	}
		
	/**
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		if (isDefaultWorkingDirectory()) {
			fLocalDirButton.setSelection(true);
			fWorkspaceDirButton.setSelection(false);
			fLocalDirButton.setEnabled(false);
			fWorkingDirText.setText(getDefaultWorkingDir());
			fWorkingDirText.setEnabled(false);
			fWorkspaceDirButton.setEnabled(false);
			fWorkspaceDirText.setEnabled(false);
			fWorkspaceDirBrowseButton.setEnabled(false);
		} else {
			fLocalDirButton.setEnabled(true);
			fWorkspaceDirButton.setEnabled(true);
			handleLocationButtonSelected();
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
		
		if (isLocalWorkingDirectory()) {
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
		} else {
			if (getContainer() == null) {
				setErrorMessage(LauncherMessages.getString("WorkingDirectoryBlock.Specified_project_or_folder_does_not_exist._5")); //$NON-NLS-1$
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
			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null); //$NON-NLS-1$
			fWorkspaceDirText.setText(""); //$NON-NLS-1$
			fWorkingDirText.setText(""); //$NON-NLS-1$
			if (wd == null) {
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				IPath path = new Path(wd);
				if (path.isAbsolute()) {
					fWorkingDirText.setText(wd);
					fLocalDirButton.setSelection(true);
					fWorkspaceDirButton.setSelection(false);
				} else {
					fWorkspaceDirText.setText(wd);
					fWorkspaceDirButton.setSelection(true);
					fLocalDirButton.setSelection(false);
				}
				fUseDefaultWorkingDirButton.setSelection(false);
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
		if (!isDefaultWorkingDirectory()) {
			if (isLocalWorkingDirectory()) {
				wd = getAttributeValueFrom(fWorkingDirText);
			} else {
				IPath path = new Path(fWorkspaceDirText.getText());
				path = path.makeRelative();
				wd = path.toString();
			}
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
		return LauncherMessages.getString("WorkingDirectoryBlock.Working_Directory_8"); //$NON-NLS-1$
	}	
	
	/**
	 * Returns whether the default working directory is to be used
	 */
	protected boolean isDefaultWorkingDirectory() {
		return fUseDefaultWorkingDirButton.getSelection();
	}
	
	/**
	 * Returns whether the working directory is local
	 */
	protected boolean isLocalWorkingDirectory() {
		return fLocalDirButton.getSelection();
	}
}

