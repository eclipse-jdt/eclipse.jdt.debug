package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;

public class AddVMDialog extends StatusDialog {
	private static final String JAVA_LANG_OBJECT= "java/lang/Object.java"; //$NON-NLS-1$

	private IAddVMDialogRequestor fRequestor;
	
	private IVMInstall fEditedVM;

	private IVMInstallType[] fVMTypes;
	private IVMInstallType fSelectedVMType;
	
	private ComboDialogField fVMTypeCombo;
	private VMLibraryBlock fLibraryBlock;
	
	private StringButtonDialogField fJRERoot;
	private StringDialogField fVMName;

	private StringButtonDialogField fJavadocURL;
	
	private IDialogSettings fDialogSettings;
	
	private IStatus[] fStati;
		
	public AddVMDialog(IAddVMDialogRequestor requestor, Shell shell, IVMInstallType[] vmInstallTypes, IVMInstall editedVM) {
		super(shell);
		fRequestor= requestor;
		fStati= new IStatus[5];
		for (int i= 0; i < fStati.length; i++) {
			fStati[i]= new StatusInfo();
		}
		
		fVMTypes= vmInstallTypes;
		fSelectedVMType= editedVM != null ? editedVM.getVMInstallType() : vmInstallTypes[0];
		
		fEditedVM= editedVM;
		
		fDialogSettings= JDIDebugUIPlugin.getDefault().getDialogSettings();
	}
	
