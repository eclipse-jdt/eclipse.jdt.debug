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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This tab appears for local java launch configurations and allows the user to edit
 * program arguments, VM arguments, and the working directory attributes.
 */
public class JavaArgumentsTab extends JavaLaunchConfigurationTab {
		
	// Program arguments UI widgets
	protected Label fPrgmArgumentsLabel;
	protected Text fPrgmArgumentsText;

	// VM arguments UI widgets
	protected Label fVMArgumentsLabel;
	protected Text fVMArgumentsText;
	
	// Working directory
	protected WorkingDirectoryBlock fWorkingDirectoryBlock = new WorkingDirectoryBlock();
		
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
		
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp, 1);
						
		fWorkingDirectoryBlock.createControl(comp);
		
		createVerticalSpacer(comp, 1);
				
		fPrgmArgumentsLabel = new Label(comp, SWT.NONE);
		fPrgmArgumentsLabel.setText(LauncherMessages.getString("JavaArgumentsTab.&Program_arguments__5")); //$NON-NLS-1$
						
		fPrgmArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		fPrgmArgumentsText.setLayoutData(gd);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fVMArgumentsLabel = new Label(comp, SWT.NONE);
		fVMArgumentsLabel.setText(LauncherMessages.getString("JavaArgumentsTab.VM_ar&guments__6")); //$NON-NLS-1$
		
		fVMArgumentsText = new Text(comp, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		fVMArgumentsText.setLayoutData(gd);	
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});	
		
	}
			
	protected void updatePgmArgsFromConfig(ILaunchConfiguration config) {
		try {
			String pgmArgs = EMPTY_STRING;
			if (config != null) {
				pgmArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, EMPTY_STRING);
			}
			fPrgmArgumentsText.setText(pgmArgs);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}
	}
	
	protected void updateVMArgsFromConfig(ILaunchConfiguration config) {
		try {
			String vmArgs = EMPTY_STRING;
			if (config != null) {
				vmArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, EMPTY_STRING);
			}
			fVMArgumentsText.setText(vmArgs);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
		
	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid();
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fPrgmArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			fVMArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "")); //$NON-NLS-1$
			fWorkingDirectoryBlock.initializeFrom(configuration);
		} catch (CoreException e) {
			setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getAttributeValueFrom(fPrgmArgumentsText));
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, getAttributeValueFrom(fVMArgumentsText));
		fWorkingDirectoryBlock.performApply(configuration);
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
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaArgumentsTab.&Arguments_16"); //$NON-NLS-1$
	}	
}

