package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;

public class MainTab implements ILaunchConfigurationTab {

	/*
	 * @see ILaunchConfigurationTab#createTabControl(TabItem)
	 */
	public Control createTabControl(TabItem tabItem) {
		return null;
	}

	/*
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
	}

	/*
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

}

