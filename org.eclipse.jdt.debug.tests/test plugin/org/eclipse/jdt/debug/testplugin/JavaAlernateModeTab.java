/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Test tab contribution to an existing tab group.
 * 
 * @since 3.3
 */
public class JavaAlernateModeTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationListener {
	
	private Button fAlternateModeCheckBox;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchModeConfigurationTab#getModes()
	 */
	public Set getModes() {
		HashSet modes = new HashSet();
		modes.add("alternate");
		return modes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Alternate";
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
		super.dispose();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fAlternateModeCheckBox.getSelection()) {
			configuration.addModes(getModes());
		} else {
			configuration.removeModes(getModes());
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	public void createControl(Composite parent) {
		fAlternateModeCheckBox = new Button(parent, SWT.CHECK);
		fAlternateModeCheckBox.setText("Check &me for 'alternate' mode");
		fAlternateModeCheckBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		setControl(fAlternateModeCheckBox);
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	public String getId() {
		return "org.eclipse.jdt.debug.tests.javaAlternateModeTab";
	}

	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		try {
			Set modes = configuration.getModes();
			modes.add(getLaunchConfigurationDialog().getMode());
			if(!fAlternateModeCheckBox.isDisposed()) {
				fAlternateModeCheckBox.setSelection(modes.contains("alternate"));
			}
		}
		catch(CoreException ce) {DebugUIPlugin.log(ce);}
	}

}
