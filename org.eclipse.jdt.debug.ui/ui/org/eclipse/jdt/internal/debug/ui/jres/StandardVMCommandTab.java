/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class StandardVMCommandTab extends AbstractLaunchConfigurationTab {

	protected Text fJavaCommandText;
	protected Button fDefaultButton;
	protected Button fSpecificButton;

	protected static final Map<?, ?> EMPTY_MAP = new HashMap<>(1);

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = new Composite(parent, parent.getStyle());
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setFont(font);

		Group group = new Group(comp, SWT.NONE);
		setControl(group);
		GridLayout topLayout = new GridLayout();
		group.setLayout(topLayout);
		topLayout.numColumns = 2;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setFont(font);

		group.setText(JREMessages.AbstractJavaCommandTab_1);

		fDefaultButton = new Button(group, SWT.RADIO);
		fDefaultButton.setFont(font);
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.setText(NLS.bind(JREMessages.AbstractJavaCommandTab_2, getDefaultCommand()));

		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSelection();
			}
		});

		fSpecificButton = new Button(group, SWT.RADIO);
		fSpecificButton.setFont(font);
		gd = new GridData(GridData.BEGINNING);
		fSpecificButton.setLayoutData(gd);
		fSpecificButton.setText(JREMessages.AbstractJavaCommandTab_4);

		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSelection();
			}
		});

		fJavaCommandText= new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJavaCommandText.setLayoutData(gd);
		fJavaCommandText.setFont(font);
		fJavaCommandText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		ControlAccessibleListener.addListener(fJavaCommandText, fSpecificButton.getText());
		setControl(group);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return JREMessages.AbstractJavaCommandTab_3;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		String javaCommand= null;
		try {
			Map<String, String> attributeMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map<String, String>)null);
			if (attributeMap != null) {
				javaCommand = attributeMap.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fDefaultButton.getSelection()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map<String, String>)null);
		} else {
			String javaCommand = fJavaCommandText.getText();
			Map<String, String> attributeMap = new HashMap<>(1);
			attributeMap.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, javaCommand);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, attributeMap);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map<String, String>)null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return "org.eclipse.jdt.debug.ui.standardVMCommandTab"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid= fDefaultButton.getSelection() || fJavaCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		} else {
			setErrorMessage(JREMessages.AbstractJavaCommandTab_Java_executable_must_be_specified_5);
			setMessage(null);
		}
		return valid;
	}
}
