package org.eclipse.jdt.internal.debug.ui.launcher;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddProjectAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddVariableAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
 
/**
 * Control used to edit the source lookup path for a Java launch configuration.
 */
public class SourceLookupBlock {
	
	protected IJavaProject fProject;
	protected ILaunchConfiguration fConfig;
	
	protected RuntimeClasspathViewer fPathViewer;
	protected Button fDefaultButton;
	protected List fActions = new ArrayList(10);
	
	/**
	 * Creates and returns the source lookup control.
	 * 
	 * @param parent the parent widget of this control
	 */
	public Control createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		fPathViewer = new RuntimeClasspathViewer(comp);
		gd = new GridData(GridData.FILL_BOTH);
		fPathViewer.getControl().setLayoutData(gd);

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);

		createVerticalSpacer(comp, 2);
						
		fDefaultButton = new Button(comp, SWT.CHECK);
		fDefaultButton.setText(LauncherMessages.getString("SourceLookupBlock.Use_defau&lt_source_lookup_path_1")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleDefaultButtonSelected();
			}
		});
		
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

		action = new AddExternalJarAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddFolderAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddExternalFolderAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new AddVariableAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		
														
		retargetActions(fPathViewer);
				
		return comp;
	}

	/**
	 * The "default" button has been toggled
	 */
	protected void handleDefaultButtonSelected() {
		boolean def = fDefaultButton.getSelection();
		if (def) {
			try {
				IRuntimeClasspathEntry[] defs = JavaRuntime.computeRuntimeClasspath(getLaunchConfiguration());
				fPathViewer.setEntries(defs);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		fPathViewer.setEnabled(!def);
	}

	/**
	 * Creates and returns a button 
	 * 
	 * @param parent parent widget
	 * @param label label
	 * @param image image
	 * @return Button
	 */
	protected Button createPushButton(
		Composite parent,
		String label,
		Image image) {
		return SWTUtil.createPushButton(parent, label, image);
	}
	
	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
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
	 * Initializes this control based on the settings in the given
	 * launch configuration.
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			setLaunchConfiguration(config);
			IJavaProject project = JavaRuntime.getJavaProject(config);
			setProject(project);
			boolean useDefault = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
			fDefaultButton.setSelection(useDefault);
			IRuntimeClasspathEntry[] entries = JavaRuntime.computeSourceLookupPath(config);
			fPathViewer.setEntries(entries);
			fPathViewer.setEnabled(!useDefault);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
	
	/**
	 * Saves settings in the given working copy
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		boolean def = fDefaultButton.getSelection();		
		if (def) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, (String)null);
		} else {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, def);
			try {
				IRuntimeClasspathEntry[] entries = fPathViewer.getEntries();
				List mementos = new ArrayList(entries.length);
				for (int i = 0; i < entries.length; i++) {
					mementos.add(entries[i].getMemento());
				}
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, mementos);
			} catch (CoreException e) {
				JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("SourceLookupBlock.Unable_to_save_source_lookup_path_1"), e); //$NON-NLS-1$
			}	
		}		
	}	
	
	/**
	 * Returns the entries visible in the viewer
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		return fPathViewer.getEntries();
	}	
	/**
	 * Returns the Java project associated with the launch configuration
	 * or <code>null</code> if none.
	 * 
	 * @return Java project or <code>null</code>
	 */
	protected IJavaProject getProject() {
		return fProject;
	}

	/**
	 * Sets the Java project associated with the launch configuration
	 * or <code>null</code> if none.
	 * 
	 * @param project Java project or <code>null</code>
	 */
	private void setProject(IJavaProject project) {
		fProject = project;
	}
	
	/**
	 * Sets the configuration associated with this source lookup
	 * block.
	 * 
	 * @param configuration launch configuration
	 */
	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fConfig = configuration;
	}
	
	/**
	 * Sets the configuration associated with this source lookup
	 * block.
	 * 
	 * @param configuration launch configuration
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fConfig;
	}	

}
