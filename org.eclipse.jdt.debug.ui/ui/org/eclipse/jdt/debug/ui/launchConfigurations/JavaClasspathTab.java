package org.eclipse.jdt.debug.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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

	protected TabFolder fPathTabFolder;
	protected TabItem fBootPathTabItem;
	protected TabItem fClassPathTabItem;
	protected RuntimeClasspathViewer fClasspathViewer;
	protected RuntimeClasspathViewer fBootpathViewer;
	protected Button fClassPathDefaultButton;
	protected List fActions = new ArrayList(10);
	protected Image fImage = null;

	protected static final String DIALOG_SETTINGS_PREFIX = "JavaClasspathTab"; //$NON-NLS-1$
	
	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
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
		
		createVerticalSpacer(comp, 2);
		
		fPathTabFolder = new TabFolder(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		fPathTabFolder.setLayoutData(gd);
		fPathTabFolder.setFont(font);
		fPathTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TabItem[] tabs = fPathTabFolder.getSelection();
				if (tabs.length == 1) {
					RuntimeClasspathViewer data = (RuntimeClasspathViewer)tabs[0].getData();
					retargetActions(data);
				}
			}
		});
		
		fClasspathViewer = new RuntimeClasspathViewer(fPathTabFolder);
		fClasspathViewer.addEntriesChangedListener(this);
		fClasspathViewer.getControl().setFont(font);
		fClassPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 0);
		fClassPathTabItem.setText(LauncherMessages.getString("JavaClasspathTab.Us&er_classes_1")); //$NON-NLS-1$
		fClassPathTabItem.setControl(fClasspathViewer.getControl());
		fClassPathTabItem.setData(fClasspathViewer);

		fBootpathViewer = new RuntimeClasspathViewer(fPathTabFolder);
		fBootpathViewer.addEntriesChangedListener(this);
		fBootpathViewer.getControl().setFont(font);
		fBootPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 1);
		fBootPathTabItem.setText(LauncherMessages.getString("JavaClasspathTab.&Bootstrap_classes_2")); //$NON-NLS-1$
		fBootPathTabItem.setControl(fBootpathViewer.getControl());
		fBootPathTabItem.setData(fBootpathViewer);

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);

		createVerticalSpacer(comp, 2);
						
		fClassPathDefaultButton = new Button(comp, SWT.CHECK);
		fClassPathDefaultButton.setText(LauncherMessages.getString("JavaEnvironmentTab.Use_defau&lt_classpath_10")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fClassPathDefaultButton.setLayoutData(gd);
		fClassPathDefaultButton.setFont(font);
		fClassPathDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleClasspathDefaultButtonSelected();
			}
		});
		
		createVerticalSpacer(pathButtonComp, 1);
		
		List advancedActions = new ArrayList(5);
		
		RuntimeClasspathAction action = new MoveUpAction(null);								
		Button button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);
		
		action = new MoveDownAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new RemoveAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		
		
		action = new AddProjectAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddJarAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddExternalJarAction(null, DIALOG_SETTINGS_PREFIX);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddFolderAction(null);								
		advancedActions.add(action);

		action = new AddExternalFolderAction(null, DIALOG_SETTINGS_PREFIX);								
		advancedActions.add(action);		

		action = new AddVariableAction(null);								
		advancedActions.add(action);		
		
		action = new AttachSourceAction(null);								
		advancedActions.add(action);
		
		IAction[] adv = (IAction[])advancedActions.toArray(new IAction[advancedActions.size()]);
		action = new AddAdvancedAction(null, adv);
		button = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);
														
		retargetActions(fClasspathViewer);		
	}

	/**
	 * The default classpath button has been toggled
	 */
	protected void handleClasspathDefaultButtonSelected() {
		boolean useDefault = fClassPathDefaultButton.getSelection();
		fClassPathDefaultButton.setSelection(useDefault);
		if (useDefault) {
			displayDefaultClasspath();
		}
		fClasspathViewer.setEnabled(!useDefault);
		fBootpathViewer.setEnabled(!useDefault);
		updateLaunchConfigurationDialog();
		setDirty(true);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean useDefault = true;
		try {
			useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		
		if (configuration == getLaunchConfiguration()) {
			// no need to update if an explicit path is being used and this setting
			// has not changed (and viewing the same config as last time)
			if (!useDefault && !fClassPathDefaultButton.getSelection()) {
				setDirty(false);
				return;			
			}
		}
		
		setLaunchConfiguration(configuration);
		fClassPathDefaultButton.setSelection(useDefault);
		try {
			setClasspathEntries(JavaRuntime.computeUnresolvedRuntimeClasspath(configuration));
		} catch (CoreException e) {
			if (e.getStatus().getCode() != IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT) {
				// do not log error if this is a reference to a non-existent project
				JDIDebugUIPlugin.log(e);
			}
		}
		fClasspathViewer.setEnabled(!useDefault);
		fBootpathViewer.setEnabled(!useDefault);
		fClasspathViewer.setLaunchConfiguration(configuration);
		fBootpathViewer.setLaunchConfiguration(configuration);
		setDirty(false);
	}

	/**
	 * Displays the default classpath in the UI
	 */
	protected void displayDefaultClasspath() {
		ILaunchConfiguration config = getLaunchConfiguration();
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			if (config.isWorkingCopy()) {
				wc= (ILaunchConfigurationWorkingCopy)config;
			} else {
				wc = config.getWorkingCopy();
			}
			performApply(wc);
			setClasspathEntries(JavaRuntime.computeUnresolvedRuntimeClasspath(wc));
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}

	}
	
	/**
	 * Displays the given classpath entries, grouping into user and bootstrap entries
	 */
	protected void setClasspathEntries(IRuntimeClasspathEntry[] entries) {
		List cp = new ArrayList(entries.length);
		List bp = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			switch (entries[i].getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					cp.add(entries[i]);
					break;
				default:
					bp.add(entries[i]);
					break;
			}
		}
		fClasspathViewer.setEntries((IRuntimeClasspathEntry[])cp.toArray(new IRuntimeClasspathEntry[cp.size()]));
		fBootpathViewer.setEntries((IRuntimeClasspathEntry[])bp.toArray(new IRuntimeClasspathEntry[bp.size()]));		
	}
	
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isDirty()) {
			boolean def = fClassPathDefaultButton.getSelection();		
			if (def) {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, (String)null);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
			} else {
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				try {
					IRuntimeClasspathEntry[] boot = fBootpathViewer.getEntries();
					IRuntimeClasspathEntry[] user = fClasspathViewer.getEntries();
					List mementos = new ArrayList(boot.length + user.length);
					for (int i = 0; i < boot.length; i++) {
						boot[i].setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
						mementos.add(boot[i].getMemento());
					}
					for (int i = 0; i < user.length; i++) {
						user[i].setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
						mementos.add(user[i].getMemento());
					}
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, mementos);
				} catch (CoreException e) {
					JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaClasspathTab.Unable_to_save_classpath_1"), e); //$NON-NLS-1$
				}	
			}
		}
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaClasspathTab.Cla&ss_path_3"); //$NON-NLS-1$
	}
	
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		if (fImage == null) {
			fImage = JavaPluginImages.DESC_TOOL_CLASSPATH_ORDER.createImage();
		}
		return fImage;
	}		
	
	/**
	 * Sets the java project currently specified by the
	 * given launch config, if any.
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}	
	
	/**
	 * Returns the current java project context
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}
	
	/**
	 * Adds the given action to the action collection in this tab
	 */
	protected void addAction(RuntimeClasspathAction action) {
		fActions.add(action);
	}
	
	/**
	 * Re-targets actions to the given viewer
	 */
	protected void retargetActions(RuntimeClasspathViewer viewer) {
		Iterator actions = fActions.iterator();
		while (actions.hasNext()) {
			RuntimeClasspathAction action = (RuntimeClasspathAction)actions.next();
			action.setViewer(viewer);
		}
	}
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if (fClasspathViewer != null) {
			fClasspathViewer.removeEntriesChangedListener(this);
			fBootpathViewer.removeEntriesChangedListener(this);
		}
		if (fImage != null) {
			fImage.dispose();
		}
		super.dispose();
	}

}
