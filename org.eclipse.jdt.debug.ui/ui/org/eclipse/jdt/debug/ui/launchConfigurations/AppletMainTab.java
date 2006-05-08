/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletLaunchConfigurationUtils;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This tab appears for Java applet launch configurations and allows the user to edit
 * attributes such as the applet class to launch and its owning project, if any.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.1
 */
public class AppletMainTab extends SharedJavaMainTab {
	
	// Applet viewer UI widgets
	private Text fAppletViewerClassText;
	private Button fAppletViewerClassDefaultButton;
	
	/**
	 * Creates the applet viewer control area
	 * @param parent the composite to add this control to
	 */
	private void createAppletViewerControl(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText(LauncherMessages.AppletMainTab_1);
		GridLayout layout = new GridLayout(2, true);
		layout.verticalSpacing = 0;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setLayout(layout);
		group.setFont(font);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fAppletViewerClassText= new Text(group, SWT.SINGLE | SWT.BORDER);
		fAppletViewerClassText.setLayoutData(gd);
		fAppletViewerClassText.setFont(font);
		fAppletViewerClassText.addModifyListener(getDefaultListener());
		createVerticalSpacer(group, 1);
		fAppletViewerClassDefaultButton= createCheckButton(group, LauncherMessages.AppletMainTab_2); 
		gd= new GridData();
		gd.horizontalSpan= 2;
		fAppletViewerClassDefaultButton.setLayoutData(gd);
		fAppletViewerClassDefaultButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				handleAppletViewerClassDefaultSelected();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font= parent.getFont();
		Composite projComp= new Composite(parent, SWT.NONE);
		setControl(projComp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_MAIN_TAB);
		GridLayout projLayout= new GridLayout();
		projLayout.verticalSpacing = 0;
		projComp.setLayout(projLayout);
		projComp.setFont(font);
		createProjectEditor(projComp);
		createVerticalSpacer(projComp, 1);
		createMainTypeEditor(projComp, LauncherMessages.appletlauncher_maintab_mainclasslabel_name, null);
		createVerticalSpacer(projComp, 1);
		createAppletViewerControl(projComp);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.appletlauncher_maintab_name;
	}

	/**
	 * When the "use default" button is selected, update the "applet viewer class" text.
	 */
	private void handleAppletViewerClassDefaultSelected() {
		setAppletViewerTextEnabledState();
		if (isDefaultAppletViewerClassName()) {
			fAppletViewerClassText.setText(IJavaLaunchConfigurationConstants.DEFAULT_APPLETVIEWER_CLASS);
		}//end if 
		else {
			fAppletViewerClassText.setText(EMPTY_STRING);
		}//end else
	}
	
	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {
		IJavaElement[] scope= null;
		IJavaProject project = getJavaProject();
		if (project == null) {
			try {
				scope = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
			}//end try 
			catch (JavaModelException e) {
				setErrorMessage(e.getMessage());
				return;
			}//end catch
		}//end if 
		else {
			scope = new IJavaElement[]{project};
		}//end else
		IType[] types = null;
		try {
			types = AppletLaunchConfigurationUtils.findApplets(getLaunchConfigurationDialog(), scope);
		} 
		catch (InterruptedException e) {return;} 
		catch (InvocationTargetException e) {
			setErrorMessage(e.getTargetException().getMessage());
			return;
		}//end catch
		SelectionDialog dialog = null;
		try {
			dialog = JavaUI.createTypeDialog(
					getShell(),
					getLaunchConfigurationDialog(),
					SearchEngine.createJavaSearchScope(types),
					IJavaElementSearchConstants.CONSIDER_CLASSES, 
					false,
					"**"); //$NON-NLS-1$
		}//end try 
		catch (JavaModelException e) {
			setErrorMessage(e.getMessage());
			return;
		}//end catch
		dialog.setTitle(LauncherMessages.appletlauncher_maintab_selection_applet_dialog_title); 
 		dialog.setMessage(LauncherMessages.appletlauncher_maintab_selection_applet_dialog_message); 
		if (dialog.open() == Window.CANCEL) {
			return;
		}//end if
		Object[] results= dialog.getResult();	
		IType type= (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			fProjText.setText(type.getJavaProject().getElementName());
		}//end if
	}

	/**
	 * Initialize the appletviewer class name attribute.
	 */
	private void initializeAppletViewerClass(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, (String)null);
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
	 * Set the VM attributes on the working copy based on the workbench default VM.
	 */
	private void initializeDefaultVM(ILaunchConfigurationWorkingCopy config) {
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall == null) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		}//end if 
		else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmInstall.getName());
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmInstall.getVMInstallType().getId());
		}//end else
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateMainTypeFromConfig(config);
		updateAppletViewerClassNameFromConfig(config);
	}

	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	private void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		initializeDefaultVM(config);
	}
	
	/**
	 * Returns whether the default appletviewer is to be used
	 */
	private boolean isDefaultAppletViewerClassName() {
		return fAppletViewerClassDefaultButton.getSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		String name= fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.appletlauncher_maintab_project_error_doesnotexist); 
				return false;
			}//end if
		}//end if
		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.appletlauncher_maintab_type_error_doesnotexist); 
			return false;
		}//end if
		name = fAppletViewerClassText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.AppletMainTab_3);  
			return false;			
		}//end if
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText());
		mapResources(config);
		performApplyAppletViewerClassName(config);		
	}
	
	/**
	 * Set the current appletviewer class name on the specified working copy.
	 */
	private void performApplyAppletViewerClassName(ILaunchConfigurationWorkingCopy config) {
		String appletViewerClassName= null;
		if (!isDefaultAppletViewerClassName()) {
			appletViewerClassName= fAppletViewerClassText.getText().trim();
			if (appletViewerClassName.length() <= 0) {
				appletViewerClassName= null;
			}//end if
		}//end if
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, appletViewerClassName);
	}
	
	/**
	 * Set the appropriate enabled state for the appletviewer text widget.
	 */
	private void setAppletViewerTextEnabledState() {
		if (isDefaultAppletViewerClassName()) {
			fAppletViewerClassText.setEnabled(false);
		}//end if 
		else {
			fAppletViewerClassText.setEnabled(true);
		}//end else
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement je= getContext();
		if (je == null) {
			initializeHardCodedDefaults(config);
		}//end if 
		else {
			initializeDefaults(je, config);
		}//end else
	}

	/**
	 * updates the applet class name from the specified launch configuration
	 * @param config the config to load the class name attribute from 
	 */
	private void updateAppletViewerClassNameFromConfig(ILaunchConfiguration config) {
		String appletViewerClassName = EMPTY_STRING;
		try {
			appletViewerClassName= config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, EMPTY_STRING);
			if (appletViewerClassName.equals(EMPTY_STRING)) {
				fAppletViewerClassText.setText(IJavaLaunchConfigurationConstants.DEFAULT_APPLETVIEWER_CLASS);
				fAppletViewerClassDefaultButton.setSelection(true);
			}//end if 
			else {
				fAppletViewerClassText.setText(appletViewerClassName);
				fAppletViewerClassDefaultButton.setSelection(false);
			}//end else
			setAppletViewerTextEnabledState();
		}//end try 
		catch (CoreException ce) {JDIDebugUIPlugin.log(ce);}
	}
}//end class
