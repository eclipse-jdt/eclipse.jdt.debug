package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractJavaCommandTab extends AbstractLaunchConfigurationTab {
	
	protected Text fJavaCommandText;
	protected Button fDefaultButton;
	
	protected static final Map EMPTY_MAP = new HashMap(1);
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		topLayout.marginWidth= 0;
		topLayout.marginHeight= 0;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
		
		createVerticalSpacer(comp, 2);
		
		Label javaCommandLabel= new Label(comp, SWT.NONE);
		javaCommandLabel.setText(LauncherMessages.getString("AbstractJavaCommandTab.Name_of_Java_e&xecutable__1"));  //$NON-NLS-1$
		
		fJavaCommandText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJavaCommandText.setLayoutData(gd);
		fJavaCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fDefaultButton = new Button(comp, SWT.CHECK);
		fDefaultButton.setText(LauncherMessages.getString("AbstractJavaCommandTab.Use_de&fault_Java_executable_2")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleDefaultButtonSelected(fDefaultButton.getSelection());
			}
		});

		setControl(comp);
	}

	protected void handleDefaultButtonSelected(boolean useDefault) {
		if (useDefault) {
			fJavaCommandText.setText(getCommand());
		} 
		fJavaCommandText.setEnabled(!useDefault);
	}
	
	protected abstract String getCommand();

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("AbstractJavaCommandTab.Standard_VM_Java_Command_3"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String javaCommand= LauncherMessages.getString("AbstractJavaCommandTab._4"); //$NON-NLS-1$
		try {
			Map attributeMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, EMPTY_MAP);
			if (attributeMap != null) {
				javaCommand = (String) attributeMap.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
				if (javaCommand == null) {
					javaCommand = "javaw"; //$NON-NLS-1$
				}
			}
		} catch(CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
		fJavaCommandText.setText(javaCommand);
		if (javaCommand.equals(getCommand())) {
			//using the default
			fDefaultButton.setSelection(true);
			handleDefaultButtonSelected(true);
		} else {
			fDefaultButton.setSelection(false);
			handleDefaultButtonSelected(false);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String javaCommand = fJavaCommandText.getText();
		Map attributeMap = new HashMap(1);
		attributeMap.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, javaCommand); //$NON-NLS-1$
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, attributeMap);	
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid= fJavaCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		} else {
			setErrorMessage(LauncherMessages.getString("AbstractJavaCommandTab.Java_executable_must_be_specified_5")); //$NON-NLS-1$
			setMessage(null);
		}
		return valid;
	}
}