package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Page to set working directory property on scrapbook page.
 */
public class SnippetEditorPropertyPage extends PropertyPage {
	


	private WorkingDirectoryBlock fWorkingDirBlock = new WorkingDirectoryBlock();
	
	private JavaJRETab fJRETab = new JavaJRETab();
	
	// launch config template for this scrapbook file
	private ILaunchConfiguration fConfig;
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	private Proxy fProxy;

	class Proxy implements ILaunchConfigurationDialog {
		/**
		 * @see ILaunchConfigurationDialog#generateName(String)
		 */
		public String generateName(String name) {
			return null;
		}

		/**
		 * @see ILaunchConfigurationDialog#getMode()
		 */
		public String getMode() {
			return ILaunchManager.DEBUG_MODE;
		}

		/**
		 * @see ILaunchConfigurationDialog#getTabs()
		 */
		public ILaunchConfigurationTab[] getTabs() {
			return new ILaunchConfigurationTab[] {fWorkingDirBlock};
		}

		/**
		 * @see ILaunchConfigurationDialog#getActiveTab()
		 */
		public ILaunchConfigurationTab getActiveTab() {
			return fWorkingDirBlock;
		}

		/**
		 * @see ILaunchConfigurationDialog#setName(String)
		 */
		public void setName(String name) {
		}

		/**
		 * @see ILaunchConfigurationDialog#updateButtons()
		 */
		public void updateButtons() {
			
		}

		/**
		 * @see ILaunchConfigurationDialog#updateMessage()
		 */
		public void updateMessage() {
			setValid(isValid());
			setMessage(fWorkingDirBlock.getMessage());
			setErrorMessage(fWorkingDirBlock.getErrorMessage());
		}

		/**
		 * @see IRunnableContext#run(boolean, boolean, IRunnableWithProgress)
		 */
		public void run(
			boolean fork,
			boolean cancelable,
			IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		}

	}
		
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);				
		
		// fake launch config dialog
		fProxy = new Proxy();
		
		try {
			fConfig = ScrapbookLauncher.getLaunchConfigurationTemplate(getFile());
			if (fConfig != null) {
				fWorkingCopy = fConfig.getWorkingCopy();
			}
		} catch (CoreException e) {
			// unable to retrieve launch config, create a new one
			fConfig = null;
			fWorkingCopy = null;
			JDIDebugUIPlugin.errorDialog(SnippetMessages.getString("SnippetEditorPropertyPage.Unable_to_retrieve_scrapbook_runtime_settings._Settings_will_revert_to_defaults._1"), e); //$NON-NLS-1$
		}

		if (fConfig == null) {
			try {
				fConfig = ScrapbookLauncher.createLaunchConfigurationTemplate(getFile());
				fWorkingCopy = fConfig.getWorkingCopy();
			} catch (CoreException e) {
				JDIDebugUIPlugin.errorDialog(SnippetMessages.getString("SnippetEditorPropertyPage.Unable_to_create_launch_configuration_for_scrapbook_file_2"), e); //$NON-NLS-1$
			}
		}
				
		fWorkingDirBlock.setLaunchConfigurationDialog(fProxy);
		fWorkingDirBlock.createControl(comp);		
		
		fJRETab.setLaunchConfigurationDialog(fProxy);
		fJRETab.createControl(comp);
		
		fWorkingDirBlock.initializeFrom(fConfig);
		fJRETab.initializeFrom(fConfig);
		
		return comp;
	}
	
	/**
	 * Returns the snippet page (file)
	 */
	protected IFile getFile() {
		return (IFile)getElement();
	}
	
	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		fWorkingDirBlock.setDefaults(fWorkingCopy);
		fJRETab.setDefaults(fWorkingCopy);
		fWorkingDirBlock.initializeFrom(fWorkingCopy);
		fJRETab.initializeFrom(fWorkingCopy);
	}
	
	/**
	 * @see IPreferencePage#isValid()
	 */
	public boolean isValid() {
		return fWorkingDirBlock.isValid(fConfig);
	}

	/**
	 * @see IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		String message = fWorkingDirBlock.getErrorMessage();
		if (message != null) {
			return fJRETab.getErrorMessage();
		}
		return null;
	}

	/**
	 * @see IMessageProvider#getMessage()
	 */
	public String getMessage() {
		String message = fWorkingDirBlock.getMessage();
		if (message != null) {
			return fJRETab.getMessage();
		}
		return null;
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fWorkingDirBlock.performApply(fWorkingCopy);
		fJRETab.performApply(fWorkingCopy);
		try {
			if (!fWorkingCopy.contentsEqual(fConfig)) {
				fConfig = fWorkingCopy.doSave();
				fWorkingCopy = fConfig.getWorkingCopy();
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(SnippetMessages.getString("SnippetEditorPropertyPage.Unable_to_save_scrapbook_settings._3"), e); //$NON-NLS-1$
		}
		return super.performOk();
	}
}
