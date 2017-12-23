/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Allows the user to specify if they want to delete the working set, all the breakpoints in the working
 * set or both
 * @since 3.2
 */
public class OverrideDependenciesDialog extends MessageDialog {
	Text fModuleArgumentsText;
	String fOriginalText;
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
		gd.heightHint = convertHeightInCharsToPixels(15);
		gd.widthHint = convertWidthInCharsToPixels(45);
		group.setLayoutData(gd);
		group.setFont(font);

		fModuleArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(10);
		gd.widthHint = convertWidthInCharsToPixels(35);
		fModuleArgumentsText.setLayoutData(gd);
		try {
			if (!flaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true)) {
				String str = flaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, "");//$NON-NLS-1$
				fModuleArgumentsText.setText(str);

			} else {
				AbstractJavaLaunchConfigurationDelegate delegate = getJavaLaunchConfigurationDelegate();
				if (delegate != null) {
					fModuleArgumentsText.setText(delegate.getModuleCLIOptions(flaunchConfiguration));
				}
			}
			fOriginalText = fModuleArgumentsText.getText();

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
			if (!fModuleArgumentsText.getText().equals(fOriginalText)) {
				ILaunchConfigurationWorkingCopy workingCopy;
				try {
					workingCopy = flaunchConfiguration.getWorkingCopy();
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, false);
					workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, fModuleArgumentsText.getText());
					workingCopy.doSave();
				}
				catch (CoreException e) {
					e.printStackTrace();
				}
			}

		}
		super.buttonPressed(buttonId);
	}



}
