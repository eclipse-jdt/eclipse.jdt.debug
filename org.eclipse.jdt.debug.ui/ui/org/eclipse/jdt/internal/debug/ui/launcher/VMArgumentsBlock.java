/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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
	private Button fPgrmArgVariableButton;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
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
		fVMArgumentsText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE:
					case SWT.TRAVERSE_PAGE_NEXT:
					case SWT.TRAVERSE_PAGE_PREVIOUS:
						e.doit = true;
						break;
					case SWT.TRAVERSE_RETURN:
					case SWT.TRAVERSE_TAB_NEXT:
					case SWT.TRAVERSE_TAB_PREVIOUS:
						if ((fVMArgumentsText.getStyle() & SWT.SINGLE) != 0) {
							e.doit = true;
						} else {
							if (!fVMArgumentsText.isEnabled() || (e.stateMask & SWT.MODIFIER_MASK) != 0) {
								e.doit = true;
							}
						}
						break;
				}
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
		fVMArgumentsText.setLayoutData(gd);
		fVMArgumentsText.setFont(font);
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});	
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
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);		
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fVMArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "")); //$NON-NLS-1$
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
			JDIDebugUIPlugin.log(e);			
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, getAttributeValueFrom(fVMArgumentsText));
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.VMArgumentsBlock_VM_Arguments; 
	}
	
	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
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
