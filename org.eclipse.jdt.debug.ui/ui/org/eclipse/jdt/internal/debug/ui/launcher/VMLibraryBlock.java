package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AttachSourceAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
 
/**
 * Control used to edit the libraries associated with a VM install
 */
public class VMLibraryBlock implements IEntriesChangedListener {
	
	protected IVMInstall fVmInstall;
	protected IVMInstallType fVmInstallType;
	protected File fHome;
	// used to make buttons all the same width
	protected Button fAddJarButton;
	
	protected RuntimeClasspathViewer fPathViewer;
	protected Button fDefaultButton;
	protected List fActions = new ArrayList(10);
	
	protected AddVMDialog fDialog = null;
	protected boolean fInCallback = false;
	
	/**
	 * Constructor for VMLibraryBlock.
	 */
	public VMLibraryBlock(AddVMDialog dialog) {
		fDialog = dialog;
	}

	/**
	 * Creates and returns the source lookup control.
	 * 
	 * @param parent the parent widget of this control
	 */
	public Control createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		fPathViewer = new RuntimeClasspathViewer(comp);
		gd = new GridData(GridData.FILL_BOTH);
		fPathViewer.getControl().setLayoutData(gd);
		fPathViewer.addEntriesChangedListener(this);

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);

		createVerticalSpacer(comp, 2);
						
		fDefaultButton = new Button(comp, SWT.CHECK);
		fDefaultButton.setText(LauncherMessages.getString("VMLibraryBlock.Use_default_system_libraries_1")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleDefaultButtonSelected();
			}
		});
		
		RuntimeClasspathAction action = new MoveUpAction(null);								
		Button button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);
		
		action = new MoveDownAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		

		action = new RemoveAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		
		
		action = new AddExternalJarAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		
		fAddJarButton = button;
		
		action = new AttachSourceAction(null);								
		button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		addAction(action);		
		fAddJarButton = button;		
														
		retargetActions(fPathViewer);
				
		return comp;
	}

	/**
	 * The "default" button has been toggled
	 */
	protected void handleDefaultButtonSelected() {
		update();
	}

	/**
	 * Creates and returns a button 
	 * 
	 * @param parent parent widget
	 * @param label label
	 * @param image image
	 * @return Button
	 */
	protected Button createPushButton(
		Composite parent,
		String label,
		Image image) {
		return SWTUtil.createPushButton(parent, label, image);
	}
	
	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}	

	/**
	 * Adds the given action to the action collection in this tab
	 */
	protected void addAction(RuntimeClasspathAction action) {
		fActions.add(action);
	}
	
	/**
	 * Re-targets actions to the given viewer
	 */
	protected void retargetActions(RuntimeClasspathViewer viewer) {
		Iterator actions = fActions.iterator();
		while (actions.hasNext()) {
			RuntimeClasspathAction action = (RuntimeClasspathAction)actions.next();
			action.setViewer(viewer);
		}
	}
	
	/**
	 * Initializes this control based on the settings in the given
	 * vm install and type.
	 * 
	 * @param vm vm or <code>null</code> if none
	 * @param type type of vm install
	 */
	public void initializeFrom(IVMInstall vm, IVMInstallType type) {
		setVMInstall(vm);
		setVMInstallType(type);
		if (vm != null) {
			setHomeDirectory(vm.getInstallLocation());	
		}
		fDefaultButton.setSelection(vm == null || vm.getLibraryLocations() == null);
		if (isDefaultSystemLibrary()) {
			update();
		} else {
			LibraryLocation[] libs = vm.getLibraryLocations();
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[libs.length];
			for (int i = 0; i < libs.length; i++) {
				entries[i] = JavaRuntime.newArchiveRuntimeClasspathEntry(libs[i].getSystemLibraryPath());
				entries[i].setSourceAttachmentPath(libs[i].getSystemLibrarySourcePath());
				entries[i].setSourceAttachmentRootPath(libs[i].getPackageRootPath());
			}
			fPathViewer.setEntries(entries);
			fPathViewer.setEnabled(true);						
		}
	}
	
	/**
	 * Sets the home directory of the VM Install the user has chosen
	 */
	public void setHomeDirectory(File file) {
		fHome = file;
	}
	
	/**
	 * Returns the home directory
	 */
	protected File getHomeDirectory() {
		return fHome;
	}
	
	/**
	 * Updates libraries based on settings
	 */
	public void update() {
		boolean useDefault = fDefaultButton.getSelection();
		LibraryLocation[] libs = null;
		if (useDefault) {
			if (getHomeDirectory() == null) {
				libs = new LibraryLocation[0];
			} else {
				libs = getVMInstallType().getDefaultLibraryLocations(getHomeDirectory());
			}
			IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[libs.length];
			for (int i = 0; i < libs.length; i++) {
				entries[i] = JavaRuntime.newArchiveRuntimeClasspathEntry(libs[i].getSystemLibraryPath());
				entries[i].setSourceAttachmentPath(libs[i].getSystemLibrarySourcePath());
				entries[i].setSourceAttachmentRootPath(libs[i].getPackageRootPath());
			}
			fPathViewer.setEntries(entries);
		}
		fPathViewer.setEnabled(!useDefault);		
		IStatus status = null;
		if (getEntries().length == 0 && !isDefaultSystemLibrary()) {
			status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				LauncherMessages.getString("VMLibraryBlock.Libraries_cannot_be_empty._1"), null); //$NON-NLS-1$
		} else {
			status = new StatusInfo();
		}		
		fDialog.setSystemLibraryStatus(status);
		fDialog.updateStatusLine();
	}
	
	/**
	 * Saves settings in the given working copy
	 */
	public void performApply(IVMInstall vm) {
		boolean def = fDefaultButton.getSelection();		
		if (def) {
			vm.setLibraryLocations(null);
		} else {
			IRuntimeClasspathEntry[] entries = fPathViewer.getEntries();
			LibraryLocation[] libs = new LibraryLocation[entries.length];
			for (int i = 0; i < entries.length; i++) {
				IPath lib = entries[i].getPath();
				IPath src = entries[i].getSourceAttachmentPath();
				if (src == null) {
					src = Path.EMPTY;
				}
				IPath root = entries[i].getSourceAttachmentRootPath();
				if (root == null) {
					root = Path.EMPTY;
				}
				libs[i] = new LibraryLocation(lib, src, root);
			}
			vm.setLibraryLocations(libs);
		}		
	}	
	
	/**
	 * Returns the entries visible in the viewer
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		return fPathViewer.getEntries();
	}	

	/**
	 * Sets the vm install associated with this library block.
	 * 
	 * @param vm vm install
	 */
	private void setVMInstall(IVMInstall vm) {
		fVmInstall = vm;
	}
	
	/**
	 * Returns the vm install associated with this library block.
	 * 
	 * @return vm install
	 */
	protected IVMInstall getVMInstall() {
		return fVmInstall;
	}	
	
	/**
	 * Returns whether the default system libraries are to be used
	 */
	public boolean isDefaultSystemLibrary() {
		return fDefaultButton.getSelection();
	}

	/**
	 * Sets the vm install type associated with this library block.
	 * 
	 * @param type vm install type
	 */
	private void setVMInstallType(IVMInstallType type) {
		fVmInstallType = type;
	}
	
	/**
	 * Returns the vm install type associated with this library block.
	 * 
	 * @return vm install
	 */
	protected IVMInstallType getVMInstallType() {
		return fVmInstallType;
	}	
	/**
	 * @see IEntriesChangedListener#entriesChanged(RuntimeClasspathViewer)
	 */
	public void entriesChanged(RuntimeClasspathViewer viewer) {
		if (!fInCallback) {
			fInCallback = true;
			update();
			fInCallback = false;
		}
	}

}
