package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


/**
 * <b>THIS CLASS IS EXPERIMENTAL AND STILL UNDER CONSTRUCTION</b>
 * 
 * Tab for setting classpath and bootpath.
 */
public class JavaSourceLookupTab extends JavaLaunchConfigurationTab {

	protected SourceLookupBlock2 fSourceLookupBlock;
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);		
		GridData gd;
		
		
		createVerticalSpacer(comp, 1);
		
		fSourceLookupBlock = new SourceLookupBlock2();
		fSourceLookupBlock.createControl(comp);
		
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fSourceLookupBlock.initializeFrom(configuration);
	}
	
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		fSourceLookupBlock.performApply(configuration);
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return isValid();
	}
	
	/**
	 * @see ILaunchConfigurationTab#isValid()
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaSourceLookupTab.Source_1"); //$NON-NLS-1$
	}
		
}
