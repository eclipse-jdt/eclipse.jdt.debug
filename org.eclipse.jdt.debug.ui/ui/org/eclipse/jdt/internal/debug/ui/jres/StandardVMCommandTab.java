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
package org.eclipse.jdt.internal.debug.ui.jres;


import java.text.MessageFormat;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class StandardVMCommandTab extends AbstractLaunchConfigurationTab {
	
	protected Text fJavaCommandText;
	protected Button fDefaultButton;
	protected Button fSpecificButton;
	
	protected static final Map EMPTY_MAP = new HashMap(1);
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		topLayout.numColumns = 2;
		topLayout.marginWidth= 0;
		topLayout.marginHeight= 0;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
		comp.setFont(font);
		
		createVerticalSpacer(comp, 2);
		
		Label javaCommandLabel= new Label(comp, SWT.NONE);
		javaCommandLabel.setText(JREMessages.getString("AbstractJavaCommandTab.1"));  //$NON-NLS-1$
		javaCommandLabel.setFont(font);
		
		fDefaultButton = new Button(comp, SWT.RADIO);
		fDefaultButton.setFont(font);
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.setText(MessageFormat.format(JREMessages.getString("AbstractJavaCommandTab.2"), new String[]{getDefaultCommand()})); //$NON-NLS-1$
		
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSelection();
			}
		});
		
		fSpecificButton = new Button(comp, SWT.RADIO);
		fSpecificButton.setFont(font);
		gd = new GridData(GridData.BEGINNING);
		fSpecificButton.setLayoutData(gd);
		fSpecificButton.setText(JREMessages.getString("AbstractJavaCommandTab.4")); //$NON-NLS-1$
		
		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSelection();
			}
		});
				
		fJavaCommandText= new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJavaCommandText.setLayoutData(gd);
		fJavaCommandText.setFont(font);
		fJavaCommandText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		setControl(comp);
	}

	protected void handleSelection() {
		boolean useDefault = fDefaultButton.getSelection();
		fDefaultButton.setSelection(useDefault);
		fSpecificButton.setSelection(!useDefault);
		fJavaCommandText.setEnabled(!useDefault);
		updateLaunchConfigurationDialog();
	}
	
	protected String getDefaultCommand() {
		return "javaw";  //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return JREMessages.getString("AbstractJavaCommandTab.3"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String javaCommand= null;
		try {
			Map attributeMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
			if (attributeMap != null) {
				javaCommand = (String) attributeMap.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
			}
		} catch(CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
		if (javaCommand == null) {
			javaCommand = getDefaultCommand();
		}
		fJavaCommandText.setText(javaCommand);
		if (javaCommand.equals(getDefaultCommand())) {
			//using the default
			fDefaultButton.setSelection(true);
		} else {
			fDefaultButton.setSelection(false);
		}
		handleSelection();
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fDefaultButton.getSelection()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
		} else {
			String javaCommand = fJavaCommandText.getText();
			Map attributeMap = new HashMap(1);
			attributeMap.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, javaCommand);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, attributeMap);		
		}		 
	}
	
	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
	}	

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid= fDefaultButton.getSelection() || fJavaCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		} else {
			setErrorMessage(JREMessages.getString("AbstractJavaCommandTab.Java_executable_must_be_specified_5")); //$NON-NLS-1$
			setMessage(null);
		}
		return valid;
	}
}
