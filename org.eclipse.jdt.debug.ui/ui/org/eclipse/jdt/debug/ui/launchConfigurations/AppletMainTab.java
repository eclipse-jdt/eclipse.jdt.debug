package org.eclipse.jdt.debug.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletLaunchConfigurationUtils;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletSelectionDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This tab appears in the LaunchConfigurationDialog for launch configurations that
 * require Java-specific launching information such as a main type and JRE.
 */
public class AppletMainTab extends JavaLaunchConfigurationTab {
		
	// Project UI widgets
	private Label fProjLabel;
	private Text fProjText;
	private Button fProjButton;

	// Main class UI widgets
	private Label fMainLabel;
	private Text fMainText;
	private Button fSearchButton;
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
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
		
		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 3;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);
		
		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText(LauncherMessages.getString("appletlauncher.maintab.projectlabel.name")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProjLabel.setLayoutData(gd);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fProjText.setLayoutData(gd);
		this.fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fProjButton = createPushButton(projComp, LauncherMessages.getString("appletlauncher.maintab.browselabel.name"), null); //$NON-NLS-1$
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		Label spacer = createVerticalSpacer(projComp);
		gd = new GridData();
		gd.horizontalSpan = 3;
		spacer.setLayoutData(gd);
		
		fMainLabel = new Label(projComp, SWT.NONE);
		fMainLabel.setText(LauncherMessages.getString("appletlauncher.maintab.mainclasslabel.name")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fMainLabel.setLayoutData(gd);

		fMainText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fMainText.setLayoutData(gd);
		this.fMainText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
				
		fSearchButton = createPushButton(projComp,LauncherMessages.getString("appletlauncher.maintab.searchlabel.name"), null); //$NON-NLS-1$
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
			}
		});
	}
		
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateMainTypeFromConfig(config);
	}
	
	private void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
		fProjText.setText(projectName);
	}
	
	private void updateMainTypeFromConfig(ILaunchConfiguration config) {
		String mainTypeName = EMPTY_STRING;
		try {
			mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);	
		}	
		fMainText.setText(mainTypeName);
	}
		
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)fMainText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, getProjectOutputDirectory());		
	}
			
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Create some empty space 
	 */
	private Label createVerticalSpacer(Composite comp) {
		return new Label(comp, SWT.NONE);
	}
		
	/**
	 * Show a dialog that lists all main types
	 */
	private void handleSearchButtonSelected() {
		
		IJavaProject javaProject = getJavaProject();		
		Shell shell = getShell();
		AppletSelectionDialog dialog =
			new AppletSelectionDialog(
				shell,
				getLaunchConfigurationDialog(),
				javaProject);
		dialog.setTitle(LauncherMessages.getString("appletlauncher.maintab.selection.applet.dialog.title")); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.getString("appletlauncher.maintab.selection.applet.dialog.message")); //$NON-NLS-1$
		if (dialog.open() == AppletSelectionDialog.CANCEL) {
			return;
		}
		
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}		
		IType type = (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			javaProject = type.getJavaProject();
			fProjText.setText(javaProject.getElementName());
		}
	}
		
	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	private void handleProjectButtonSelected() {
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
	private IJavaProject chooseJavaProject() {
		IJavaProject[] projects;
		try {
			projects= JavaCore.create(getWorkspaceRoot()).getJavaProjects();
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
			projects= new IJavaProject[0];
		}
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LauncherMessages.getString("appletlauncher.maintab.selection.project.dialog.title")); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.getString("appletlauncher.maintab.selection.project.dialog.message")); //$NON-NLS-1$
		dialog.setElements(projects);
		
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == ElementListSelectionDialog.OK) {			
			return (IJavaProject) dialog.getFirstResult();
		}			
		return null;		
	}
	
	/**
	 * Return the IJavaProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	private IJavaProject getJavaProject() {
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
	 * @see ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		
		setErrorMessage(null);
		setMessage(null);
		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.getString("appletlauncher.maintab.project.error.doesnotexist")); //$NON-NLS-1$
				return false;
			}
		}
		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.getString("appletlauncher.maintab.type.error.doesnotexist")); //$NON-NLS-1$
			return false;
		}
		IJavaProject jp = getJavaProject();
		if (jp != null) {
			// only verify type exists if Java project is specified
			try {
				AppletLaunchConfigurationUtils.getMainType(name, jp);
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				return false;
			}
		}	
		
		return true;
	}
	
	/**
	 * Initialize default attribute values based on the
	 * given Java element.
	 */
	private void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		initializeJavaProject(javaElement, config);
		initializeMainTypeAndName(javaElement, config);
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement je = getContext();
		if (je == null) {
			initializeHardCodedDefaults(config);
		} else {
			initializeDefaults(je, config);
		}
	}

	/**
	 * Set the main type & name attributes on the working copy based on the IJavaElement
	 */
	private void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		if (javaElement instanceof IMember) {
			IMember member = (IMember)javaElement;
			if (member.isBinary()) {
				javaElement = member.getClassFile();
			} else {
				javaElement = member.getCompilationUnit();
			}
		}
		if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
			if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
				ICompilationUnit cu= (ICompilationUnit) javaElement;
				IType mainType= cu.getType(Signature.getQualifier(cu.getElementName()));
				if (mainType.exists()) {
					name = mainType.getFullyQualifiedName();
				}
			} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
				try {
					IType mainType= ((IClassFile)javaElement).getType();
					name = mainType.getFullyQualifiedName();
				} catch(JavaModelException e) {
				}
			}
		}
		if (name != null) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, name);
			if (name.length() > 0) {
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(index + 1);
				}		
				name = getLaunchConfigurationDialog().generateName(name);
				config.rename(name);
			}
		}
	}

	/**
	 * Set the VM attributes on the working copy based on the workbench default VM.
	 */
	private void initializeDefaultVM(ILaunchConfigurationWorkingCopy config) {
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		if (vmInstall == null) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmInstall.getName());
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmInstall.getVMInstallType().getId());
		}
	}
	
	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	private void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		initializeDefaultVM(config);
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("appletlauncher.maintab.name"); //$NON-NLS-1$
	}
			
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}

	private String getProjectOutputDirectory() {
		IJavaProject jproject = getJavaProject();
		if (jproject == null) {
			return EMPTY_STRING;
		}
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath outputLocation = jproject.getOutputLocation();
			IResource resource = root.findMember(outputLocation);
			IPath path = resource.getLocation();
			if (path == null)  {
				return EMPTY_STRING;
			}
			return path.toOSString();
		} catch(JavaModelException e) {
			return EMPTY_STRING;
		}
	}

}

