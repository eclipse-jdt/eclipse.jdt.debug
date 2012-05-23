/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletLaunchConfigurationUtils;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
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

/**
 * This tab appears for Java applet launch configurations and allows the user to edit
 * attributes such as the applet class to launch and its owning project, if any.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.1
 * @noextend This class is not intended to be sub-classed by clients.
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
		Group group = SWTFactory.createGroup(parent, LauncherMessages.AppletMainTab_1, 2, 1, GridData.FILL_HORIZONTAL);
		Composite comp = SWTFactory.createComposite(group, font, 2, 2, GridData.FILL_BOTH, 0, 0);
		fAppletViewerClassText= SWTFactory.createSingleText(comp, 2); 
		fAppletViewerClassText.addModifyListener(getDefaultListener());
		createVerticalSpacer(comp, 1);
		fAppletViewerClassDefaultButton= createCheckButton(comp, LauncherMessages.AppletMainTab_2);
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
		Composite projComp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH); 
		((GridLayout)projComp.getLayout()).verticalSpacing = 0;
		createProjectEditor(projComp);
		createVerticalSpacer(projComp, 1);
		createMainTypeEditor(projComp, LauncherMessages.appletlauncher_maintab_mainclasslabel_name);
		createVerticalSpacer(projComp, 1);
		createAppletViewerControl(projComp);
		setControl(projComp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_MAIN_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
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
		} 
		else {
			fAppletViewerClassText.setText(EMPTY_STRING);
		}
	}
	
	/**
	 * Show a dialog that lists all main types
	 */
	@Override
	protected void handleSearchButtonSelected() {
		IJavaElement[] scope= null;
		IJavaProject project = getJavaProject();
		if (project == null) {
			try {
				scope = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
			}
			catch (JavaModelException e) {
				setErrorMessage(e.getMessage());
				return;
			}
		}
		else {
			scope = new IJavaElement[]{project};
		}
		IType[] types = null;
		try {
			types = AppletLaunchConfigurationUtils.findApplets(getLaunchConfigurationDialog(), scope);
		} 
		catch (InterruptedException e) {return;} 
		catch (InvocationTargetException e) {
			setErrorMessage(e.getTargetException().getMessage());
			return;
		}
		DebugTypeSelectionDialog dialog = new DebugTypeSelectionDialog(getShell(), types, LauncherMessages.appletlauncher_maintab_selection_applet_dialog_title); 
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		Object[] results = dialog.getResult();	
		IType type = (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			fProjText.setText(type.getJavaProject().getElementName());
		}
	}

	/**
	 * Initialize the applet viewer class name attribute.
	 * @param config the configuration
	 */
	private void initializeAppletViewerClass(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, (String)null);
	}
	
	/**
	 * Initialize default attribute values based on the
	 * given Java element.
	 * @param javaElement the Java element
	 * @param config the configuration
	 */
	private void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		initializeJavaProject(javaElement, config);
		initializeMainTypeAndName(javaElement, config);
		initializeAppletViewerClass(config);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateMainTypeFromConfig(config);
		updateAppletViewerClassNameFromConfig(config);
	}
	
	/**
	 * Returns whether the default applet viewer is to be used
	 * @return if the viewer should be used
	 */
	private boolean isDefaultAppletViewerClassName() {
		return fAppletViewerClassDefaultButton.getSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		String name= fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.appletlauncher_maintab_project_error_doesnotexist); 
				return false;
			}
		}
		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.appletlauncher_maintab_type_error_doesnotexist); 
			return false;
		}
		name = fAppletViewerClassText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.AppletMainTab_3);  
			return false;			
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText());
		mapResources(config);
		String appletViewerClassName= null;
		if (!isDefaultAppletViewerClassName()) {
			appletViewerClassName= fAppletViewerClassText.getText().trim();
			if (appletViewerClassName.length() <= 0) {
				appletViewerClassName= null;
			}
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, appletViewerClassName);
	}

	/**
	 * Set the appropriate enabled state for the applet viewer text widget.
	 */
	private void setAppletViewerTextEnabledState() {
		if (isDefaultAppletViewerClassName()) {
			fAppletViewerClassText.setEnabled(false);
		}
		else {
			fAppletViewerClassText.setEnabled(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement je= getContext();
		if (je != null) {
			initializeDefaults(je, config);
		}
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
			}
			else {
				fAppletViewerClassText.setText(appletViewerClassName);
				fAppletViewerClassDefaultButton.setSelection(false);
			}
			setAppletViewerTextEnabledState();
		}
		catch (CoreException ce) {JDIDebugUIPlugin.log(ce);}
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	@Override
	public String getId() {
		return "org.eclipse.jdt.debug.ui.appletMainTab"; //$NON-NLS-1$
	}
}
