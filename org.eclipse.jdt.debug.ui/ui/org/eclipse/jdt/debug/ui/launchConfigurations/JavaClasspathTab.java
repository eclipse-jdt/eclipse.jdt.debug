/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.AddAdvancedAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddProjectAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddVariableAction;
import org.eclipse.jdt.internal.debug.ui.actions.AttachSourceAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathContentProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathLabelProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A launch configuration tab that displays and edits the user and
 * bootstrap classes comprising the classpath launch configuration
 * attribute.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class JavaClasspathTab extends JavaLaunchConfigurationTab {

	protected RuntimeClasspathViewer fClasspathViewer;
	protected static Image fgClasspathImage = null;
	private ClasspathModel model;

	protected static final String DIALOG_SETTINGS_PREFIX = "JavaClasspathTab"; //$NON-NLS-1$
	
	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		fClasspathViewer = new RuntimeClasspathViewer(comp);
		fClasspathViewer.addEntriesChangedListener(this);
		fClasspathViewer.getControl().setFont(font);
		fClasspathViewer.setLabelProvider(new ClasspathLabelProvider());
		fClasspathViewer.setContentProvider(new ClasspathContentProvider());
	
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);
		
		createVerticalSpacer(pathButtonComp, 1);
		
		List advancedActions = new ArrayList(5);
		
		RuntimeClasspathAction action = new MoveUpAction(fClasspathViewer);								
		Button button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		
		action = new MoveDownAction(fClasspathViewer);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);

		action = new RemoveAction(fClasspathViewer);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		
		action = new AddProjectAction(fClasspathViewer);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);

		action = new AddJarAction(fClasspathViewer);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);	

		action = new AddExternalJarAction(fClasspathViewer, DIALOG_SETTINGS_PREFIX);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);

		action = new AddFolderAction(null);								
		advancedActions.add(action);

		action = new AddExternalFolderAction(null, DIALOG_SETTINGS_PREFIX);								
		advancedActions.add(action);		

		action = new AddVariableAction(null);								
		advancedActions.add(action);		
		
		action = new AttachSourceAction(null, SWT.RADIO);								
		advancedActions.add(action);
		
		IAction[] adv = (IAction[])advancedActions.toArray(new IAction[advancedActions.size()]);
		action = new AddAdvancedAction(fClasspathViewer, adv);
		button = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		
		button= createPushButton(pathButtonComp, LauncherMessages.getString("JavaClasspathTab.3"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				restoreDefaultEntries();
			}
		});
	}
	
	private void restoreDefaultEntries() {
		IRuntimeClasspathEntry[] entries= null;
		try {
			ILaunchConfigurationWorkingCopy copy= getLaunchConfiguration().getWorkingCopy();
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			entries= JavaRuntime.computeUnresolvedRuntimeClasspath(copy);
		} catch (CoreException e) {
			//TODO set error message
			return;
		}
					
		List bootEntries= new ArrayList(entries.length);
		for (int j = 0; j < entries.length; j++) {
			if (entries[j].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				bootEntries.add(entries[j]);
			}
		}
		model.setBootstrapEntries((IRuntimeClasspathEntry[])bootEntries.toArray(new IRuntimeClasspathEntry[bootEntries.size()]));
		
		List userEntries= new ArrayList(entries.length);
		for (int j = 0; j < entries.length; j++) {
			if (entries[j].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				userEntries.add(entries[j]);
			}
		}
		model.setUserEntries((IRuntimeClasspathEntry[])userEntries.toArray(new IRuntimeClasspathEntry[userEntries.size()]));
		
		fClasspathViewer.refresh();	
		entriesChanged(fClasspathViewer);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean useDefault = true;
		setErrorMessage(null);
		try {
			useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		
		if (configuration == getLaunchConfiguration()) {
			// no need to update if an explicit path is being used and this setting
			// has not changed (and viewing the same config as last time)
			if (!useDefault) {
				setDirty(false);
				return;			
			}
		}
		
		setLaunchConfiguration(configuration);
		try {
			createClasspathModel(configuration);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
		
		fClasspathViewer.setLaunchConfiguration(configuration);
		fClasspathViewer.setInput(model);
		setDirty(false);
	}
	
	private void createClasspathModel(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries= JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		model= new ClasspathModel();
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < entries.length; i++) {
			entry= entries[i];
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:				
					model.addEntry(ClasspathModel.USER, entry);
					break;
				default:
					model.addEntry(ClasspathModel.BOOTSTRAP, entry);
					break;
			}
		}	
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isDirty()) {
			boolean def = !hasClasspathChanged(configuration.getOriginal());
			if (def) {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, (String)null);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
			} else {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				try {
					IClasspathEntry[] boot = model.getEntries(ClasspathModel.BOOTSTRAP);
					IClasspathEntry[] user = model.getEntries(ClasspathModel.USER);
					List mementos = new ArrayList(boot.length + user.length);
					IClasspathEntry bootEntry;
					IRuntimeClasspathEntry entry;
					for (int i = 0; i < boot.length; i++) {
						bootEntry= boot[i];
						if (bootEntry instanceof IRuntimeClasspathEntry) {
							entry= (IRuntimeClasspathEntry) boot[i];
							entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
							mementos.add(entry.getMemento());
						}
					}
					IClasspathEntry userEntry;
					for (int i = 0; i < user.length; i++) {
						userEntry= user[i];
						if (userEntry instanceof IRuntimeClasspathEntry) {
							entry= (IRuntimeClasspathEntry) user[i];
							entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
							mementos.add(entry.getMemento());
						}
					}
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, mementos);
				} catch (CoreException e) {
					JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaClasspathTab.Unable_to_save_classpath_1"), e); //$NON-NLS-1$
				}	
			}
		}
	}

	/**
	 * @param configuration
	 * @return
	 */
	private boolean hasClasspathChanged(ILaunchConfiguration configuration) {
		try {
			IRuntimeClasspathEntry[] entries= JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
			IClasspathEntry[] bootEntries= model.getEntries(ClasspathModel.BOOTSTRAP);
			IClasspathEntry entry;
			int i;
			for (i = 0; i < bootEntries.length; i++) {
				if (i == entries.length) {
					return true;
				}
				entry = bootEntries[i];
				if (i > entries.length || !entry.equals(entries[i])) {
					return true;
				}
				
			}
			
			IClasspathEntry[] userEntries= model.getEntries(ClasspathModel.USER);
			for (;i < entries.length; i++) {
				if (i >= userEntries.length) {
					return true;
				}
				entry = userEntries[i];
				if (i > entries.length || !entry.equals(entries[i])) {
					return true;
				}
				
			}
		} catch (CoreException e) {
			return true;
		}
		
		return false;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaClasspathTab.Cla&ss_path_3"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public static Image getClasspathImage() {
		if (fgClasspathImage == null) {
			fgClasspathImage = JavaPluginImages.DESC_TOOL_CLASSPATH_ORDER.createImage();
		}
		return fgClasspathImage;
	}		
	
	/**
	 * Sets the launch configuration for this classpath tab
	 */
	private void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}	
	
	/**
	 * Returns the current launch configuration
	 */
	private ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if (fClasspathViewer != null) {
			fClasspathViewer.removeEntriesChangedListener(this);
		}
		if (fgClasspathImage != null) {
			fgClasspathImage.dispose();
			fgClasspathImage= null;
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return getClasspathImage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		String projectName= null;
		try {
			projectName= launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			return false;
		}
		if (projectName.length() == 0) {
			return false;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateName(projectName, IResource.PROJECT);
		if (status.isOK()) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).exists()) {
				setErrorMessage(LauncherMessages.getString("JavaMainTab.Project_does_not_exist_15")); //$NON-NLS-1$
				return false;
			}
		} else {
			setErrorMessage(MessageFormat.format(LauncherMessages.getString("JavaMainTab.19"), new String[]{status.getMessage()})); //$NON-NLS-1$
			return false;
		}
		return true;
	}
}