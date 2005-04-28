/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.internal.debug.ui.launcher.MainTypeSelectionDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * A launch configuration tab that displays and edits project and
 * main type name launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class JavaMainTab extends JavaLaunchConfigurationTab {
		
	// Project UI widgets
	protected Text fProjText;
	protected Button fProjButton;

	// Main class UI widgets
	protected Text fMainText;
	protected Button fSearchButton;
	protected Button fSearchExternalJarsCheckButton;
	protected Button fConsiderInheritedMainButton;
	protected Button fStopInMainCheckButton;
			
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * A listener which handles widget change events for the controls
	 * in this tab.
	 */
	private class WidgetListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fProjButton) {
				handleProjectButtonSelected();
			} else if (source == fSearchButton) {
				handleSearchButtonSelected();
			} else {
				updateLaunchConfigurationDialog();
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	private WidgetListener fListener = new WidgetListener();
	
	/**
	 * Boolean launch configuration attribute indicating that external jars (on
	 * the runtime classpath) should be searched when looking for a main type.
	 * Default value is <code>false</code>.
	 * 
	 * @since 2.1
	 */
	public static final String ATTR_INCLUDE_EXTERNAL_JARS = IJavaDebugUIConstants.PLUGIN_ID + ".INCLUDE_EXTERNAL_JARS"; //$NON-NLS-1$
	
	/**
	 * Boolean launch configuration attribute indicating whether types inheriting
	 * a main method should be considerd when searching for a main type.
	 * Default value is <code>false</code>.
	 * 
	 * @since 3.0
	 */
	public static final String ATTR_CONSIDER_INHERITED_MAIN = IJavaDebugUIConstants.PLUGIN_ID + ".CONSIDER_INHERITED_MAIN"; //$NON-NLS-1$	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createMainTypeEditor(comp);
		createVerticalSpacer(comp, 1);
		
		fStopInMainCheckButton = createCheckButton(comp, LauncherMessages.JavaMainTab_St_op_in_main_1); //$NON-NLS-1$
		GridData gd = new GridData();
		fStopInMainCheckButton.setLayoutData(gd);
		fStopInMainCheckButton.addSelectionListener(fListener);		
		
	}
		
	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent the parent composite
	 */
	private void createMainTypeEditor(Composite parent) {
		Font font= parent.getFont();
		Group mainGroup= new Group(parent, SWT.NONE);
		mainGroup.setText(LauncherMessages.JavaMainTab_Main_cla_ss__4); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainGroup.setLayout(layout);
		mainGroup.setFont(font);

		fMainText = new Text(mainGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fMainText.setLayoutData(gd);
		fMainText.setFont(font);
		fMainText.addModifyListener(fListener);
		
		fSearchButton = createPushButton(mainGroup,LauncherMessages.JavaMainTab_Searc_h_5, null); //$NON-NLS-1$
		fSearchButton.addSelectionListener(fListener);
		
		fSearchExternalJarsCheckButton = createCheckButton(mainGroup, LauncherMessages.JavaMainTab_E_xt__jars_6); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fSearchExternalJarsCheckButton.setLayoutData(gd);
		fSearchExternalJarsCheckButton.addSelectionListener(fListener);

		fConsiderInheritedMainButton = createCheckButton(mainGroup, LauncherMessages.JavaMainTab_22); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fConsiderInheritedMainButton.setLayoutData(gd);
		fConsiderInheritedMainButton.addSelectionListener(fListener);
	}
	
	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent the parent composite
	 */
	private void createProjectEditor(Composite parent) {
		Font font= parent.getFont();
		Group group= new Group(parent, SWT.NONE);
		group.setText(LauncherMessages.JavaMainTab__Project__2); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);

		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.setFont(font);
		fProjText.addModifyListener(fListener);
		
		fProjButton = createPushButton(group, LauncherMessages.JavaMainTab__Browse_3, null); //$NON-NLS-1$
		fProjButton.addSelectionListener(fListener);
	}	

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateMainTypeFromConfig(config);
		updateStopInMainFromConfig(config);
		updateInheritedMainsFromConfig(config);
		updateExternalJars(config);
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
	
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
		String mainTypeName = ""; //$NON-NLS-1$
		try {
			mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);	
		}	
		fMainText.setText(mainTypeName);	
	}
	
	protected void updateStopInMainFromConfig(ILaunchConfiguration configuration) {
		boolean stop = false;
		try {
			stop = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		fStopInMainCheckButton.setSelection(stop);
	}
	
	protected void updateInheritedMainsFromConfig(ILaunchConfiguration configuration) {
		boolean inherit = false;
		try {
			inherit = configuration.getAttribute(ATTR_CONSIDER_INHERITED_MAIN, false);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		fConsiderInheritedMainButton.setSelection(inherit);
	}	
	
	protected void updateExternalJars(ILaunchConfiguration configuration) {
		boolean search = false;
		try {
			search = configuration.getAttribute(ATTR_INCLUDE_EXTERNAL_JARS, false);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		fSearchExternalJarsCheckButton.setSelection(search);
	}	
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText().trim());
		
		// attribute added in 2.1, so null must be used instead of false for backwards compatibility
		if (fStopInMainCheckButton.getSelection()) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, (String)null);
		}
		
		// attribute added in 2.1, so null must be used instead of false for backwards compatibility
		if (fSearchExternalJarsCheckButton.getSelection()) {
			config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, true);
		} else {
			config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, (String)null);
		}
		
		// attribute added in 3.0, so null must be used instead of false for backwards compatibility
		if (fConsiderInheritedMainButton.getSelection()) {
			config.setAttribute(ATTR_CONSIDER_INHERITED_MAIN, true);
		} else {
			config.setAttribute(ATTR_CONSIDER_INHERITED_MAIN, (String)null);
		}		
	}
			
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
			
	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {
		
		IJavaProject javaProject = getJavaProject();
		IJavaElement[] elements = null;
		if ((javaProject == null) || !javaProject.exists()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel model = JavaCore.create(root);
			if (model != null) {
				try {
					elements = model.getJavaProjects();
				} catch (JavaModelException e) {
				}
			}
		} else {
			elements = new IJavaElement[]{javaProject};
		}		
		if (elements == null) {
			elements = new IJavaElement[]{};
		}
		int constraints = IJavaSearchScope.SOURCES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
			constraints |= IJavaSearchScope.SYSTEM_LIBRARIES;
		}		
		IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, constraints);
		
		MainMethodSearchEngine engine = new MainMethodSearchEngine();
		IType[] types = null;
		try {
			types = engine.searchMainMethods(getLaunchConfigurationDialog(), searchScope, fConsiderInheritedMainButton.getSelection());
		} catch (InvocationTargetException e) {
			setErrorMessage(e.getMessage());
			return;
		} catch (InterruptedException e) {
			setErrorMessage(e.getMessage());
			return;
		}
		
		Shell shell = getShell();
		SelectionDialog dialog = new MainTypeSelectionDialog(shell, types); 
		dialog.setTitle(LauncherMessages.JavaMainTab_Choose_Main_Type_11); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.JavaMainTab_Choose_a_main__type_to_launch__12); //$NON-NLS-1$
		if (dialog.open() == Window.CANCEL) {
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
		dialog.setTitle(LauncherMessages.JavaMainTab_Project_Selection_13); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.JavaMainTab_Choose_a__project_to_constrain_the_search_for_main_types__14); //$NON-NLS-1$
		dialog.setElements(projects);
		
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {			
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		
		setErrorMessage(null);
		setMessage(null);
		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (!project.exists()) {
					setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_20, new String[] {name})); //$NON-NLS-1$
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_21, new String[] {name})); //$NON-NLS-1$
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_19, new String[]{status.getMessage()})); //$NON-NLS-1$
				return false;
			}
		}

		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.JavaMainTab_Main_type_not_specified_16); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeJavaProject(javaElement, config);
		} else {
			// We set empty attributes for project & main type so that when one config is
			// compared to another, the existence of empty attributes doesn't cause an
			// incorrect result (the performApply() method can result in empty values
			// for these attributes being set on a config if there is nothing in the
			// corresponding text boxes)
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		}
		initializeMainTypeAndName(javaElement, config);
	}

	/**
	 * Set the main type & name attributes on the working copy based on the IJavaElement
	 */
	protected void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name= null;
		if (javaElement instanceof IMember) {
			IMember member = (IMember)javaElement;
			if (member.isBinary()) {
				javaElement = member.getClassFile();
			} else {
				javaElement = member.getCompilationUnit();
			}
		}
		if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
			try {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaElement}, false);
				MainMethodSearchEngine engine = new MainMethodSearchEngine();
				IType[] types = engine.searchMainMethods(getLaunchConfigurationDialog(), scope, false);				
				if (types != null && (types.length > 0)) {
					// Simply grab the first main type found in the searched element
					name = types[0].getFullyQualifiedName();
				}
			} catch (InterruptedException ie) {
			} catch (InvocationTargetException ite) {
			}
		}
		if (name == null) {
			name= ""; //$NON-NLS-1$
		}
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

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.JavaMainTab__Main_19; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when activated
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}
}

