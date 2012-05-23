/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *      - Bug 214696 Expose WorkingDirectoryBlock as API
 *      - Bug 221973 Make WorkingDirectoryBlock from JDT a Debug API class
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaWorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
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
	
	private WorkingDirectoryBlock fWorkingDirBlock = new JavaWorkingDirectoryBlock();
	
	private JavaJRETab fJRETab = new JavaJRETab();
	
	private VMArgumentsBlock fVMArgumentsBlock = new VMArgumentsBlock();
	
	// launch config template for this scrapbook file
	private ILaunchConfiguration fConfig;
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	private Proxy fProxy;

	class Proxy implements ILaunchConfigurationDialog {
		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#generateName(java.lang.String)
		 */
		public String generateName(String name) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getMode()
		 */
		public String getMode() {
			return ILaunchManager.DEBUG_MODE;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getTabs()
		 */
		public ILaunchConfigurationTab[] getTabs() {
			return new ILaunchConfigurationTab[] {fWorkingDirBlock};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getActiveTab()
		 */
		public ILaunchConfigurationTab getActiveTab() {
			return fWorkingDirBlock;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setName(java.lang.String)
		 */
		public void setName(String name) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateButtons()
		 */
		public void updateButtons() {
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateMessage()
		 */
		public void updateMessage() {
			setValid(isValid());
			setMessage(getMessage());
			setErrorMessage(getErrorMessage());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
		 */
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(org.eclipse.debug.ui.ILaunchConfigurationTab)
		 */
		public void setActiveTab(ILaunchConfigurationTab tab) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(int)
		 */
		public void setActiveTab(int index) {
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(parent.getFont());		
		
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
			JDIDebugUIPlugin.statusDialog(SnippetMessages.getString("ScrapbookLauncher.Unable_to_retrieve_settings"), e.getStatus()); //$NON-NLS-1$
		}

		if (fConfig == null) {
			try {
				fConfig = ScrapbookLauncher.createLaunchConfigurationTemplate(getFile());
				fWorkingCopy = fConfig.getWorkingCopy();
			} catch (CoreException e) {
				JDIDebugUIPlugin.statusDialog(SnippetMessages.getString("ScrapbookLauncher.Unable_to_retrieve_settings"), e.getStatus()); //$NON-NLS-1$
			}
		}
				
		fWorkingDirBlock.setLaunchConfigurationDialog(fProxy);
		fWorkingDirBlock.createControl(comp);		
		fWorkingDirBlock.initializeFrom(fConfig);
		
		fVMArgumentsBlock.setLaunchConfigurationDialog(fProxy);
		fVMArgumentsBlock.createControl(comp);
		fVMArgumentsBlock.initializeFrom(fConfig);		
		
		fJRETab.setLaunchConfigurationDialog(fProxy);
		fJRETab.setVMSpecificArgumentsVisible(false);
		fJRETab.createControl(comp);
		fJRETab.initializeFrom(fConfig);
		
		return comp;
	}

	/**
	 * Returns the snippet page (file)
	 */
	protected IFile getFile() {
		return (IFile)getElement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		fWorkingDirBlock.setDefaults(fWorkingCopy);
		fJRETab.setDefaults(fWorkingCopy);
		fVMArgumentsBlock.setDefaults(fWorkingCopy);
		fWorkingDirBlock.initializeFrom(fWorkingCopy);
		fJRETab.initializeFrom(fWorkingCopy);
		fVMArgumentsBlock.initializeFrom(fWorkingCopy);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		return fWorkingDirBlock.isValid(fConfig);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		String message = fWorkingDirBlock.getErrorMessage();
		if (message == null) {
			return fJRETab.getErrorMessage();
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public String getMessage() {
		String message = fWorkingDirBlock.getMessage();
		if (message == null) {
			return fJRETab.getMessage();
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		fWorkingDirBlock.performApply(fWorkingCopy);
		fJRETab.performApply(fWorkingCopy);
		fVMArgumentsBlock.performApply(fWorkingCopy);
		try {
			if (!fWorkingCopy.contentsEqual(fConfig)) {
				fConfig = fWorkingCopy.doSave();
				fWorkingCopy = fConfig.getWorkingCopy();
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(e.getStatus());
		}
		return super.performOk();
	}
}
