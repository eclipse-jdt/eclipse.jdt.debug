/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jdt.internal.debug.ui.StatusInfo;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

/**
 * Provides the Add VM dialog
 */
public class AddVMDialog extends StatusDialog {
	
	private IAddVMDialogRequestor fRequestor;
	private IVMInstall fEditedVM;
	private IVMInstallType[] fVMTypes;
	private IVMInstallType fSelectedVMType;
	private Combo fVMCombo;
	private Text fVMName;
	private Text fVMArgs;
	private Text fJRERoot;
	private VMLibraryBlock fLibraryBlock;
// the VM install's javadoc location
	private URL fJavadocLocation = null;
	private boolean fAutoDetectJavadocLocation = false;
	private IStatus[] fStatus;
	private int fPrevIndex = -1;
		
	/**
	 * Constructor
	 * @param requestor dialog validation requestor
	 * @param shell the parent shell 
	 * @param vmInstallTypes the types of VM installs
	 * @param editedVM the editedVM
	 */
	public AddVMDialog(IAddVMDialogRequestor requestor, Shell shell, IVMInstallType[] vmInstallTypes, IVMInstall editedVM) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fRequestor = requestor;
		fStatus = new IStatus[5];
		for (int i= 0; i < fStatus.length; i++) {
			fStatus[i] = new StatusInfo();
		}
		fVMTypes = vmInstallTypes;
		fSelectedVMType = editedVM != null ? editedVM.getVMInstallType() : vmInstallTypes[0];
		fEditedVM = editedVM;
	//only detect the javadoc location if not already set
		fAutoDetectJavadocLocation = fEditedVM == null || fEditedVM.getJavadocLocation() == null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaDebugHelpContextIds.EDIT_JRE_DIALOG);
	}		
	
	/**
	 * Returns the VM name from the text control
	 * @return
	 */
	protected String getVMName() {
		return fVMName.getText();
	}
		
	/**
	 * Returns the installation location as a file from the JRE root text control
	 * @return the installation location as a file
	 */
	protected File getInstallLocation() {
		return new File(fJRERoot.getText());
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite ancestor) {
		Composite parent = (Composite)super.createDialogArea(ancestor);
		((GridLayout)parent.getLayout()).numColumns = 3;
	//VM combo
		SWTUtil.createLabel(parent, JREMessages.addVMDialog_jreType, 1);
		fVMCombo = SWTUtil.createCombo(parent, SWT.READ_ONLY, 2, getVMTypeNames());
	//VM name
		SWTUtil.createLabel(parent, JREMessages.addVMDialog_jreName, 1);
		fVMName = SWTUtil.createSingleText(parent, 2);
	//VM root
		SWTUtil.createLabel(parent, JREMessages.addVMDialog_jreHome, 1);
		fJRERoot = SWTUtil.createSingleText(parent, 1);
		Button browse = SWTUtil.createPushButton(parent, JREMessages.addVMDialog_browse1, null);
	//VM args
		SWTUtil.createLabel(parent, JREMessages.AddVMDialog_23, 1);
		fVMArgs = SWTUtil.createSingleText(parent, 2);
	//VM libraries block 
		SWTUtil.createLabel(parent, JREMessages.AddVMDialog_JRE_system_libraries__1, 3);
		fLibraryBlock = new VMLibraryBlock(this);
		Control block = fLibraryBlock.createControl(parent);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		block.setLayoutData(gd);
	
	//init the fields
		initializeFields();
		
	//add the listeners now to prevent them from monkeying with initialized settings
		fVMCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				updateVMType();
			}
		});
		fVMName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateVMName();
				updateStatusLine();	
			}
		});
		fJRERoot.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateJRELocation();
				updateStatusLine();
			}
		});
		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setFilterPath(fJRERoot.getText());
				dialog.setMessage(JREMessages.addVMDialog_pickJRERootDialog_message); 
				String newPath = dialog.open();
				if (newPath != null) {
					fJRERoot.setText(newPath);
				}
			}
		});
		applyDialogFont(parent);
		return parent;
	}
	
	/**
	 * Updates the JRE location status and inits the library block
	 */
	private void updateVMType() {
		int selIndex = fVMCombo.getSelectionIndex();
		if (selIndex == fPrevIndex) {
			return;
		}
		fPrevIndex = selIndex;
		if (selIndex >= 0 && selIndex < fVMTypes.length) {
			fSelectedVMType= fVMTypes[selIndex];
		}
		validateJRELocation();
		fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
		updateStatusLine();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#create()
	 */
	public void create() {
		super.create();
		fVMName.setFocus();
		selectVMType();  
	}
	
	/**
	 * Returns the VM type names
	 * @return an array of strings with the names of the applicable VMs
	 */
	private String[] getVMTypeNames() {
		String[] names =  new String[fVMTypes.length];
		for (int i = 0; i < fVMTypes.length; i++) {
			names[i]= fVMTypes[i].getName();
		}
		return names;
	}
	
	/**
	 * Selects the corresponding VM for fSelectedVMType
	 */
	private void selectVMType() {
		for (int i= 0; i < fVMTypes.length; i++) {
			if (fSelectedVMType == fVMTypes[i]) {
				if(i < fVMCombo.getItemCount()) {
					fVMCombo.select(i);
					return;
				}
			}
		}
	}
	
	/**
	 * Initialize the dialogs fields
	 */
	private void initializeFields() {
		if (fEditedVM == null) {
			fVMName.setText(""); //$NON-NLS-1$
			fJRERoot.setText(""); //$NON-NLS-1$
			fLibraryBlock.initializeFrom(null, fSelectedVMType);
			fVMArgs.setText(""); //$NON-NLS-1$
		} else {
			fVMCombo.setEnabled(false);
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
		validateVMName();
		updateStatusLine();
	}
	
	/**
	 * Validates the JRE location
	 * @return the status after validating the JRE location
	 */
	private IStatus validateJRELocation() {
		String locationName = fJRERoot.getText();
		IStatus s = null;
		File file = null;
		if (locationName.length() == 0) {
			s = new StatusInfo(IStatus.INFO, JREMessages.addVMDialog_enterLocation); 
		} 
		else {
			file = new File(locationName);
			if (!file.exists()) {
				s = new StatusInfo(IStatus.ERROR, JREMessages.addVMDialog_locationNotExists); 
			} 
			else {
				final IStatus[] temp = new IStatus[1];
				final File tempFile = file; 
				Runnable r = new Runnable() {
					public void run() {
						temp[0] = fSelectedVMType.validateInstallLocation(tempFile);
					}
				};
				BusyIndicator.showWhile(getShell().getDisplay(), r);
				s = temp[0];
			}
		}
		if (s.isOK()) {
			fLibraryBlock.setHomeDirectory(file);
			String name = fVMName.getText();
			if (name == null || name.trim().length() == 0) {
				// auto-generate VM name
				try {
					String genName = null;
					IPath path = new Path(file.getCanonicalPath());
					int segs = path.segmentCount();
					if (segs == 1) {
						genName = path.segment(0);
					} 
					else if (segs >= 2) {
						String last = path.lastSegment();
						if ("jre".equalsIgnoreCase(last)) { //$NON-NLS-1$
							genName = path.segment(segs - 2);
						} 
						else {
							genName = last;
						}
					}
					if (genName != null) {
						fVMName.setText(genName);
					}
				} catch (IOException e) {}
			}
		} else {
			fLibraryBlock.setHomeDirectory(null);
		}
		fLibraryBlock.restoreDefaultLibraries();
		detectJavadocLocation();
		fStatus[1] = s;
		return s;
	}
	
	/**
	 * Auto-detects the default javadoc location
	 */
	private void detectJavadocLocation() {
		if (fAutoDetectJavadocLocation) {
			if (fSelectedVMType instanceof AbstractVMInstallType) {
				AbstractVMInstallType type = (AbstractVMInstallType)fSelectedVMType;
				fJavadocLocation = type.getDefaultJavadocLocation(getInstallLocation());
			}
		} else {
			fJavadocLocation = fEditedVM.getJavadocLocation();
		}
	}

	/**
	 * Validates the entered name of the VM
	 * @return the status of the name validation
	 */
	private IStatus validateVMName() {
		StatusInfo status= new StatusInfo();
		String name= fVMName.getText();
		if (name == null || name.trim().length() == 0) {
			status.setInfo(JREMessages.addVMDialog_enterName); 
		} else {
			if (fRequestor.isDuplicateName(name) && (fEditedVM == null || !name.equals(fEditedVM.getName()))) {
				status.setError(JREMessages.addVMDialog_duplicateName); 
			} else {
				IStatus s = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
				if (!s.isOK()) {
					status.setError(MessageFormat.format(JREMessages.AddVMDialog_JRE_name_must_be_a_valid_file_name___0__1, new String[]{s.getMessage()})); 
				}
			}
		}
		fStatus[0] = status;
		return status;
	}
	
	/**
	 * Updates the status line to show/hide messages to the user 
	 */
	protected void updateStatusLine() {
		IStatus max= null;
		for (int i = 0; i < fStatus.length; i++) {
			IStatus curr = fStatus[i];
			if (curr.matches(IStatus.ERROR)) {
				updateStatus(curr);
				return;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max = curr;
			}
		}
		updateStatus(max);
	}
	
	/**
	 * Returns the URL for the javadoc location
	 * @return the URL for the javadoc location
	 */
	protected URL getURL() {
		return fJavadocLocation;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (fEditedVM == null) {
			IVMInstall vm = new VMStandin(fSelectedVMType, createUniqueId(fSelectedVMType));
			setFieldValuesToVM(vm);
			fRequestor.vmAdded(vm);
		} else {
			setFieldValuesToVM(fEditedVM);
		}
		super.okPressed();
	}
	
	/**
	 * Creates a unique name for the VMInstallType
	 * @param vmType the vm install type
	 * @return a unique name
	 */
	private String createUniqueId(IVMInstallType vmType) {
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null);
		return id;
	}
	
	/**
	 * init fields to the specified VM
	 * @param vm the VM to init from
	 */
	protected void setFieldValuesToVM(IVMInstall vm) {
		File dir = new File(fJRERoot.getText());
		try {
			vm.setInstallLocation(dir.getCanonicalFile());
		} 
		catch (IOException e) {
			vm.setInstallLocation(dir.getAbsoluteFile());
		}
		vm.setName(fVMName.getText());
		vm.setJavadocLocation(getURL());
		
		String argString = fVMArgs.getText().trim();
		if (vm instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) vm;
			if (argString != null && argString.length() > 0) {
				vm2.setVMArgs(argString);			
			} 
			else {
				vm2.setVMArgs(null);
			}
		} 
		else {
			if (argString != null && argString.length() > 0) {
				vm.setVMArguments(DebugPlugin.parseArguments(argString));			
			} 
			else {
				vm.setVMArguments(null);
			}			
		}
		fLibraryBlock.performApply(vm);
	}
	
	/**
	 * returns an absolute file or an empty file if the path is either null or zero length
	 * @param path the path to the file
	 * @return a new file
	 */
	protected File getAbsoluteFileOrEmpty(String path) {
		if (path == null || path.length() == 0) {
			return new File(""); //$NON-NLS-1$
		}
		return new File(path).getAbsoluteFile();
	}
	
	/**
	 * @return the status of the system library 
	 */
	protected IStatus getSystemLibraryStatus() {
		return fStatus[3];
	}
	
	/**
	 * Allows the VM page to set the status of the current system library
	 * @param status the specified status
	 */
	protected void setSystemLibraryStatus(IStatus status) {
		fStatus[3] = status;
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
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return "ADD_VM_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         } 
         return section;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogBoundsSettings();
		if(settings != null) {
			try {
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if(width > 0 & height > 0) {
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe) {
				return new Point(500, 500);
			}
		}
		return new Point(500, 500);
	}

	protected void setButtonLayoutData(Button button) {
		super.setButtonLayoutData(button);
	}
    
}
