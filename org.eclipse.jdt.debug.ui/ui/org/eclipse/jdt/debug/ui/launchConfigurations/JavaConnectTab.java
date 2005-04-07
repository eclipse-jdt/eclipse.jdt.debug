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


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.ComboFieldEditor;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import com.sun.jdi.connect.Connector;

/**
 * A launch configuration tab that displays and edits the project associated
 * with a remote connection and the connector used to connect to a remote
 * VM.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class JavaConnectTab extends JavaLaunchConfigurationTab implements IPropertyChangeListener {

	// Project UI widgets
	protected Label fProjLabel;
	protected Text fProjText;
	protected Button fProjButton;
	
	// Allow terminate UI widgets
	protected Button fAllowTerminateButton;
	
	// Connector attributes for selected connector
	protected Map fArgumentMap;
	protected Map fFieldEditorMap = new HashMap();
	protected Composite fArgumentComposite;
	// the selected connector
	protected IVMConnector fConnector;
	
	// Connector combo
	protected Combo fConnectorCombo;
	protected IVMConnector[] fConnectors = JavaRuntime.getVMConnectors();
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CONNECT_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		GridData gd;
		
		createVerticalSpacer(comp, 1);

		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);
		projComp.setFont(font);
		
		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText(LauncherMessages.JavaConnectTab__Project__2); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);
		fProjLabel.setFont(font);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.setFont(font);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fProjButton = createPushButton(projComp, LauncherMessages.JavaConnectTab__Browse_3, null); //$NON-NLS-1$
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		Composite connectorComp = new Composite(comp,SWT.NONE);
		GridLayout y = new GridLayout();
		y.numColumns = 2;
		y.marginHeight = 0;
		y.marginWidth = 0;
		connectorComp.setLayout(y);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		connectorComp.setLayoutData(gd);
		
		Label l = new Label(connectorComp, SWT.NONE);
		l.setText(LauncherMessages.JavaConnectTab_Connect_ion_Type__7); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);
		l.setFont(font);
		
		fConnectorCombo = new Combo(connectorComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fConnectorCombo.setLayoutData(gd);
		fConnectorCombo.setFont(font);
		String[] names = new String[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			names[i] = fConnectors[i].getName();
		}
		fConnectorCombo.setItems(names);
		fConnectorCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleConnectorComboModified();
			}
		});
		
		createVerticalSpacer(comp, 2);
		
		Group group = new Group(comp, SWT.NONE);
		group.setText(LauncherMessages.JavaConnectTab_Connection_Properties_1); //$NON-NLS-1$
		group.setLayout(new GridLayout());
		group.setFont(font);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);		
	
		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite(group, SWT.NONE);
		y = new GridLayout();
		spacingComposite.setLayout(y);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		spacingComposite.setLayoutData(gd);	
		fArgumentComposite= spacingComposite;
		fArgumentComposite.setFont(font);
		createVerticalSpacer(comp, 2);		
		
		fAllowTerminateButton = createCheckButton(comp, LauncherMessages.JavaConnectTab__Allow_termination_of_remote_VM_6); //$NON-NLS-1$
		fAllowTerminateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Update the argument area to show the selected connector's arguments
	 */
	protected void handleConnectorComboModified() {
		int index = fConnectorCombo.getSelectionIndex();
		if ( (index < 0) || (index >= fConnectors.length) ) {
			return;
		}
		IVMConnector vm = fConnectors[index];
		if (vm.equals(fConnector)) {
			return; // selection did not change
		}
		fConnector = vm;
		try {
			fArgumentMap = vm.getDefaultArguments();
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.JavaConnectTab_Unable_to_display_connection_arguments__2, e.getStatus()); //$NON-NLS-1$
			return;
		}
		
		// Dispose of any current child widgets in the tab holder area
		Control[] children = fArgumentComposite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		fFieldEditorMap.clear();
		PreferenceStore store = new PreferenceStore();
		// create editors
		Iterator keys = vm.getArgumentOrder().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			Connector.Argument arg = (Connector.Argument)fArgumentMap.get(key);
			FieldEditor field = null;
			if (arg instanceof Connector.IntegerArgument) {
				store.setDefault(arg.name(), ((Connector.IntegerArgument)arg).intValue());
				field = new IntegerFieldEditor(arg.name(), getLabel(arg.label()), fArgumentComposite);
			} else if (arg instanceof Connector.SelectedArgument) {
				List choices = ((Connector.SelectedArgument)arg).choices();
				String[][] namesAndValues = new String[choices.size()][2];
				Iterator iter = choices.iterator();
				int count = 0;
				while (iter.hasNext()) {
					String choice = (String)iter.next();
					namesAndValues[count][0] = choice;
					namesAndValues[count][1] = choice;
					count++;
				}
				store.setDefault(arg.name(), arg.value());
				field = new ComboFieldEditor(arg.name(), getLabel(arg.label()), namesAndValues, fArgumentComposite);
			} else if (arg instanceof Connector.StringArgument) {
				store.setDefault(arg.name(), arg.value());
				field = new StringFieldEditor(arg.name(), getLabel(arg.label()), fArgumentComposite);
			} else if (arg instanceof Connector.BooleanArgument) {
				store.setDefault(arg.name(), ((Connector.BooleanArgument)arg).booleanValue());
				field = new BooleanFieldEditor(arg.name(), getLabel(arg.label()), fArgumentComposite);					
			}
			field.setPreferenceStore(store);
			field.loadDefault();
			field.setPropertyChangeListener(this);
			fFieldEditorMap.put(key, field);
		}
		
		fArgumentComposite.getParent().getParent().layout();
		fArgumentComposite.layout();
	}
	
	/**
	 * Adds a colon to the label if required
	 */
	protected String getLabel(String label) {
		if (!label.endsWith(":")) { //$NON-NLS-1$
			label += ":"; //$NON-NLS-1$
		}
		return label;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateAllowTerminateFromConfig(config);
		updateConnectionFromConfig(config);
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
	
	protected void updateAllowTerminateFromConfig(ILaunchConfiguration config) {
		boolean allowTerminate = false;
		try {
			allowTerminate = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);	
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
		fAllowTerminateButton.setSelection(allowTerminate);	
	}

	protected void updateConnectionFromConfig(ILaunchConfiguration config) {
		String id = null;
		try {
			id = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, JavaRuntime.getDefaultVMConnector().getIdentifier());
			fConnectorCombo.setText(JavaRuntime.getVMConnector(id).getName());
			handleConnectorComboModified();
			
			Map attrMap = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map)null);
			if (attrMap == null) {
				return;
			}
			Iterator keys = attrMap.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				Connector.Argument arg = (Connector.Argument)fArgumentMap.get(key);
				FieldEditor editor = (FieldEditor)fFieldEditorMap.get(key);
				if (arg != null && editor != null) {
					String value = (String)attrMap.get(key);
					if (arg instanceof Connector.StringArgument || arg instanceof Connector.SelectedArgument) {
						editor.getPreferenceStore().setValue(key, value);
					} else if (arg instanceof Connector.BooleanArgument) {
						boolean b = new Boolean(value).booleanValue();
						editor.getPreferenceStore().setValue(key, b);
					} else if (arg instanceof Connector.IntegerArgument) {
						int i = new Integer(value).intValue();
						editor.getPreferenceStore().setValue(key, i);
					}
					editor.load();
				}
			}						
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}	
			
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, fAllowTerminateButton.getSelection());
		IVMConnector vmc = getSelectedConnector();
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, vmc.getIdentifier());
		
		Map attrMap = new HashMap(fFieldEditorMap.size());
		Iterator keys = fFieldEditorMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			FieldEditor editor = (FieldEditor)fFieldEditorMap.get(key);
			if (!editor.isValid()) {
				return;
			}
			Connector.Argument arg = (Connector.Argument)fArgumentMap.get(key);
			editor.store();
			if (arg instanceof Connector.StringArgument || arg instanceof Connector.SelectedArgument) {
				String value = editor.getPreferenceStore().getString(key);
				attrMap.put(key, value);
			} else if (arg instanceof Connector.BooleanArgument) {
				boolean value = editor.getPreferenceStore().getBoolean(key);
				attrMap.put(key, new Boolean(value).toString());
			} else if (arg instanceof Connector.IntegerArgument) {
				int value = editor.getPreferenceStore().getInt(key);
				attrMap.put(key, new Integer(value).toString());
			}
		}				
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, attrMap);
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
		dialog.setTitle(LauncherMessages.JavaConnectTab_Project_selection_10); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.JavaConnectTab_Choose_a_project_to_constrain_the_search_for_main_types_11); //$NON-NLS-1$
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
	 * Initialize default settings for the given Java element
	 */
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		initializeJavaProject(javaElement, config);
		initializeName(javaElement, config);
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
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
		String name = ""; //$NON-NLS-1$
		try {
			IResource resource = javaElement.getUnderlyingResource();
			if (resource != null) {
				name = resource.getName();
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(0, index);
				}
			} else {
				name= javaElement.getElementName();
			}
			name = getLaunchConfigurationDialog().generateName(name);				
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
		}
		config.rename(name);
	}

	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, JavaRuntime.getDefaultVMConnector().getIdentifier());
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		
		setErrorMessage(null);
		setMessage(null);

		// project		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LauncherMessages.JavaConnectTab_Project_does_not_exist_14); //$NON-NLS-1$
				return false;
			}
		}

		Iterator keys = fFieldEditorMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			Connector.Argument arg = (Connector.Argument)fArgumentMap.get(key);
			FieldEditor editor = (FieldEditor)fFieldEditorMap.get(key);
			if (editor instanceof StringFieldEditor) {
				String value = ((StringFieldEditor)editor).getStringValue();
				if (!arg.isValid(value)) {
					setErrorMessage(arg.label() + LauncherMessages.JavaConnectTab__is_invalid__5); //$NON-NLS-1$
					return false;
				}		
			}
		}				
						
		return true;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.JavaConnectTab_Conn_ect_20; //$NON-NLS-1$
	}			
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugUITools.getImage(IDebugUIConstants.IMG_LCL_DISCONNECT);
	}
		
	/**
	 * Returns the selected connector
	 */
	protected IVMConnector getSelectedConnector() {
		return fConnector;
	}
	/**
	 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		updateLaunchConfigurationDialog();
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
