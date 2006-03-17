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
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.jres.LibraryContentProvider.SubElement;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
 
/**
 * Control used to edit the libraries associated with a VM install
 */
public class VMLibraryBlock implements SelectionListener, ISelectionChangedListener {
	
	/**
	 * Attribute name for the last path used to open a file/directory chooser
	 * dialog.
	 */
	protected static final String LAST_PATH_SETTING = "LAST_PATH_SETTING"; //$NON-NLS-1$
	
	protected IVMInstall fVmInstall;
	protected IVMInstallType fVmInstallType;
	protected File fHome;
	
	protected TreeViewer fLibraryViewer;
	protected LibraryContentProvider fLibraryContentProvider;
	protected Button fDefaultButton;
	
	protected AddVMDialog fDialog = null;
	protected boolean fInCallback = false;
	
	protected static final String DIALOG_SETTINGS_PREFIX = "VMLibraryBlock"; //$NON-NLS-1$
	private Button fUpButton;
	private Button fDownButton;
	private Button fRemoveButton;
	private Button fAddButton;
	private Button fEditButton;
	
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
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		comp.setLayout(topLayout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		fLibraryViewer= new TreeViewer(comp);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 6;
		fLibraryViewer.getControl().setLayoutData(gd);
		fLibraryContentProvider= new LibraryContentProvider();
		fLibraryViewer.setContentProvider(fLibraryContentProvider);
		fLibraryViewer.setLabelProvider(new LibraryLabelProvider());
		fLibraryViewer.setInput(this);
		fLibraryViewer.addSelectionChangedListener(this);
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);
		
		fAddButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_7);
		fAddButton.addSelectionListener(this);
		
		fEditButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_8);
		fEditButton.addSelectionListener(this);
		fLibraryViewer.addDoubleClickListener(new IDoubleClickListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				if (fEditButton.isEnabled()) {
					edit((IStructuredSelection) fLibraryViewer.getSelection());
				}
			}
		});

		fRemoveButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_6);
		fRemoveButton.addSelectionListener(this);
		
		fUpButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_4);
		fUpButton.addSelectionListener(this);
		
		fDownButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_5);
		fDownButton.addSelectionListener(this);

		fDefaultButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_9);
		fDefaultButton.addSelectionListener(this);
		
		return comp;
	}

	/**
	 * The "default" button has been toggled
	 */
	public void restoreDefaultLibraries() {
		LibraryLocation[] libs = null;
		File installLocation = getHomeDirectory();
		if (installLocation == null) {
			libs = new LibraryLocation[0];
		} else {
			libs = getVMInstallType().getDefaultLibraryLocations(installLocation);
		}
		fLibraryContentProvider.setLibraries(libs);
		update();
	}

	/**
	 * Creates and returns a button 
	 * 
	 * @param parent parent widget
	 * @param label label
	 * @return Button
	 */
	protected Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		fDialog.setButtonLayoutData(button);
		return button;	
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
			fLibraryContentProvider.setLibraries(JavaRuntime.getLibraryLocations(getVMInstall()));
		}
		update();
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
	 * Updates buttons and status based on current libraries
	 */
	public void update() {
		updateButtons();
		IStatus status = new StatusInfo();
		if (fLibraryContentProvider.getLibraries().length == 0) { // && !isDefaultSystemLibrary()) {
			status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				JREMessages.VMLibraryBlock_Libraries_cannot_be_empty__1, null);
		}		
		fDialog.setSystemLibraryStatus(status);
		fDialog.updateStatusLine();
	}
	
	/**
	 * Saves settings in the given working copy
	 */
	public void performApply(IVMInstall vm) {		
		if (isDefaultLocations()) {
			vm.setLibraryLocations(null);
		} else {
			LibraryLocation[] libs = fLibraryContentProvider.getLibraries();
			vm.setLibraryLocations(libs);
		}		
	}	
	
	protected boolean isDefaultLocations() {
		LibraryLocation[] libraryLocations = fLibraryContentProvider.getLibraries();
        IVMInstall install = getVMInstall();
        
		if (install == null || libraryLocations == null) {
			return true;
		}
		File installLocation = install.getInstallLocation();
		if (installLocation != null) {
			LibraryLocation[] def = getVMInstallType().getDefaultLibraryLocations(installLocation);
			if (def.length == libraryLocations.length) {
				for (int i = 0; i < def.length; i++) {
					if (!def[i].equals(libraryLocations[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
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

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Object source= e.getSource();
		if (source == fUpButton) {
			fLibraryContentProvider.up((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fDownButton) {
			fLibraryContentProvider.down((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fRemoveButton) {
			fLibraryContentProvider.remove((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fAddButton) {
			add((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fEditButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fDefaultButton) {
			restoreDefaultLibraries();
		}
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Open the file selection dialog, and add the return jars as libraries.
	 */
	private void add(IStructuredSelection selection) {
		IDialogSettings dialogSettings= JDIDebugUIPlugin.getDefault().getDialogSettings();
		String lastUsedPath= dialogSettings.get(LAST_PATH_SETTING);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog= new FileDialog(fLibraryViewer.getControl().getShell(), SWT.MULTI);
		dialog.setText(JREMessages.VMLibraryBlock_10);
		dialog.setFilterExtensions(new String[] {"*.jar;*.zip"}); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res == null) {
			return;
		}
		String[] fileNames= dialog.getFileNames();
		int nChosen= fileNames.length;
			
		IPath filterPath= new Path(dialog.getFilterPath());
		LibraryLocation[] libs= new LibraryLocation[nChosen];
		for (int i= 0; i < nChosen; i++) {
			libs[i]= new LibraryLocation(filterPath.append(fileNames[i]).makeAbsolute(), Path.EMPTY, Path.EMPTY);
		}
		dialogSettings.put(LAST_PATH_SETTING, filterPath.toOSString());
		
		fLibraryContentProvider.add(libs, selection);
	}

	/**
	 * Open the javadoc location dialog or the source location dialog, and set the result
	 * to the selected libraries.
	 */
	private void edit(IStructuredSelection selection) {
		SubElement firstElement= (SubElement)selection.getFirstElement();
		LibraryLocation library= firstElement.getParent().toLibraryLocation();
		if (firstElement.getType() == SubElement.JAVADOC_URL) {
			URL[] urls= BuildPathDialogAccess.configureJavadocLocation(fLibraryViewer.getControl().getShell(), library.getSystemLibraryPath().toOSString(), library.getJavadocLocation());
			if (urls != null) {
				fLibraryContentProvider.setJavadoc(urls[0], selection);
			}
		} else {
			IRuntimeClasspathEntry entry= JavaRuntime.newArchiveRuntimeClasspathEntry(library.getSystemLibraryPath());
			entry.setSourceAttachmentPath(library.getSystemLibrarySourcePath());
			entry.setSourceAttachmentRootPath(library.getPackageRootPath());
			IClasspathEntry classpathEntry = BuildPathDialogAccess.configureSourceAttachment(fLibraryViewer.getControl().getShell(), entry.getClasspathEntry()); 
			if (classpathEntry != null) {
				fLibraryContentProvider.setSourcePath(classpathEntry.getSourceAttachmentPath(), classpathEntry.getSourceAttachmentRootPath(), selection);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

	/**
	 * Refresh the enable/disable state for the buttons.
	 */
	private void updateButtons() {
		IStructuredSelection selection= (IStructuredSelection) fLibraryViewer.getSelection();
		fAddButton.setEnabled(true);
		fRemoveButton.setEnabled(!selection.isEmpty());
		boolean enableUp= true;
		boolean enableDown= true;
		boolean allSource= true;
		boolean allJavadoc= true;
		Object[] libraries= fLibraryContentProvider.getElements(null);
		if (selection.isEmpty() || libraries.length == 0) {
			enableUp= enableDown= false;
		} else {
			Object first= libraries[0];
			Object last= libraries[libraries.length - 1];
			for (Iterator iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				Object lib;
				if (element instanceof SubElement) {
					SubElement subElement= (SubElement)element;
					lib= (subElement).getParent().toLibraryLocation();
					if (subElement.getType() == SubElement.JAVADOC_URL) {
						allSource= false;
					} else {
						allJavadoc= false;
					}
				} else {
					lib= element;
					allSource= allJavadoc= false;
				}
				if (lib == first) {
					enableUp= false;
				}
				if (lib == last) {
					enableDown= false;
				}
			}
		}
		fUpButton.setEnabled(enableUp);
		fDownButton.setEnabled(enableDown);
		fEditButton.setEnabled(!selection.isEmpty() && (allSource || allJavadoc));
	}
}
