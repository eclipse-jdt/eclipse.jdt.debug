/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.internal.launching.JavaMigrationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Provides general widgets and methods for a Java type launch configuration 
 * 'Main' tab. 
 * Currently there are only three Java type launch configurations: Local Java Application, Applet, and Remote Debug
 * which this class is used by
 * 
 * @since 3.2
 */
public abstract class AbstractJavaMainTab extends JavaLaunchTab {

/**
 * A listener which handles widget change events for the controls
 * in this tab.
 */
private class WidgetListener implements ModifyListener, SelectionListener {
	
	public void modifyText(ModifyEvent e) {
		updateLaunchConfigurationDialog();
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/}
	
	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		if (source == fProjButton) {
			handleProjectButtonSelected();
		}
		else {
			updateLaunchConfigurationDialog();
		}
	}
}
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	//Project UI widgets
	protected Text fProjText;

	private Button fProjButton;
	
	private WidgetListener fListener = new WidgetListener();
	
	/**
	 * chooses a project for the type of java launch config that it is
	 * @return
	 */
	private IJavaProject chooseJavaProject() {
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LauncherMessages.AbstractJavaMainTab_4); 
		dialog.setMessage(LauncherMessages.AbstractJavaMainTab_3); 
		try {
			dialog.setElements(JavaCore.create(getWorkspaceRoot()).getJavaProjects());
		}
		catch (JavaModelException jme) {JDIDebugUIPlugin.log(jme);}
		IJavaProject javaProject= getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {			
			return (IJavaProject) dialog.getFirstResult();
		}		
		return null;		
	}
	
	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent the parent composite
	 */
	protected void createProjectEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, LauncherMessages.AbstractJavaMainTab_0, 2, 1, GridData.FILL_HORIZONTAL);
		fProjText = SWTFactory.createSingleText(group, 1);
		fProjText.addModifyListener(fListener);
		ControlAccessibleListener.addListener(fProjText, group.getText());
		fProjButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null); 
		fProjButton.addSelectionListener(fListener);
	}	
	
	/**
	 * returns the default listener from this class. For all subclasses
	 * this listener will only provide the functionality of updating the current tab
	 * 
	 * @return a widget listener
	 */
	protected WidgetListener getDefaultListener() {
		return fListener;
	}
	
	/**
	 * Convenience method to get access to the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
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
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		super.initializeFrom(config);
	}
	
	/**
	 * updates the project text field form the configuration
	 * @param config the configuration we are editing
	 */
	private void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		}
		catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fProjText.setText(projectName);
	}
	
	/**
	 * Maps the config to associated java resource
	 * 
	 * @param config
	 */
	protected void mapResources(ILaunchConfigurationWorkingCopy config)  {
		try {
		//CONTEXTLAUNCHING
			IJavaProject javaProject = getJavaProject();
			if (javaProject != null && javaProject.exists() && javaProject.isOpen()) {
				JavaMigrationDelegate.updateResourceMapping(config);
			}
		} catch(CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
	}	
	
}
