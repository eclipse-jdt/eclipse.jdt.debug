package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class ConnectTab implements ILaunchConfigurationTab {

	// The launch configuration dialog that owns this tab
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	// The launch config working copy providing the values shown on this tab
	private ILaunchConfigurationWorkingCopy fWorkingCopy;

	// Project UI widgets
	private Label fProjLabel;
	private Text fProjText;
	private Button fProjButton;
	
	// Host name UI widgets
	private Label fHostLabel;
	private Text fHostText;

	// Port # UI widgets
	private Label fPortLabel;
	private Text fPortText;

	// Allow terminate UI widgets
	private Button fAllowTerminateButton;

	// Flag that when true, prevents the owning dialog's status area from getting updated.
	// Used when multiple config attributes are getting updated at once.
	private boolean fBatchUpdate = false;
	
	private static final String EMPTY_STRING = "";
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(ILaunchConfigurationDialog, TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp);

		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);
		
		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText("Project");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromProject();
			}
		});
		
		fProjButton = new Button(projComp, SWT.PUSH);
		fProjButton.setText("Browse");
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		createVerticalSpacer(comp);		
		
		fHostLabel = new Label(comp, SWT.NONE);
		fHostLabel.setText("Host name");

		fHostText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHostText.setLayoutData(gd);
		fHostText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromHostName();
			}
		});
		
		fPortLabel = new Label(comp, SWT.NONE);
		fPortLabel.setText("Port #");

		fPortText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPortText.setLayoutData(gd);
		fPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromPortNumber();
			}
		});

		fAllowTerminateButton = new Button(comp, SWT.CHECK);
		fAllowTerminateButton.setText("Allow termination of remote VM");
		fAllowTerminateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				updateConfigFromAllowTerminate();
			}
		});
				
		return comp;
	}

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
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
		updateProjectFromConfig(config);
		updateHostNameFromConfig(config);
		updatePortNumberFromConfig(config);
		updateAllowTerminateFromConfig(config);
	}
	
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		try {
			String projectName = config.getAttribute(JavaDebugUI.PROJECT_ATTR, EMPTY_STRING);
			fProjText.setText(projectName);
		} catch (CoreException ce) {
		}
	}
	
	protected void updateHostNameFromConfig(ILaunchConfiguration config) {
		try {
			String hostName = config.getAttribute(JavaDebugUI.HOSTNAME_ATTR, EMPTY_STRING);
			fHostText.setText(hostName);
		} catch (CoreException ce) {			
		}		
	}

	protected void updatePortNumberFromConfig(ILaunchConfiguration config) {
		try {
			int portNumber = config.getAttribute(JavaDebugUI.PORT_ATTR, 8000);
			fPortText.setText(String.valueOf(portNumber));
		} catch (CoreException ce) {			
		}		
	}

	protected void updateAllowTerminateFromConfig(ILaunchConfiguration config) {
		try {
			boolean allowTerminate = config.getAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, false);
			fAllowTerminateButton.setSelection(allowTerminate);
		} catch (CoreException ce) {			
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
	
	protected void refreshStatus() {
		if (!isBatchUpdate()) {
			getLaunchDialog().refreshStatus();
		}
	}
	
	protected void updateConfigFromProject() {
		if (getWorkingCopy() != null) {
			getWorkingCopy().setAttribute(JavaDebugUI.PROJECT_ATTR, (String)fProjText.getText());
			refreshStatus();			
		}
	}
	
	protected void updateConfigFromHostName() {
		if (getWorkingCopy() != null) {
			getWorkingCopy().setAttribute(JavaDebugUI.HOSTNAME_ATTR, (String)fHostText.getText());
			refreshStatus();			
		}		
	}

	protected void updateConfigFromPortNumber() {
		if (getWorkingCopy() != null) {
			getWorkingCopy().setAttribute(JavaDebugUI.PORT_ATTR, (String)fPortText.getText());
			refreshStatus();			
		}		
	}

	protected void updateConfigFromAllowTerminate() {
		if (getWorkingCopy() != null) {
			boolean allowTerminate = fAllowTerminateButton.getSelection();
			getWorkingCopy().setAttribute(JavaDebugUI.ALLOW_TERMINATE_ATTR, allowTerminate);
			refreshStatus();			
		}				
	}
	
	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		IJavaProject project = chooseJavaProject();
		if (project == null) {
			return;
		}
		
		String projectName = project.getElementName();
		fProjText.setText(projectName);		
	}
	
	/**
	 * Realize a Java Project selection dialog and return the first selected project,
	 * or null if there was none.
	 */
	protected IJavaProject chooseJavaProject() {
		IJavaProject[] projects;
		try {
			projects= JavaCore.create(getWorkspaceRoot()).getJavaProjects();
		} catch (JavaModelException e) {
			JavaPlugin.log(e.getStatus());
			projects= new IJavaProject[0];
		}
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project selection");
		dialog.setMessage("Choose a project to constrain the search for main types");
		dialog.setElements(projects);
		
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == dialog.OK) {			
			return (IJavaProject) dialog.getFirstResult();
		}			
		return null;		
	}
	
	/**
	 * Return the IJavaProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected IJavaProject getJavaProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getJavaModel().getJavaProject(projectName);		
	}
	
	/**
	 * Convenience method to get the shell.  It is important that the shell be the one 
	 * associated with the launch configuration dialog, and not the active workbench
	 * window.
	 */
	private Shell getShell() {
		return fProjLabel.getShell();
	}
	
	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Convenience method to get access to the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

}
