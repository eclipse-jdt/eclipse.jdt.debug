package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JavaConnectTab extends JavaLaunchConfigurationTab {

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
	
	// Connector combo
	private Combo fConnectorCombo;
	private IVMConnector[] fConnectors = JavaRuntime.getVMConnectors();
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
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
		fProjLabel.setText(LauncherMessages.getString("JavaConnectTab.&Project__2")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fProjButton = new Button(projComp, SWT.PUSH);
		fProjButton.setText(LauncherMessages.getString("JavaConnectTab.&Browse_3")); //$NON-NLS-1$
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		createVerticalSpacer(comp);		
		
		fHostLabel = new Label(comp, SWT.NONE);
		fHostLabel.setText(LauncherMessages.getString("JavaConnectTab.&Host_name__4")); //$NON-NLS-1$

		fHostText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHostText.setLayoutData(gd);
		fHostText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fPortLabel = new Label(comp, SWT.NONE);
		fPortLabel.setText(LauncherMessages.getString("JavaConnectTab.P&ort_#__5")); //$NON-NLS-1$

		fPortText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPortText.setLayoutData(gd);
		fPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fAllowTerminateButton = new Button(comp, SWT.CHECK);
		fAllowTerminateButton.setText(LauncherMessages.getString("JavaConnectTab.&Allow_termination_of_remote_VM_6")); //$NON-NLS-1$
		fAllowTerminateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		createVerticalSpacer(comp);
		
		Composite connectorComp = new Composite(comp,SWT.NONE);
		GridLayout y = new GridLayout();
		y.numColumns = 2;
		y.marginHeight = 0;
		y.marginWidth = 0;
		connectorComp.setLayout(y);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		connectorComp.setLayoutData(gd);
		
		Label l = new Label(connectorComp, SWT.NONE);
		l.setText(LauncherMessages.getString("JavaConnectTab.Connect&ion_Type__7")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);
		
		fConnectorCombo = new Combo(connectorComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fConnectorCombo.setLayoutData(gd);
		String[] names = new String[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			names[i] = fConnectors[i].getName();
		}
		fConnectorCombo.setItems(names);
	}

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateHostNameFromConfig(config);
		updatePortNumberFromConfig(config);
		updateAllowTerminateFromConfig(config);
		updateConnectionFromConfig(config);
	}
	
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = ""; //$NON-NLS-1$
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
		fProjText.setText(projectName);
	}
	
	protected void updateHostNameFromConfig(ILaunchConfiguration config) {
		String hostName = ""; //$NON-NLS-1$
		try {
			hostName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, EMPTY_STRING);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}		
		fHostText.setText(hostName);
	}

	protected void updatePortNumberFromConfig(ILaunchConfiguration config) {
		int portNumber = 8000;
		try {
			portNumber = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PORT_NUMBER, 8000);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}	
		fPortText.setText(String.valueOf(portNumber));	
	}

	protected void updateAllowTerminateFromConfig(ILaunchConfiguration config) {
		boolean allowTerminate = false;
		try {
			allowTerminate = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);	
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
		fAllowTerminateButton.setSelection(allowTerminate);	
	}

	protected void updateConnectionFromConfig(ILaunchConfiguration config) {
		String id = null;
		try {
			id = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, JavaRuntime.getDefaultVMConnector().getIdentifier());
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}	
		fConnectorCombo.setText(JavaRuntime.getVMConnector(id).getName());	
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
		
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, fHostText.getText().trim());
		String portString = fPortText.getText();
		int port = -1;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException nfe) {				
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PORT_NUMBER, port);		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, fAllowTerminateButton.getSelection());
		
		int index = fConnectorCombo.getSelectionIndex();
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, fConnectors[index].getIdentifier());
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
			JDIDebugUIPlugin.log(e);
			projects= new IJavaProject[0];
		}
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LauncherMessages.getString("JavaConnectTab.Project_selection_10")); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.getString("JavaConnectTab.Choose_a_project_to_constrain_the_search_for_main_types_11")); //$NON-NLS-1$
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

	/**
	 * Initialize default settings for the given Java element
	 */
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		initializeJavaProject(javaElement, config);
		initializeName(javaElement, config);
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement == null) {
			initializeHardCodedDefaults(config);
		} else {
			initializeDefaults(javaElement, config);
		}
	}

	/**
	 * Find the first instance of a type, compilation unit, class file or project in the
	 * specified element's parental hierarchy, and use this as the default name.
	 */
	protected void initializeName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name = ""; //$NON-NLS-1$
		try {
			IResource resource = javaElement.getUnderlyingResource();
			name = resource.getName();
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(0, index);
			}
			name = getLaunchConfigurationDialog().generateName(name);				
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
		}
		config.rename(name);
	}

	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, "localhost"); //$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PORT_NUMBER, 8000);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
	}
	
	/**
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {
		
		setErrorMessage(null);
		setMessage(null);

		// project		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.getString("JavaConnectTab.Project_does_not_exist_14")); //$NON-NLS-1$
				return false;
			}
		}
				
		// Host
		String hostName = fHostText.getText().trim();
		if (hostName.length() < 1) {
			setErrorMessage(LauncherMessages.getString("JavaConnectTab.Host_name_not_specified_15")); //$NON-NLS-1$
			return false;
		}
		if (hostName.indexOf(' ') > -1) {
			setErrorMessage(LauncherMessages.getString("JavaConnectTab.Invalid_host_name_16")); //$NON-NLS-1$
			return false;
		}
		
		// Port
		String portString = fPortText.getText().trim();
		int portNumber = -1;
		try {
			portNumber = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			setErrorMessage(LauncherMessages.getString("JavaConnectTab.Invalid_port_number_specified_17")); //$NON-NLS-1$
			return false;
		}
		if (portNumber == Integer.MIN_VALUE) {
			setErrorMessage(LauncherMessages.getString("JavaConnectTab.Port_number_not_specified_18")); //$NON-NLS-1$
			return false;
		}
		if (portNumber < 1) {
			setErrorMessage(LauncherMessages.getString("JavaConnectTab.Invalid_port_number_specified_19")); //$NON-NLS-1$
			return false;
		}
				
		return true;
	}
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaConnectTab.Conn&ect_20"); //$NON-NLS-1$
	}			
}
