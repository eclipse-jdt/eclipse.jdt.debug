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
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
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
	
	private static final String EMPTY_STRING = "";
	
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
		fProjLabel.setText("&Project:");
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
		fProjButton.setText("&Browse...");
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		createVerticalSpacer(comp);		
		
		fHostLabel = new Label(comp, SWT.NONE);
		fHostLabel.setText("&Host name:");

		fHostText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHostText.setLayoutData(gd);
		fHostText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fPortLabel = new Label(comp, SWT.NONE);
		fPortLabel.setText("P&ort #:");

		fPortText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPortText.setLayoutData(gd);
		fPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fAllowTerminateButton = new Button(comp, SWT.CHECK);
		fAllowTerminateButton.setText("&Allow termination of remote VM");
		fAllowTerminateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
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
	}
	
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = "";
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		} catch (CoreException ce) {
		}
		fProjText.setText(projectName);
	}
	
	protected void updateHostNameFromConfig(ILaunchConfiguration config) {
		String hostName = "";
		try {
			hostName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, EMPTY_STRING);
		} catch (CoreException ce) {			
		}		
		fHostText.setText(hostName);
	}

	protected void updatePortNumberFromConfig(ILaunchConfiguration config) {
		int portNumber = 8000;
		try {
			portNumber = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PORT_NUMBER, 8000);
		} catch (CoreException ce) {			
		}	
		fPortText.setText(String.valueOf(portNumber));	
	}

	protected void updateAllowTerminateFromConfig(ILaunchConfiguration config) {
		boolean allowTerminate = false;
		try {
			allowTerminate = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);	
		} catch (CoreException ce) {			
		}
		fAllowTerminateButton.setSelection(allowTerminate);	
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
		String name = "";
		try {
			IResource resource = javaElement.getUnderlyingResource();
			name = resource.getName();
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(0, index);
			}
			name = getLaunchConfigurationDialog().generateName(name);				
		} catch (JavaModelException jme) {
		}
		config.rename(name);
	}

	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_HOSTNAME, "localhost");
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
				setErrorMessage("Project does not exist.");
				return false;
			}
		}
				
		// Host
		String hostName = fHostText.getText().trim();
		if (hostName.length() < 1) {
			setErrorMessage("Host name not specified.");
			return false;
		}
		if (hostName.indexOf(' ') > -1) {
			setErrorMessage("Invalid host name.");
			return false;
		}
		
		// Port
		String portString = fPortText.getText().trim();
		int portNumber = -1;
		try {
			portNumber = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid port number specified");
			return false;
		}
		if (portNumber == Integer.MIN_VALUE) {
			setErrorMessage("Port number not specified.");
			return false;
		}
		if (portNumber < 1) {
			setErrorMessage("Invalid port number specified");
			return false;
		}
				
		return true;
	}	
}