	/**
	 * @see Windows#configureShell
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		WorkbenchHelp.setHelp(newShell, IJavaDebugHelpContextIds.EDIT_JRE_DIALOG);
	}		
	
	protected void createDialogFields() {
		fVMTypeCombo= new ComboDialogField(SWT.READ_ONLY);
		fVMTypeCombo.setLabelText(LauncherMessages.getString("addVMDialog.jreType")); //$NON-NLS-1$
		fVMTypeCombo.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				updateVMType();
			}
		});
		
		fVMName= new StringDialogField();
		fVMName.setLabelText(LauncherMessages.getString("addVMDialog.jreName")); //$NON-NLS-1$
		fVMName.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setVMNameStatus(validateVMName());
				updateStatusLine();
			}
		});
		
		fJRERoot= new StringButtonDialogField(new IStringButtonAdapter() {
			public void changeControlPressed(DialogField field) {
				browseForInstallDir();
			}
		});
		fJRERoot.setLabelText(LauncherMessages.getString("addVMDialog.jreHome")); //$NON-NLS-1$
		fJRERoot.setButtonLabel(LauncherMessages.getString("addVMDialog.browse1")); //$NON-NLS-1$
		fJRERoot.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setJRELocationStatus(validateJRELocation());
				updateStatusLine();
			}
		});
	
		fJavadocURL = new StringButtonDialogField(new IStringButtonAdapter() {
			public void changeControlPressed(DialogField field) {
				browseForJavadocURL();
			}
		});
		fJavadocURL.setLabelText(LauncherMessages.getString("AddVMDialog.Java&doc_URL__1")); //$NON-NLS-1$
		fJavadocURL.setButtonLabel(LauncherMessages.getString("AddVMDialog.Bro&wse..._2")); //$NON-NLS-1$
	}
	
	protected String getVMName() {
		return fVMName.getText();
	}
		
	protected File getInstallLocation() {
		return new File(fJRERoot.getText());
	}
		
	protected Control createDialogArea(Composite ancestor) {
		createDialogFields();
		Composite parent= new Composite(ancestor, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		parent.setLayout(layout);
		
		fVMTypeCombo.doFillIntoGrid(parent, 3);
		((GridData)fVMTypeCombo.getComboControl(null).getLayoutData()).widthHint= convertWidthInCharsToPixels(50);

		fVMName.doFillIntoGrid(parent, 3);
	
		fJRERoot.doFillIntoGrid(parent, 3);
		
		fJavadocURL.doFillIntoGrid(parent, 3);
		
		Label l = new Label(parent, SWT.NONE);
		l.setText(LauncherMessages.getString("AddVMDialog.JRE_system_libraries__1")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		l.setLayoutData(gd);		
		
		fLibraryBlock = new VMLibraryBlock();
		Control block = fLibraryBlock.createControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		block.setLayoutData(gd);
		
		Text t= fJRERoot.getTextControl(parent);
		gd= (GridData)t.getLayoutData();
		gd.grabExcessHorizontalSpace=true;
		gd.widthHint= convertWidthInCharsToPixels(50);
		
		t= fJavadocURL.getTextControl(parent);
		gd= (GridData)t.getLayoutData();
		gd.grabExcessHorizontalSpace=true;
		gd.widthHint= convertWidthInCharsToPixels(50);
		
		initializeFields();
		
		return parent;
	}
	
	private void updateVMType() {
		int selIndex= fVMTypeCombo.getSelectionIndex();
		if (selIndex >= 0 && selIndex < fVMTypes.length) {
			fSelectedVMType= fVMTypes[selIndex];
		}
		setJRELocationStatus(validateJRELocation());
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
			fJavadocURL.setText(""); //$NON-NLS-1$
			fLibraryBlock.initializeFrom(null, fSelectedVMType);
		} else {
			fVMTypeCombo.setEnabled(false);
			fVMName.setText(fEditedVM.getName());
			fJRERoot.setText(fEditedVM.getInstallLocation().getAbsolutePath());
			URL url = fEditedVM.getJavadocLocation();
			if (url == null) {
				fJavadocURL.setText(""); //$NON-NLS-1$
			} else {
				fJavadocURL.setText(url.toExternalForm());
			}
			fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
		}
	}
	
	private IVMInstallType getVMType() {
		return fSelectedVMType;
	}
	
	private IStatus validateJRELocation() {
		String locationName= fJRERoot.getText();
		IStatus s = null;
		File file = null;
		if (locationName.length() == 0) {//$NON-NLS-1$
			s = new StatusInfo(IStatus.INFO, LauncherMessages.getString("addVMDialog.enterLocation")); //$NON-NLS-1$
		} else {
			file= new File(locationName);
			if (!file.exists()) {
				s = new StatusInfo(IStatus.ERROR, LauncherMessages.getString("addVMDialog.locationNotExists")); //$NON-NLS-1$
			} else {
				s = getVMType().validateInstallLocation(file);
			}
		}
		if (s.isOK()) {
			fLibraryBlock.setHomeDirectory(file);
		} else {
			fLibraryBlock.setHomeDirectory(null);
		}
		fLibraryBlock.update();
		return s;
	}

	private IStatus validateVMName() {
		StatusInfo status= new StatusInfo();
		String name= fVMName.getText();
		if (name == null || name.trim().length() == 0) {
			status.setInfo(LauncherMessages.getString("addVMDialog.enterName")); //$NON-NLS-1$
		} else {
			IVMInstallType type= getVMType();
			if (fRequestor.isDuplicateName(type, name) && (fEditedVM == null || !name.equals(fEditedVM.getName()))) {
				status.setError(LauncherMessages.getString("addVMDialog.duplicateName")); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	private void updateStatusLine() {
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
			
	/**
	 * try finding the package prefix
	 */
	private IPath determinePackagePrefix(IPath sourceJar) {
		if (sourceJar.isEmpty() || !sourceJar.toFile().isFile()) {
			return null;
		}
		ZipFile zip= null;
		try {
			zip= new ZipFile(sourceJar.toFile());
			Enumeration zipEntries= zip.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry= (ZipEntry) zipEntries.nextElement();
				String name= entry.getName();
				if (name.endsWith(JAVA_LANG_OBJECT)) {
					String prefix= name.substring(0, name.length() - JAVA_LANG_OBJECT.length());
					if (prefix.endsWith("/")) { //$NON-NLS-1$
						prefix= prefix.substring(0, prefix.length() - 1);
					}
					return new Path(prefix);
				}
			}
		} catch (IOException e) {
			JDIDebugUIPlugin.log(e);
		} finally {
			if (zip != null) {
				try { 
					zip.close();
				} catch (IOException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	private void browseForInstallDir() {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		dialog.setFilterPath(fJRERoot.getText());
		dialog.setMessage(LauncherMessages.getString("addVMDialog.pickJRERootDialog.message")); //$NON-NLS-1$
		String newPath= dialog.open();
		if (newPath != null) {
			fJRERoot.setText(newPath);
		}
	}
	
	private void browseForJavadocURL() {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		
		String initPath= ""; //$NON-NLS-1$
		URL url = getURL();
		if (url != null && "file".equals(url.getProtocol())) { //$NON-NLS-1$
			initPath= (new File(url.getFile())).getPath();
		}

		dialog.setFilterPath(initPath);
		dialog.setMessage(LauncherMessages.getString("AddVMDialog.Select_Javadoc_location__3")); //$NON-NLS-1$
		String res = dialog.open();
		if (res != null) {
			try {
				url = (new File(res)).toURL();
				fJavadocURL.setText(url.toExternalForm());
			} catch (MalformedURLException e) {
				// should not happen
				JDIDebugUIPlugin.log(e);
			}
		}
	}	
	
	protected URL getURL() {
		try {
			return new URL(fJavadocURL.getText());
		} catch (MalformedURLException e) {
			return null;
		}
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
		fLibraryBlock.performApply(vm);
	}
	
	protected File getAbsoluteFileOrEmpty(String path) {
		if (path == null || path.length() == 0) {
			return new File(""); //$NON-NLS-1$
		}
		return new File(path).getAbsoluteFile();
	}
	
	private IStatus getVMNameStatus() {
		return fStati[0];
	}
	
	private void setVMNameStatus(IStatus status) {
		fStati[0]= status;
	}
	
	private IStatus getJRELocationStatus() {
		return fStati[1];
	}
	
	private void setJRELocationStatus(IStatus status) {
		fStati[1]= status;
	}
	
	private IStatus getDebuggerTimeoutStatus() {
		return fStati[2];
	}
	
	private void setDebuggerTimeoutStatus(IStatus status) {
		fStati[2]= status;
	}
	
	private IStatus getSystemLibraryStatus() {
		return fStati[3];
	}
	
	private void setSystemLibraryStatus(IStatus status) {
		fStati[3]= status;
	}
	
	private IStatus getSystemLibrarySourceStatus() {
		return fStati[4];
	}
	
	private void setSystemLibrarySourceStatus(IStatus status) {
		fStati[4]= status;
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
	
}
