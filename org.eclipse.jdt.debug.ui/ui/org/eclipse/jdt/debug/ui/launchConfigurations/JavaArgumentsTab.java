package org.eclipse.jdt.debug.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
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
 * A launch configuration tab that displays and edits program arguments,
 * VM arguments, and working directory launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
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
		gd = new GridData(GridData.FILL_BOTH);
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
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		fVMArgumentsText.setLayoutData(gd);	
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});	
		
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
		return fWorkingDirectoryBlock.isValid(config);
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
	
	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
	}	
	/**
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	/**
	 * @see ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}

}

