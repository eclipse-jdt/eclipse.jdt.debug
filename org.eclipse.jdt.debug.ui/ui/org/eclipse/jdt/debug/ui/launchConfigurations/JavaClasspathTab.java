/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     BEA - Daniel R Somerfield - Bug 88939
 *     Axel Richard (Obeo) - Bug 41353 - Launch configurations prototypes
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.actions.AddAdvancedAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddLibraryAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddProjectAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddVariableAction;
import org.eclipse.jdt.internal.debug.ui.actions.AttachSourceAction;
import org.eclipse.jdt.internal.debug.ui.actions.CopyAction;
import org.eclipse.jdt.internal.debug.ui.actions.EditClasspathEntryAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
import org.eclipse.jdt.internal.debug.ui.actions.RestoreDefaultEntriesAction;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.debug.ui.classpath.BootpathFilter;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathContentProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathLabelProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits the user and
 * bootstrap classes comprising the classpath launch configuration
 * attribute.
 * <p>
 * Clients may call {@link #setHelpContextId(String)} on this tab prior to control
 * creation to alter the default context help associated with this tab.
 * </p>
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.0
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class JavaClasspathTab extends AbstractJavaClasspathTab {

	protected RuntimeClasspathViewer fClasspathViewer;
	private ClasspathModel fModel;

	protected static final String DIALOG_SETTINGS_PREFIX = "JavaClasspathTab"; //$NON-NLS-1$

	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;

	private Button fExcludeTestCodeButton;
	private Button fUseClasspathOnlyJarButton;

	/**
	 * Constructor
	 */
	public JavaClasspathTab() {
		super();
		setHelpContextId(IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		GridData gd;

		Label label = new Label(comp, SWT.NONE);
		label.setText(LauncherMessages.JavaClasspathTab_0);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fClasspathViewer = new RuntimeClasspathViewer(comp);
		fClasspathViewer.addEntriesChangedListener(this);
		fClasspathViewer.getTreeViewer().getControl().setFont(font);
		fClasspathViewer.getTreeViewer().setLabelProvider(new ClasspathLabelProvider());
		fClasspathViewer.getTreeViewer().setContentProvider(new ClasspathContentProvider(this));
		if (!isShowBootpath()) {
			fClasspathViewer.getTreeViewer().addFilter(new BootpathFilter());
		}

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);

		createPathButtons(pathButtonComp);
		SWTFactory.createVerticalSpacer(comp, 2);

		fExcludeTestCodeButton = SWTFactory.createCheckButton(comp, LauncherMessages.JavaClasspathTab_Exclude_Test_Code, null, false, 2);
		fExcludeTestCodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		fUseClasspathOnlyJarButton = SWTFactory.createCheckButton(comp, LauncherMessages.VMArgumentsBlock_1, null, false, 1);
		fUseClasspathOnlyJarButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Creates the buttons to manipulate the classpath.
	 *
	 * @param pathButtonComp composite buttons are contained in
	 * @since 3.0
	 */
	protected void createPathButtons(Composite pathButtonComp) {
		List<RuntimeClasspathAction> advancedActions = new ArrayList<>(5);

		createButton(pathButtonComp, new MoveUpAction(fClasspathViewer));
		createButton(pathButtonComp, new MoveDownAction(fClasspathViewer));
		createButton(pathButtonComp, new RemoveAction(fClasspathViewer));
		createButton(pathButtonComp, new CopyAction(fClasspathViewer));
		createButton(pathButtonComp, new AddProjectAction(fClasspathViewer));
		createButton(pathButtonComp, new AddJarAction(fClasspathViewer));
		createButton(pathButtonComp, new AddExternalJarAction(fClasspathViewer, DIALOG_SETTINGS_PREFIX));

		RuntimeClasspathAction action = new AddFolderAction(null);
		advancedActions.add(action);

		action = new AddExternalFolderAction(null, DIALOG_SETTINGS_PREFIX);
		advancedActions.add(action);

		action = new AddVariableAction(null);
		advancedActions.add(action);

		action = new AddLibraryAction(null);
		advancedActions.add(action);

		action = new AttachSourceAction(null, SWT.RADIO);
		advancedActions.add(action);

		IAction[] adv = advancedActions.toArray(new IAction[advancedActions.size()]);
		createButton(pathButtonComp, new AddAdvancedAction(fClasspathViewer, adv));

		action = new EditClasspathEntryAction(fClasspathViewer, getLaunchConfiguration());
		createButton(pathButtonComp, action);

		action= new RestoreDefaultEntriesAction(fClasspathViewer, this);
		createButton(pathButtonComp, action);
		action.setEnabled(true);
	}

	/**
	 * Creates a button for the given action.
	 *
	 * @param pathButtonComp parent composite for the button
	 * @param action the action triggered by the button
	 * @return the button that was created
	 */
	protected Button createButton(Composite pathButtonComp, RuntimeClasspathAction action) {
		Button button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		refresh(configuration);
		fClasspathViewer.getTreeViewer().expandToLevel(2);
		try {
			fExcludeTestCodeButton.setSelection(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, false));
			fUseClasspathOnlyJarButton.setSelection(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, false));
		} catch (CoreException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		try {
			boolean useDefault= workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			if (useDefault) {
				if (!isDefaultClasspath(getCurrentClasspath(), workingCopy)) {
					initializeFrom(workingCopy);
					return;
				}
			}
			fClasspathViewer.getTreeViewer().refresh();
		} catch (CoreException e) {
		}
	}

	/**
	 * Refreshes the classpath entries based on the current state of the given
	 * launch configuration.
	 * @param configuration the configuration
	 */
	private void refresh(ILaunchConfiguration configuration) {
		setErrorMessage(null);

		setLaunchConfiguration(configuration);
		try {
			createClasspathModel(configuration);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}

		fClasspathViewer.setLaunchConfiguration(configuration);
		fClasspathViewer.getTreeViewer().setInput(fModel);
		setDirty(false);
	}

	private void createClasspathModel(ILaunchConfiguration configuration) throws CoreException {
		fModel= new ClasspathModel();
		IRuntimeClasspathEntry[] entries= JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < entries.length; i++) {
			entry= entries[i];
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					fModel.addEntry(ClasspathModel.USER, entry);
					break;
				default:
					fModel.addEntry(ClasspathModel.BOOTSTRAP, entry);
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isDirty()) {
			IRuntimeClasspathEntry[] classpath = getCurrentClasspath();
			boolean def = isDefaultClasspath(classpath, configuration);
			if (def) {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, (String)null);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
			} else {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				try {
					List<String> mementos = new ArrayList<>(classpath.length);
					for (int i = 0; i < classpath.length; i++) {
						IRuntimeClasspathEntry entry = classpath[i];
						mementos.add(entry.getMemento());
					}
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, mementos);
				} catch (CoreException e) {
					JDIDebugUIPlugin.statusDialog(LauncherMessages.JavaClasspathTab_Unable_to_save_classpath_1, e.getStatus());
				}
			}
			try {
				boolean previousExcludeTestCode = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, false);
				if (previousExcludeTestCode != fExcludeTestCodeButton.getSelection()) {
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, fExcludeTestCodeButton.getSelection());
					fClasspathViewer.setEntries(JavaRuntime.computeUnresolvedRuntimeClasspath(configuration));
				}
			}
			catch (CoreException e) {
				JDIDebugUIPlugin.statusDialog(LauncherMessages.JavaClasspathTab_Unable_to_save_classpath_1, e.getStatus());
			}
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, fUseClasspathOnlyJarButton.getSelection());
		}
	}

	/**
	 * Returns the classpath entries currently specified by this tab.
	 *
	 * @return the classpath entries currently specified by this tab
	 */
	private IRuntimeClasspathEntry[] getCurrentClasspath() {
		IClasspathEntry[] boot = fModel.getEntries(ClasspathModel.BOOTSTRAP);
		IClasspathEntry[] user = fModel.getEntries(ClasspathModel.USER);
		List<IRuntimeClasspathEntry> entries = new ArrayList<>(boot.length + user.length);
		IClasspathEntry bootEntry;
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < boot.length; i++) {
			bootEntry= boot[i];
			entry = null;
			if (bootEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry)bootEntry).getDelegate();
			} else if (bootEntry instanceof IRuntimeClasspathEntry) {
				entry= (IRuntimeClasspathEntry) boot[i];
			}
			if (entry != null) {
				if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
					entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
				}
				entries.add(entry);
			}
		}
		IClasspathEntry userEntry;
		for (int i = 0; i < user.length; i++) {
			userEntry= user[i];
			entry = null;
			if (userEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry)userEntry).getDelegate();
			} else if (userEntry instanceof IRuntimeClasspathEntry) {
				entry= (IRuntimeClasspathEntry) user[i];
			}
			if (entry != null) {
				entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
				entries.add(entry);
			}
		}
		return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}

	/**
	 * Returns whether the specified classpath is equivalent to the
	 * default classpath for this configuration.
	 *
	 * @param classpath classpath to compare to default
	 * @param configuration original configuration
	 * @return whether the specified classpath is equivalent to the
	 * default classpath for this configuration
	 */
	private boolean isDefaultClasspath(IRuntimeClasspathEntry[] classpath, ILaunchConfiguration configuration) {
		try {
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			IRuntimeClasspathEntry[] entries= JavaRuntime.computeUnresolvedRuntimeClasspath(wc);
			if (classpath.length == entries.length) {
				for (int i = 0; i < entries.length; i++) {
					IRuntimeClasspathEntry entry = entries[i];
					if (!entry.equals(classpath[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		} catch (CoreException e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return LauncherMessages.JavaClasspathTab_Cla_ss_path_3;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 *
	 * @since 3.3
	 */
	@Override
	public String getId() {
		return "org.eclipse.jdt.debug.ui.javaClasspathTab"; //$NON-NLS-1$
	}

	/**
	 * Returns the image for this tab, or <code>null</code> if none
	 *
	 * @return the image for this tab, or <code>null</code> if none
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public static Image getClasspathImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_CLASSPATH);
	}

	/**
	 * Sets the launch configuration for this classpath tab
	 * @param config the backing {@link ILaunchConfiguration}
	 */
	private void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}

	/**
	 * Returns the current launch configuration
	 * @return the backing {@link ILaunchConfiguration}
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		if (fClasspathViewer != null) {
			fClasspathViewer.removeEntriesChangedListener(this);
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return getClasspathImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		String projectName= null;
		try {
			projectName= launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			return false;
		}
		if (projectName.length() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(projectName, IResource.PROJECT);
			if (status.isOK()) {
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (!project.exists()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_20, projectName));
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_21, projectName));
					return false;
				}
			} else {
				setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_19, status.getMessage()));
				return false;
			}
		}

		IRuntimeClasspathEntry [] entries = fModel.getAllEntries();
		int type = -1;
		for (int i=0; i<entries.length; i++) {
			type = entries[i].getType();
			if (type == IRuntimeClasspathEntry.ARCHIVE) {
				if(!entries[i].getPath().isAbsolute())	{
					setErrorMessage(NLS.bind(LauncherMessages.JavaClasspathTab_Invalid_runtime_classpath_1, entries[i].getPath().toString()));
					return false;
				}
			}
			if(type == IRuntimeClasspathEntry.PROJECT) {
				IResource res = entries[i].getResource();
				if(res != null && !res.isAccessible()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaClasspathTab_1, res.getName()));
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns whether the bootpath should be displayed.
	 *
	 * @return whether the bootpath should be displayed
	 * @since 3.0
	 */
	public boolean isShowBootpath() {
		return true;
	}

	/**
	 * @return Returns the classpath model.
	 */
	protected ClasspathModel getModel() {
		return fModel;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab#initializeAttributes()
	 * @since 3.9
	 */
	@Override
	protected void initializeAttributes() {
		super.initializeAttributes();
		getAttributesLabelsForPrototype().put(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, LauncherMessages.JavaClasspathTab_AttributeLabel_DefaultClasspath);
		getAttributesLabelsForPrototype().put(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, LauncherMessages.JavaClasspathTab_AttributeLabel_Classpath);
	}
}
