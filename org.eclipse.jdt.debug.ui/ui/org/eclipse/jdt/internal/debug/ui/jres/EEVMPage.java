/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.StatusInfo;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Page used to edit an 'EE' VM.
 * 
 * @since 3.3
 */
public class EEVMPage extends AbstractVMInstallPage {
	
	// VM being edited or created
	private VMStandin fVM;
	private Text fVMName;
	private Text fVMArgs;
	private Text fEEFile;
	private VMLibraryBlock fLibraryBlock;
	private IStatus[] fFieldStatus = new IStatus[1];
	private boolean fIgnoreCallbacks = false;
	
	/**
	 * 
	 */
	public EEVMPage() {
		super(JREMessages.EEVMPage_0);
		for (int i = 0; i < fFieldStatus.length; i++) {
			fFieldStatus[i] = Status.OK_STATUS;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_WIZBAN_LIBRARY);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite p) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(p, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	// VM location
		SWTFactory.createLabel(composite, JREMessages.EEVMPage_1, 1);
		fEEFile = SWTFactory.createSingleText(composite, 1);
		Button folders = SWTFactory.createPushButton(composite, JREMessages.EEVMPage_2, null);
		GridData data = (GridData) folders.getLayoutData();
		data.horizontalAlignment = GridData.END;
	//VM name
		SWTFactory.createLabel(composite, JREMessages.addVMDialog_jreName, 1);
		fVMName = SWTFactory.createSingleText(composite, 2);
	//VM arguments
		Label label = SWTFactory.createLabel(composite, JREMessages.AddVMDialog_23, 2);
		GridData gd = (GridData) label.getLayoutData();
		gd.verticalAlignment = SWT.BEGINNING;
		fVMArgs = SWTFactory.createText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP, 3, ""); //$NON-NLS-1$
		gd = (GridData) fVMArgs.getLayoutData();
		gd.widthHint = 200;
		gd.heightHint = 75;
	//Variables button
		Button variables = SWTFactory.createPushButton(composite, JREMessages.EEVMPage_3, null);
		gd = (GridData) variables.getLayoutData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.END;
	//VM libraries block 
		SWTFactory.createLabel(composite, JREMessages.AddVMDialog_JRE_system_libraries__1, 3);
		fLibraryBlock = new VMLibraryBlock();
		fLibraryBlock.setWizard(getWizard());
		fLibraryBlock.createControl(composite);
		Control libControl = fLibraryBlock.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		libControl.setLayoutData(gd);
		
