package org.eclipse.jdt.debug.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This tab appears for java applet launch configurations and allows the user to edit
 * attributes such as the applet class to launch and its owning project, if any.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.1
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
	
	// Applet viewer UI widgets
	private Label fAppletViewerClassLabel;
	private Text fAppletViewerClassText;
	private Button fAppletViewerClassDefaultButton;
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_MAIN_TAB);
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
		projComp.setFont(font);
		
		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText(LauncherMessages.getString("appletlauncher.maintab.projectlabel.name")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProjLabel.setLayoutData(gd);
		fProjLabel.setFont(font);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fProjText.setLayoutData(gd);
		fProjText.setFont(font);
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
		fMainLabel.setFont(font);

		fMainText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fMainText.setLayoutData(gd);
		fMainText.setFont(font);
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
		
		createVerticalSpacer(projComp);
		
		fAppletViewerClassLabel = new Label(comp, SWT.NONE);
		fAppletViewerClassLabel.setText(LauncherMessages.getString("AppletMainTab.Name_of_appletviewer_class__1")); //$NON-NLS-1$
		fAppletViewerClassLabel.setFont(font);
		
		fAppletViewerClassText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fAppletViewerClassText.setLayoutData(gd);
		fAppletViewerClassText.setFont(font);
		fAppletViewerClassText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fAppletViewerClassDefaultButton = new Button(comp, SWT.CHECK);
		fAppletViewerClassDefaultButton.setFont(font);
		fAppletViewerClassDefaultButton.setText(LauncherMessages.getString("AppletMainTab.Use_default_appletviewer_class_2")); //$NON-NLS-1$
		fAppletViewerClassDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setAppletViewerTextEnabledState();
				if (isDefaultAppletViewerClassName()) {
					fAppletViewerClassText.setText(IJavaLaunchConfigurationConstants.DEFAULT_APPLETVIEWER_CLASS);
				} else {
					fAppletViewerClassText.setText(EMPTY_STRING);
				}
			}
		});
	}
		
	/**
	 * Set the appropriate enabled state for the appletviewqer text widget.
	 */
	protected void setAppletViewerTextEnabledState() {
		if (isDefaultAppletViewerClassName()) {
			fAppletViewerClassText.setEnabled(false);
		} else {
			fAppletViewerClassText.setEnabled(true);
		}
	}
	
	/**
	 * Returns whether the default appletviewer is to be used
	 */
	protected boolean isDefaultAppletViewerClassName() {
		return fAppletViewerClassDefaultButton.getSelection();
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateMainTypeFromConfig(config);
		updateAppletViewerClassNameFromConfig(config);
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
	
	private void updateAppletViewerClassNameFromConfig(ILaunchConfiguration config) {
		String appletViewerClassName = null;
		try {
			appletViewerClassName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, (String)null);
			if (appletViewerClassName == null) {
				fAppletViewerClassText.setText(IJavaLaunchConfigurationConstants.DEFAULT_APPLETVIEWER_CLASS);
				fAppletViewerClassDefaultButton.setSelection(true);
			} else {
				fAppletViewerClassText.setText(appletViewerClassName);
				fAppletViewerClassDefaultButton.setSelection(false);
			}
			setAppletViewerTextEnabledState();
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);				
		}
	}
		
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)fMainText.getText());
		performApplyAppletViewerClassName(config);		
	}
	
	/**
	 * Set the current appletviewer class name on the specified working copy.
	 */
	private void performApplyAppletViewerClassName(ILaunchConfigurationWorkingCopy config) {
		String appletViewerClassName = null;
		if (!isDefaultAppletViewerClassName()) {
			appletViewerClassName = fAppletViewerClassText.getText().trim();
			if (appletViewerClassName.length() <= 0) {
				appletViewerClassName = null;
			}
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, appletViewerClassName);
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
		
		// Verify project
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.getString("appletlauncher.maintab.project.error.doesnotexist")); //$NON-NLS-1$
				return false;
			}
		}
		
		// Verify applet class
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
		
		// Verify appletviewer class
		name = fAppletViewerClassText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.getString("AppletMainTab.Appletviewer_class_must_be_specified_3"));  //$NON-NLS-1$
			return false;			
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
		initializeAppletViewerClass(config);
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
	 * Initialize the appletviewer class name attribute.
	 */
	private void initializeAppletViewerClass(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, (String)null);
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

}

