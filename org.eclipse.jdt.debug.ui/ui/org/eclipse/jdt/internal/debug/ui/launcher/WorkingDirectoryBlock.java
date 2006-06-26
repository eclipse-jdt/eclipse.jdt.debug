/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * A control for setting the working directory associated with a launch
 * configuration.
 */
public class WorkingDirectoryBlock extends JavaLaunchTab {
			
	// Local directory
	private Button fWorkspaceButton;
	private Button fFileSystemButton;
	private Button fVariablesButton;
	
	//bug 29565 fix
	private Button fUseDefaultDirButton = null;
	private Button fUseOtherDirButton = null;
	private Text fOtherWorkingText = null;
	private Text fWorkingDirText;
	
	/**
	 * The last launch config this tab was initialized from
	 */
	private ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * A listener to update for text changes and widget selection
	 */
	private class WidgetListener extends SelectionAdapter implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == fWorkspaceButton) {
				handleWorkspaceDirBrowseButtonSelected();
			}
			else if (source == fFileSystemButton) {
				handleWorkingDirBrowseButtonSelected();
			} 
			else if (source == fVariablesButton) {
				handleWorkingDirVariablesButtonSelected();
			} 
			else if(source == fUseDefaultDirButton) {
				//only perform the action if this is the button that was selected
				if(fUseDefaultDirButton.getSelection()) {
					setDefaultWorkingDir();
				}
			} 
			else if(source == fUseOtherDirButton) {
				//only perform the action if this is the button that was selected
				if(fUseOtherDirButton.getSelection()) {
					handleUseOtherWorkingDirButtonSelected();
				}
			}
		}
	}
	
	private WidgetListener fListener = new WidgetListener();
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();	
		Group group = SWTUtil.createGroup(parent, LauncherMessages.WorkingDirectoryBlock_12, 2, 1, GridData.FILL_HORIZONTAL);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(group, IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);
	//default choice
		Composite comp = SWTUtil.createComposite(group, font, 2, 2, GridData.FILL_BOTH, 0, 0);
		fUseDefaultDirButton = SWTUtil.createRadioButton(comp, LauncherMessages.WorkingDirectoryBlock_18);
		fUseDefaultDirButton.addSelectionListener(fListener);
		fWorkingDirText = SWTUtil.createSingleText(comp, 1); 
		fWorkingDirText.addModifyListener(fListener);
		fWorkingDirText.setEnabled(false);
	//user enter choice
		fUseOtherDirButton = SWTUtil.createRadioButton(comp, LauncherMessages.WorkingDirectoryBlock_19);
		fUseOtherDirButton.addSelectionListener(fListener);
		fOtherWorkingText = SWTUtil.createSingleText(comp, 1);
		fOtherWorkingText.addModifyListener(fListener);
	//buttons
		Composite buttonComp = SWTUtil.createComposite(comp, font, 3, 2, GridData.HORIZONTAL_ALIGN_END); 
		fWorkspaceButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_0, null); 
		fWorkspaceButton.addSelectionListener(fListener);
		fFileSystemButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_1, null); 
		fFileSystemButton.addSelectionListener(fListener);
		fVariablesButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_17, null); 
		fVariablesButton.addSelectionListener(fListener);
	}
		
	/**
	 * Show a dialog that lets the user select a working directory
	 */
	private void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LauncherMessages.WorkingDirectoryBlock_7); 
		String currentWorkingDir = getWorkingDirectoryText();
		if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
			File path = new File(currentWorkingDir);
			if (path.exists()) {
				dialog.setFilterPath(currentWorkingDir);
			}		
		}
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			fOtherWorkingText.setText(selectedDirectory);
		}		
	}

	/**
	 * Show a dialog that lets the user select a working directory from 
	 * the workspace
	 */
	private void handleWorkspaceDirBrowseButtonSelected() {
	    IContainer currentContainer= getContainer();
		if (currentContainer == null) {
		    currentContainer = ResourcesPlugin.getWorkspace().getRoot();
		} 
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), currentContainer, false,	LauncherMessages.WorkingDirectoryBlock_4); 
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();		
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.makeRelative().toString();
			setOtherWorkingDirectoryText("${workspace_loc:" + containerName + "}"); //$NON-NLS-1$ //$NON-NLS-2$
		}			
	}
	
	/**
	 * Returns the selected workspace container,or <code>null</code>
	 */
	protected IContainer getContainer() {
		String path = getWorkingDirectoryText();
		if (path.length() > 0) {
		    IResource res = null;
		    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		    if (path.startsWith("${workspace_loc:")) { //$NON-NLS-1$
		        IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
			    try {
                    path = manager.performStringSubstitution(path, false);
                    IContainer[] containers = root.findContainersForLocation(new Path(path));
                    if (containers.length > 0) {
                        res = containers[0];
                    }
                } 
			    catch (CoreException e) {}
			} 
		    else {	    
				res = root.findMember(path);
			}
			if (res instanceof IContainer) {
				return (IContainer)res;
			}
		}
		return null;
	}
		
	/**
	 * The default working dir radio button has been selected.
	 */
	private void handleUseDefaultWorkingDirButtonSelected() {
		fWorkspaceButton.setEnabled(false);
		fOtherWorkingText.setEnabled(false);
		fVariablesButton.setEnabled(false);
		fFileSystemButton.setEnabled(false);
		fUseOtherDirButton.setSelection(false);
	}

	/**
	 * The other working dir radio button has been selected
	 * 
	 * @since 3.2
	 */
	private void handleUseOtherWorkingDirButtonSelected() {
		fOtherWorkingText.setEnabled(true);
		fWorkspaceButton.setEnabled(true);
		fVariablesButton.setEnabled(true);
		fFileSystemButton.setEnabled(true);
		updateLaunchConfigurationDialog();
	}

	/**
	 * The working dir variables button has been selected
	 */
	private void handleWorkingDirVariablesButtonSelected() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		String variableText = dialog.getVariableExpression();
		if (variableText != null) {
			fOtherWorkingText.insert(variableText);
		}
	}
	
	/**
	 * Sets the default working directory
	 */
	protected void setDefaultWorkingDir() {
		try {
			ILaunchConfiguration config = getLaunchConfiguration();
			if (config != null) {
				IJavaProject javaProject = JavaRuntime.getJavaProject(config);
				if (javaProject != null) {
					setDefaultWorkingDirectoryText("${workspace_loc:" + javaProject.getPath().makeRelative().toOSString() + "}");  //$NON-NLS-1$//$NON-NLS-2$
					return;
				}
			}
		} 
		catch (CoreException ce) {}
		setDefaultWorkingDirectoryText(System.getProperty("user.dir")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		// if variables are present, we cannot resolve the directory
		String workingDirPath = getWorkingDirectoryText();
		if (workingDirPath.indexOf("${") >= 0) { //$NON-NLS-1$
			IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
			try {
				manager.validateStringVariables(workingDirPath);
			}
			catch (CoreException e) {
				setErrorMessage(e.getMessage());
				return false;
			}
		} 
		else if (workingDirPath.length() > 0) {
			IContainer container = getContainer();
			if (container == null) {
				File dir = new File(workingDirPath);
				if (dir.isDirectory()) {
					return true;
				}
				setErrorMessage(LauncherMessages.WorkingDirectoryBlock_10); 
				return false;
			}
		} else if (workingDirPath.length() == 0) {
			setErrorMessage(LauncherMessages.WorkingDirectoryBlock_20);
		}
		return true;
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {			
			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
			setDefaultWorkingDir();
			if (wd != null) {
				setOtherWorkingDirectoryText(wd);
			}
		} 
		catch (CoreException e) {
			setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
			JDIDebugUIPlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(fUseDefaultDirButton.getSelection()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		}
		else {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, getWorkingDirectoryText());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.WorkingDirectoryBlock_Working_Directory_8; 
	}
	
	/**
	 * gets the path from the text box that is selected
	 * @return the working directory the user wishes to use
	 * @since 3.2
	 */
	protected String getWorkingDirectoryText() {
		if(fUseDefaultDirButton.getSelection()) {
			return fWorkingDirText.getText().trim();
		}
		return fOtherWorkingText.getText().trim();
	}
	
	/**
	 * sets the default working directory text
	 * @param dir the dir to set the widget to
	 * @since 3.2
	 */
	protected void setDefaultWorkingDirectoryText(String dir) {
		if(dir != null) {
			fWorkingDirText.setText(dir);
			fUseDefaultDirButton.setSelection(true);
			handleUseDefaultWorkingDirButtonSelected();
		}
	}
	
	/**
	 * sets the other dir text
	 * @param dir the new text
	 * @since 3.2
	 */
	protected void setOtherWorkingDirectoryText(String dir) {
		if(dir != null) {
			fOtherWorkingText.setText(dir);
			fUseDefaultDirButton.setSelection(false);
			fUseOtherDirButton.setSelection(true);
			handleUseOtherWorkingDirButtonSelected();
		}
	}
	
	/**
	 * Sets the java project currently specified by the
	 * given launch config, if any.
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}	
	
	/**
	 * Returns the current java project context
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}
	
	/**
	 * Allows this entire block to be enabled/disabled
	 * @param enabled whether to enable it or not
	 */
	protected void setEnabled(boolean enabled) {
		fUseDefaultDirButton.setEnabled(enabled);
		fUseOtherDirButton.setEnabled(enabled);
		if(fOtherWorkingText.isEnabled()) {
			fOtherWorkingText.setEnabled(enabled);
			fWorkspaceButton.setEnabled(enabled);
			fVariablesButton.setEnabled(enabled);
			fFileSystemButton.setEnabled(enabled);
		}
		// in the case where the 'other' text is selected and we want to enable
		if(fUseOtherDirButton.getSelection() && enabled == true) {
			fOtherWorkingText.setEnabled(enabled);
		}
	}
	
}

