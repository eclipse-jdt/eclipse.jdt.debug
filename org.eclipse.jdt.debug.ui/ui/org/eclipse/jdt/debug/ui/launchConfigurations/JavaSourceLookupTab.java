package org.eclipse.jdt.debug.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceLookupBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A launch configuration tab that displays and edits source lookup
 * path launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class JavaSourceLookupTab extends JavaLaunchConfigurationTab {

	protected SourceLookupBlock fSourceLookupBlock;
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_SOURCE_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(parent.getFont());	
		
		createVerticalSpacer(comp, 1);
		
		fSourceLookupBlock = new SourceLookupBlock();
		fSourceLookupBlock.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		fSourceLookupBlock.createControl(comp);
		GridData gd = (GridData)fSourceLookupBlock.getControl().getLayoutData();
		gd.heightHint = 200;
		gd.widthHint = 250;				
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// be default, use a prompting source locator
		configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, (String)null);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, (List)null);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		getControl().setRedraw(false);
		fSourceLookupBlock.initializeFrom(configuration);
		getControl().setRedraw(true);
	}
	
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
		fSourceLookupBlock.performApply(configuration);
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaSourceLookupTab.Source_1"); //$NON-NLS-1$
	}
	
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}	
		
}
