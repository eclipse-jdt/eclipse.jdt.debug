/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.debug.ui.DialogSettingsHelper;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class AddVMDialog extends StatusDialog {

	private IAddVMDialogRequestor fRequestor;
	
	private IVMInstall fEditedVM;

	private IVMInstallType[] fVMTypes;
	private IVMInstallType fSelectedVMType;
	
	private ComboDialogField fVMTypeCombo;
	private VMLibraryBlock fLibraryBlock;
	
	private StringButtonDialogField fJRERoot;
	private StringDialogField fVMName;
	
	private StringDialogField fVMArgs;

	// the VM install's javadoc location
	private URL fJavadocLocation = null;
	private boolean fAutoDetectJavadocLocation = false;
	
	private IStatus[] fStati;
	private int fPrevIndex = -1;
		
	public AddVMDialog(IAddVMDialogRequestor requestor, Shell shell, IVMInstallType[] vmInstallTypes, IVMInstall editedVM) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fRequestor= requestor;
		fStati= new IStatus[5];
		for (int i= 0; i < fStati.length; i++) {
			fStati[i]= new StatusInfo();
		}
		
		fVMTypes= vmInstallTypes;
		fSelectedVMType= editedVM != null ? editedVM.getVMInstallType() : vmInstallTypes[0];
		
		fEditedVM= editedVM;
		
		//only detect the javadoc location if not already set
		fAutoDetectJavadocLocation = fEditedVM == null || fEditedVM.getJavadocLocation() == null;
	}
	
	/**
	 * @see Windows#configureShell
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaDebugHelpContextIds.EDIT_JRE_DIALOG);
	}		
	
	protected void createDialogFields() {
		fVMTypeCombo= new ComboDialogField(SWT.READ_ONLY);
		fVMTypeCombo.setLabelText(JREMessages.addVMDialog_jreType); //$NON-NLS-1$
		fVMTypeCombo.setItems(getVMTypeNames());
		
		fVMName= new StringDialogField();
		fVMName.setLabelText(JREMessages.addVMDialog_jreName); //$NON-NLS-1$
		
		fJRERoot= new StringButtonDialogField(new IStringButtonAdapter() {
			public void changeControlPressed(DialogField field) {
				browseForInstallDir();
			}
		});
		fJRERoot.setLabelText(JREMessages.addVMDialog_jreHome); //$NON-NLS-1$
		fJRERoot.setButtonLabel(JREMessages.addVMDialog_browse1); //$NON-NLS-1$
			
		fVMArgs= new StringDialogField();
		fVMArgs.setLabelText(JREMessages.AddVMDialog_23); //$NON-NLS-1$
	}
	
	protected void createFieldListeners() {
		fVMTypeCombo.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				updateVMType();
			}
		});
		
		fVMName.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setVMNameStatus(validateVMName());
				updateStatusLine();
			}
		});
		
		fJRERoot.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setJRELocationStatus(validateJRELocation());
				updateStatusLine();
			}
		});
	
	}
	
	protected String getVMName() {
		return fVMName.getText();
	}
		
	protected File getInstallLocation() {
		return new File(fJRERoot.getText());
	}
		
	protected Control createDialogArea(Composite ancestor) {
		createDialogFields();
		Composite parent = (Composite)super.createDialogArea(ancestor);
		((GridLayout)parent.getLayout()).numColumns= 3;
		
		fVMTypeCombo.doFillIntoGrid(parent, 3);
		((GridData)fVMTypeCombo.getComboControl(null).getLayoutData()).widthHint= convertWidthInCharsToPixels(50);

		fVMName.doFillIntoGrid(parent, 3);
	
		fJRERoot.doFillIntoGrid(parent, 3);
		
		fVMArgs.doFillIntoGrid(parent, 3);
		
		Label l = new Label(parent, SWT.NONE);
		l.setText(JREMessages.AddVMDialog_JRE_system_libraries__1); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		l.setLayoutData(gd);	
		
		fLibraryBlock = new VMLibraryBlock(this);
		Control block = fLibraryBlock.createControl(parent);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		block.setLayoutData(gd);
		
		Text t= fJRERoot.getTextControl(parent);
		gd= (GridData)t.getLayoutData();
		gd.grabExcessHorizontalSpace=true;
		gd.widthHint= convertWidthInCharsToPixels(50);
		
		initializeFields();
		createFieldListeners();
		applyDialogFont(parent);
		return parent;
	}
	
	private void updateVMType() {
		int selIndex= fVMTypeCombo.getSelectionIndex();
		if (selIndex == fPrevIndex) {
			return;
		}
		fPrevIndex = selIndex;
		if (selIndex >= 0 && selIndex < fVMTypes.length) {
			fSelectedVMType= fVMTypes[selIndex];
		}
		setJRELocationStatus(validateJRELocation());
		fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
		updateStatusLine();
	}	
	
	public void create() {
		super.create();
		fVMName.setFocus();
		selectVMType();  
	}
	
	private String[] getVMTypeNames() {
		String[] names=  new String[fVMTypes.length];
		for (int i= 0; i < fVMTypes.length; i++) {
			names[i]= fVMTypes[i].getName();
		}
		return names;
	}
	
	private void selectVMType() {
		for (int i= 0; i < fVMTypes.length; i++) {
			if (fSelectedVMType == fVMTypes[i]) {
				fVMTypeCombo.selectItem(i);
				return;
			}
		}
	}
	
	private void initializeFields() {
		fVMTypeCombo.setItems(getVMTypeNames());
		if (fEditedVM == null) {
			fVMName.setText(""); //$NON-NLS-1$
			fJRERoot.setText(""); //$NON-NLS-1$
			fLibraryBlock.initializeFrom(null, fSelectedVMType);
			fVMArgs.setText(""); //$NON-NLS-1$
		} else {
			fVMTypeCombo.setEnabled(false);
			fVMName.setText(fEditedVM.getName());
			fJRERoot.setText(fEditedVM.getInstallLocation().getAbsolutePath());
			fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
			if (fEditedVM instanceof IVMInstall2) {
				IVMInstall2 vm2 = (IVMInstall2) fEditedVM;
				String vmArgs = vm2.getVMArgs();
				if (vmArgs != null) {
					fVMArgs.setText(vmArgs);
				}
			} else {
				String[] vmArgs = fEditedVM.getVMArguments();
				if (vmArgs != null) {
					StringBuffer buffer = new StringBuffer();
					int length= vmArgs.length;
					if (length > 0) {
						buffer.append(vmArgs[0]);
						for (int i = 1; i < length; i++) {
							buffer.append(' ').append(vmArgs[i]);
						}
					}
					fVMArgs.setText(buffer.toString());
				}				
			}
		}
		setVMNameStatus(validateVMName());
		updateStatusLine();
	}
	
	private IVMInstallType getVMType() {
		return fSelectedVMType;
	}
	
	private IStatus validateJRELocation() {
		String locationName= fJRERoot.getText();
		IStatus s = null;
		File file = null;
		if (locationName.length() == 0) {//$NON-NLS-1$
			s = new StatusInfo(IStatus.INFO, JREMessages.addVMDialog_enterLocation); //$NON-NLS-1$
		} else {
			file= new File(locationName);
			if (!file.exists()) {
				s = new StatusInfo(IStatus.ERROR, JREMessages.addVMDialog_locationNotExists); //$NON-NLS-1$
			} else {
				final IStatus[] temp = new IStatus[1];
				final File tempFile = file; 
				Runnable r = new Runnable() {
					/**
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						temp[0] = getVMType().validateInstallLocation(tempFile);
					}
				};
				BusyIndicator.showWhile(getShell().getDisplay(), r);
				s = temp[0];
			}
		}
		if (s.isOK()) {
			fLibraryBlock.setHomeDirectory(file);
		} else {
			fLibraryBlock.setHomeDirectory(null);
		}
		fLibraryBlock.update();
		detectJavadocLocation();
		return s;
	}
	
	/**
	 * Auto-detects the default javadoc location
	 */
	private void detectJavadocLocation() {
		if (fAutoDetectJavadocLocation) {
			if (getVMType() instanceof AbstractVMInstallType) {
				AbstractVMInstallType type = (AbstractVMInstallType)getVMType();
				fJavadocLocation = type.getDefaultJavadocLocation(getInstallLocation());
			}
		} else {
			fJavadocLocation = fEditedVM.getJavadocLocation();
		}
	}

	private IStatus validateVMName() {
		StatusInfo status= new StatusInfo();
		String name= fVMName.getText();
		if (name == null || name.trim().length() == 0) {
			status.setInfo(JREMessages.addVMDialog_enterName); //$NON-NLS-1$
		} else {
			if (fRequestor.isDuplicateName(name) && (fEditedVM == null || !name.equals(fEditedVM.getName()))) {
				status.setError(JREMessages.addVMDialog_duplicateName); //$NON-NLS-1$
			} else {
				IStatus s = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
				if (!s.isOK()) {
					status.setError(MessageFormat.format(JREMessages.AddVMDialog_JRE_name_must_be_a_valid_file_name___0__1, new String[]{s.getMessage()})); //$NON-NLS-1$
				}
			}
		}
		return status;
	}
	
	protected void updateStatusLine() {
		IStatus max= null;
		for (int i= 0; i < fStati.length; i++) {
			IStatus curr= fStati[i];
			if (curr.matches(IStatus.ERROR)) {
				updateStatus(curr);
				return;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max= curr;
			}
		}
		updateStatus(max);
	}
			
	private void browseForInstallDir() {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		dialog.setFilterPath(fJRERoot.getText());
		dialog.setMessage(JREMessages.addVMDialog_pickJRERootDialog_message); //$NON-NLS-1$
		String newPath= dialog.open();
		if (newPath != null) {
			fJRERoot.setText(newPath);
		}
	}
	
	protected URL getURL() {
		return fJavadocLocation;
	}
	
	protected void okPressed() {
		doOkPressed();
		super.okPressed();
	}
	
	private void doOkPressed() {
		if (fEditedVM == null) {
			IVMInstall vm= new VMStandin(fSelectedVMType, createUniqueId(fSelectedVMType));
			setFieldValuesToVM(vm);
			fRequestor.vmAdded(vm);
		} else {
			setFieldValuesToVM(fEditedVM);
		}
	}
	
	private String createUniqueId(IVMInstallType vmType) {
		String id= null;
		do {
			id= String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null);
		return id;
	}
	
	protected void setFieldValuesToVM(IVMInstall vm) {
		vm.setInstallLocation(new File(fJRERoot.getText()).getAbsoluteFile());
		vm.setName(fVMName.getText());
		vm.setJavadocLocation(getURL());
		
		String argString = fVMArgs.getText().trim();
		if (vm instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) vm;
			if (argString != null && argString.length() >0) {
				vm2.setVMArgs(argString);			
			} else {
				vm2.setVMArgs(null);
			}
		} else {
			if (argString != null && argString.length() >0) {
				vm.setVMArguments(DebugPlugin.parseArguments(argString));			
			} else {
				vm.setVMArguments(null);
			}			
		}
		

		fLibraryBlock.performApply(vm);
	}
	
	protected File getAbsoluteFileOrEmpty(String path) {
		if (path == null || path.length() == 0) {
			return new File(""); //$NON-NLS-1$
		}
		return new File(path).getAbsoluteFile();
	}
	
	private void setVMNameStatus(IStatus status) {
		fStati[0]= status;
	}
	
	private void setJRELocationStatus(IStatus status) {
		fStati[1]= status;
	}
	
	protected IStatus getSystemLibraryStatus() {
		return fStati[3];
	}
	
	protected void setSystemLibraryStatus(IStatus status) {
		fStati[3]= status;
	}
	
	/**
	 * Updates the status of the ok button to reflect the given status.
	 * Subclasses may override this method to update additional buttons.
	 * @param status the status.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok != null && !ok.isDisposed())
			ok.setEnabled(status.getSeverity() == IStatus.OK);
	}	
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected void setButtonLayoutData(Button button) {
		super.setButtonLayoutData(button);
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return "ADD_VM_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
		return super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point initialLocation= DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
		if (initialLocation != null) {
			return initialLocation;
		}
		return super.getInitialLocation(initialSize);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
	}
}
