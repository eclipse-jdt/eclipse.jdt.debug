/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Allows the user to specify if they want to delete the working set, all the breakpoints in the working
 * set or both
 * @since 3.2
 */
public class OverrideDependenciesDialog extends MessageDialog {
	Text fModuleArgumentsText;
	Text fModuleArgumentsNewText;
	ILaunchConfiguration flaunchConfiguration;


	public OverrideDependenciesDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, ILaunchConfiguration config) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		flaunchConfiguration = config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		Font font = parent.getFont();

		Group group = new Group(comp, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		group.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(20);
		gd.widthHint = convertWidthInCharsToPixels(70);
		group.setLayoutData(gd);
		group.setFont(font);


		Label description = new Label(group, SWT.WRAP);
		description.setText(ActionMessages.Override_Dependencies_label1);
		fModuleArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(10);
		gd.widthHint = convertWidthInCharsToPixels(60);
		fModuleArgumentsText.setLayoutData(gd);


		Label description1 = new Label(group, SWT.WRAP);
		description1.setText(ActionMessages.Override_Dependencies_label2);
		fModuleArgumentsNewText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(10);
		gd.widthHint = convertWidthInCharsToPixels(60);
		fModuleArgumentsNewText.setLayoutData(gd);

		String moduleCLIOptions = ""; //$NON-NLS-1$
		try {
			AbstractJavaLaunchConfigurationDelegate delegate = getJavaLaunchConfigurationDelegate();
			if (delegate != null) {
				moduleCLIOptions = delegate.getModuleCLIOptions(flaunchConfiguration);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		fModuleArgumentsText.setText(moduleCLIOptions);
		fModuleArgumentsText.setEditable(false);
		try {
			if (!flaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true)) {
				String str = flaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, "");//$NON-NLS-1$
				fModuleArgumentsNewText.setText(str);

			} else {
				fModuleArgumentsNewText.setText(moduleCLIOptions);
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return comp;
	}

	public AbstractJavaLaunchConfigurationDelegate getJavaLaunchConfigurationDelegate() throws CoreException {
		Set<String> modes = flaunchConfiguration.getModes();
		modes.add(ILaunchManager.RUN_MODE);
		AbstractJavaLaunchConfigurationDelegate delegate = null;
		for (ILaunchDelegate launchDelegate : flaunchConfiguration.getType().getDelegates(modes)) {
			if (launchDelegate.getDelegate() instanceof AbstractJavaLaunchConfigurationDelegate) {
				delegate = (AbstractJavaLaunchConfigurationDelegate) launchDelegate.getDelegate();
				break;
			}
		}
		return delegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if(buttonId == OK) {
			// Save if overridden
			if (!fModuleArgumentsNewText.getText().equals(fModuleArgumentsText.getText())) {
				ILaunchConfigurationWorkingCopy workingCopy;
				try {
					workingCopy = flaunchConfiguration.getWorkingCopy();
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, false);
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, fModuleArgumentsNewText.getText());
					workingCopy.doSave();
				}
				catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			} else {
				ILaunchConfigurationWorkingCopy workingCopy;
				try {
					workingCopy = flaunchConfiguration.getWorkingCopy();
					boolean attribute = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true);
					if (!attribute) {
						workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true);
						workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, fModuleArgumentsNewText.getText());
						workingCopy.doSave();
					}

				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			}

		}
		super.buttonPressed(buttonId);
	}



}
