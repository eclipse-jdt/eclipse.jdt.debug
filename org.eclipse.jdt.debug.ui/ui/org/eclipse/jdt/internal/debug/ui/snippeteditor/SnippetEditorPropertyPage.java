package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Page to set working directory property on scrapbook page.
 */
public class SnippetEditorPropertyPage extends PropertyPage {
	


	private WorkingDirectoryBlock fWorkingDirBlock = new WorkingDirectoryBlock();
	
	// temporary config used to make working dir block work like a tab
	private ILaunchConfigurationWorkingCopy fConfig;
	
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
		fProxy = new Proxy();
		fWorkingDirBlock.setLaunchConfigurationDialog(fProxy);
		fWorkingDirBlock.createControl(parent);
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		try {
			fConfig = type.newInstance(null, "TEMP_CONFIG"); //$NON-NLS-1$
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		String attr = ScrapbookLauncher.getWorkingDirectoryAttribute(getFile());
		fConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, attr);
		fWorkingDirBlock.initializeFrom(fConfig);
		return fWorkingDirBlock.getControl();
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
		fConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		fWorkingDirBlock.initializeFrom(fConfig);
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
		return fWorkingDirBlock.getErrorMessage();
	}

	/**
	 * @see IMessageProvider#getMessage()
	 */
	public String getMessage() {
		return fWorkingDirBlock.getMessage();
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fWorkingDirBlock.performApply(fConfig);
		String attr = null;
		try {
			attr = fConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(SnippetMessages.getString("SnippetEditorPropertyPage.Unable_to_save_working_directory._2"), e); //$NON-NLS-1$
		}
		ScrapbookLauncher.setWorkingDirectoryAttribute(getFile(), attr);
		return super.performOk();
	}
}
