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
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Editor for VM arguments of a Java launch configuration.
 */
public class VMArgumentsBlock extends JavaLaunchTab {

	// VM arguments widgets
	protected Text fVMArgumentsText;
	private Button fUseStartOnFirstThread = null;
	private Button fHelpfulExceptions = null;
	private Button fUseArgfile = null;
	private Button fPgrmArgVariableButton;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Group group = new Group(parent, SWT.NONE);
		setControl(group);
		GridLayout topLayout = new GridLayout();
		group.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd);
		group.setFont(font);
		group.setText(LauncherMessages.JavaArgumentsTab_VM_ar_guments__6);

		fVMArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		fVMArgumentsText.addTraverseListener(e -> {
			switch (e.detail) {
				case SWT.TRAVERSE_ESCAPE:
				case SWT.TRAVERSE_PAGE_NEXT:
				case SWT.TRAVERSE_PAGE_PREVIOUS:
					e.doit = true;
					break;
				case SWT.TRAVERSE_RETURN:
				case SWT.TRAVERSE_TAB_NEXT:
				case SWT.TRAVERSE_TAB_PREVIOUS:
					if (((fVMArgumentsText.getStyle() & SWT.SINGLE) != 0) || (!fVMArgumentsText.isEnabled() || (e.stateMask & SWT.MODIFIER_MASK) != 0)) {
						e.doit = true;
					}
					break;
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
		fVMArgumentsText.setLayoutData(gd);
		fVMArgumentsText.setFont(font);
		fVMArgumentsText.addModifyListener(evt -> scheduleUpdateJob());
		ControlAccessibleListener.addListener(fVMArgumentsText, group.getText());

		fPgrmArgVariableButton = createPushButton(group, LauncherMessages.VMArgumentsBlock_4, null);
		fPgrmArgVariableButton.setFont(font);
		fPgrmArgVariableButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		fPgrmArgVariableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
				dialog.open();
				String variable = dialog.getVariableExpression();
				if (variable != null) {
					fVMArgumentsText.insert(variable);
				}
			}
		});

		if(Platform.OS_MACOSX.equals(Platform.getOS())) {
			fUseStartOnFirstThread = SWTFactory.createCheckButton(group, LauncherMessages.VMArgumentsBlock_0, null, false, 1);
			fUseStartOnFirstThread.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					scheduleUpdateJob();
				}
			});
		}
		fHelpfulExceptions = SWTFactory.createCheckButton(group, LauncherMessages.VMArgumentsBlock_2, null, true, 1);
		fHelpfulExceptions.setEnabled(false);
		fHelpfulExceptions.setToolTipText(LauncherMessages.JavaArgumentsTab_AttributeTooltip_ActivateHelpfulNullPointerExceptions);
		fHelpfulExceptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduleUpdateJob();
			}
		});
		fUseArgfile = SWTFactory.createCheckButton(group, LauncherMessages.VMArgumentsBlock_3, null, true, 1);
		fUseArgfile.setEnabled(false);
		fUseArgfile.setToolTipText(LauncherMessages.JavaArgumentsTab_AttributeTooltip_UseArgfile);
		fUseArgfile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduleUpdateJob();
			}
		});
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, true);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, false);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fVMArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "")); //$NON-NLS-1$
			if(fUseStartOnFirstThread != null) {
				fUseStartOnFirstThread.setSelection(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true));
			}
			if (fHelpfulExceptions != null) {
				fHelpfulExceptions.setSelection(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, true));
			}
			if (fUseArgfile != null) {
				fUseArgfile.setSelection(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, false));
			}
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage());
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, getAttributeValueFrom(fVMArgumentsText));
		if(fUseStartOnFirstThread != null) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, fUseStartOnFirstThread.getSelection());
		}
		if (isJavaNewerThan(configuration, JavaCore.VERSION_13)) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, fHelpfulExceptions.getSelection());
			fHelpfulExceptions.setEnabled(true);
		} else {
			fHelpfulExceptions.setEnabled(false);
		}
		if (isJavaNewerThan(configuration, JavaCore.VERSION_1_8)) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, fUseArgfile.getSelection());
			fUseArgfile.setEnabled(true);
		} else {
			fUseArgfile.setEnabled(false);
		}
	}

	/**
	 *
	 * @param version
	 *            string eg. org.eclipse.jdt.core.JavaCore.VERSION_14
	 */
	private boolean isJavaNewerThan(ILaunchConfiguration configuration, String version) {
		try {
			return JavaRuntime.compareJavaVersions(JavaRuntime.computeVMInstall(configuration), version) > 0;
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return LauncherMessages.VMArgumentsBlock_VM_Arguments;
	}

	/**
	 * Returns the string in the text widget, or <code>null</code> if empty.
	 *
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

	public void setEnabled(boolean enabled) {
		fVMArgumentsText.setEnabled(enabled);
		fPgrmArgVariableButton.setEnabled(enabled);
	}
}