		initializeFields();
	//add the listeners now to prevent them from monkeying with initialized settings
		fVMName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!fIgnoreCallbacks) {
					validateVMName();
				}
			}
		});
		fEEFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!fIgnoreCallbacks) {
					if (validateDefinitionFile().isOK()) {
						reloadDefinitionFile();
					}
				}
			}
		});
		folders.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFilterExtensions(new String[]{"*.ee"}); //$NON-NLS-1$
				File file = getDefinitionFile();
				String text = fEEFile.getText();
				if (file != null && file.isFile()) {
					text = file.getParentFile().getAbsolutePath();
				}
				dialog.setFileName(text); 
				String newPath = dialog.open();
				if (newPath != null) {
					fEEFile.setText(newPath);
				}
			}
		});
		variables.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
				if (dialog.open() == Window.OK) {
					String expression = dialog.getVariableExpression();
					if (expression != null) {
						fVMArgs.insert(expression);
					}
				}
			}
		});		
		Dialog.applyDialogFont(composite);
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.EDIT_JRE_EE_FILE_WIZARD_PAGE);	
	}	
	
	/**
	 * Validates the JRE location
	 * @return the status after validating the JRE location
	 */
	private IStatus validateDefinitionFile() {
		String locationName = fEEFile.getText();
		IStatus s = null;
		File file = null;
		if (locationName.length() == 0) {
			s = new StatusInfo(IStatus.WARNING, JREMessages.EEVMPage_4); 
		}  else {
			file = new File(locationName);
			if (!file.exists()) {
				s = new StatusInfo(IStatus.ERROR, JREMessages.EEVMPage_5); 
			} else {
				final IStatus[] temp = new IStatus[1];
				final VMStandin[] vm = new VMStandin[1];
				final File tempFile = file; 
				Runnable r = new Runnable() {
					public void run() {
						try {
							vm[0] = JavaRuntime.createVMFromDefinitionFile(tempFile, fVM.getName(), fVM.getId());
							IStatus status = vm[0].getVMInstallType().validateInstallLocation(vm[0].getInstallLocation());
							if (status.getSeverity() != IStatus.ERROR) {
								temp[0] = Status.OK_STATUS;
							} else {
								temp[0] = status;
							}
						} catch (CoreException e) {
							temp[0] = e.getStatus();
						}
					}
				};
				BusyIndicator.showWhile(getShell().getDisplay(), r);
				s = temp[0];
			}
		}
		setDefinitionFileStatus(s);
		updatePageStatus();
		return s;
	}
	
	/**
	 * Initializes the JRE attributes from the definition file
	 */
	private void reloadDefinitionFile() {
		IStatus s = Status.OK_STATUS;
		File file = getDefinitionFile();
		if (file != null && file.exists()) {
			final IStatus[] temp = new IStatus[1];
			final VMStandin[] vm = new VMStandin[1];
			final File tempFile = file; 
			Runnable r = new Runnable() {
				public void run() {
					try {
						vm[0] = JavaRuntime.createVMFromDefinitionFile(tempFile, fVM.getName(), fVM.getId());
						temp[0] = Status.OK_STATUS;
					} catch (CoreException e) {
						temp[0] = e.getStatus();
					}
				}
			};
			BusyIndicator.showWhile(getShell().getDisplay(), r);
			s = temp[0];
			if (s.isOK()) {
				fVM = vm[0];
			}
		}
		if (s.isOK() && file != null) {
			initializeFields();
		}
		setDefinitionFileStatus(s);
	}	

	/**
	 * Validates the entered name of the VM
	 */
	private void validateVMName() {
		nameChanged(fVMName.getText());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#finish()
	 */
	@Override
	public boolean finish() {
		setFieldValuesToVM(fVM);
		fLibraryBlock.finish();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#getSelection()
	 */
	@Override
	public VMStandin getSelection() {
		return fVM;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#setSelection(org.eclipse.jdt.launching.VMStandin)
	 */
	@Override
	public void setSelection(VMStandin vm) {
		super.setSelection(vm);
		fVM = vm;
		setTitle(JREMessages.EEVMPage_6);
		setDescription(JREMessages.EEVMPage_7);
	}
	
	/**
	 * initialize fields to the specified VM
	 * @param vm the VM to initialize from
	 */
	protected void setFieldValuesToVM(VMStandin vm) {
		vm.setName(fVMName.getText());
		String argString = fVMArgs.getText().trim();
		if (argString != null && argString.length() > 0) {
			vm.setVMArgs(argString);			
		} else {
			vm.setVMArgs(null);
		}
	}
	
	/**
	 * Returns the definition file from the text control or <code>null</code>
	 * if none.
	 * 
	 * @return definition file or <code>null</code>
	 */
	private File getDefinitionFile() {
		String path = fEEFile.getText().trim();
		if (path.length() > 0) {
			return new File(path);
		}
		return null;
	}
	
	/**
	 * Creates a unique name for the VMInstallType
	 * @param vmType the vm install type
	 * @return a unique name
	 */
	protected static String createUniqueId(IVMInstallType vmType) {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null);
		return id;
	}	
	
	/**
	 * Initialize the dialogs fields
	 */
	private void initializeFields() {
		try {
			fIgnoreCallbacks = true;
			fLibraryBlock.setSelection(fVM);
			fVMName.setText(fVM.getName());
			String eePath = fVM.getAttribute(EEVMInstall.ATTR_DEFINITION_FILE);
			if (eePath != null) {
				fEEFile.setText(eePath);
			}
			String vmArgs = fVM.getVMArgs();
			if (vmArgs != null) {
				fVMArgs.setText(vmArgs);
			}
			validateVMName();
			validateDefinitionFile();
		} finally {
			fIgnoreCallbacks = false;
		}
	}	
	
	/**
	 * Sets the status of the definition file.
	 * 
	 * @param status definition file status
	 */
	private void setDefinitionFileStatus(IStatus status) {
		fFieldStatus[0] = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		String message = super.getErrorMessage();
		if (message == null) {
			return fLibraryBlock.getErrorMessage();
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	@Override
	public boolean isPageComplete() {
		boolean complete = super.isPageComplete();
		if (complete) {
			return fLibraryBlock.isPageComplete();
		}
		return complete;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#getVMStatus()
	 */
	@Override
	protected IStatus[] getVMStatus() {
		return fFieldStatus;
	}
	
	
}
