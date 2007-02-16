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
import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.jres.LibraryContentProvider.SubElement;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
 
/**
 * Control used to edit the libraries associated with a VM install
 */
public class VMLibraryBlock implements SelectionListener, ISelectionChangedListener {
	
	/**
	 * Attribute name for the last path used to open a file/directory chooser
	 * dialog.
	 */
	protected static final String LAST_PATH_SETTING = "LAST_PATH_SETTING"; //$NON-NLS-1$
	
	/**
	 * the prefix for dialog setting pertaining to this block
	 */
	protected static final String DIALOG_SETTINGS_PREFIX = "VMLibraryBlock"; //$NON-NLS-1$
	
	protected boolean fInCallback = false;
	protected IVMInstall fVmInstall;
	protected IVMInstallType fVmInstallType;
	protected File fHome;
	
	//widgets
	protected LibraryContentProvider fLibraryContentProvider;
	protected AddVMDialog fDialog = null;
	protected TreeViewer fLibraryViewer;
	private Button fUpButton;
	private Button fDownButton;
	private Button fRemoveButton;
	private Button fAddButton;
	private Button fJavadocButton;
	private Button fSourceButton;
	protected Button fDefaultButton;
	
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
		
		Composite comp = SWTFactory.createComposite(parent, font, 2, 1, GridData.FILL_BOTH, 0, 0);
		
		fLibraryViewer= new TreeViewer(comp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 6;
		fLibraryViewer.getControl().setLayoutData(gd);
		fLibraryContentProvider= new LibraryContentProvider();
		fLibraryViewer.setContentProvider(fLibraryContentProvider);
		fLibraryViewer.setLabelProvider(new LibraryLabelProvider());
		fLibraryViewer.setInput(this);
		fLibraryViewer.addSelectionChangedListener(this);
		
		Composite pathButtonComp = SWTFactory.createComposite(comp, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL, 0, 0); 
		fAddButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_7, JREMessages.VMLibraryBlock_16, null);
		fAddButton.addSelectionListener(this);
		fJavadocButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_3, JREMessages.VMLibraryBlock_17, null); 
		fJavadocButton.addSelectionListener(this);
		fSourceButton =  SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_11, JREMessages.VMLibraryBlock_18, null);	
		fSourceButton.addSelectionListener(this);
		fLibraryViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getViewer().getSelection();
				Object obj = sel.getFirstElement();
				if(obj instanceof SubElement) {
					edit(sel, ((SubElement)obj).getType());
				}
			}
		});
		fRemoveButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_6, JREMessages.VMLibraryBlock_12, null);		
		fRemoveButton.addSelectionListener(this);
		fUpButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_4, JREMessages.VMLibraryBlock_13, null);
		fUpButton.addSelectionListener(this);
		fDownButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_5, JREMessages.VMLibraryBlock_14, null);
		fDownButton.addSelectionListener(this);
		fDefaultButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_9, JREMessages.VMLibraryBlock_15, null);
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
	 * Initializes this control based on the settings in the given
	 * vm install and type.
	 * 
	 * @param vm vm or <code>null</code> if none
	 * @param type type of vm install
	 */
	public void initializeFrom(IVMInstall vm, IVMInstallType type) {
		fVmInstall = vm;
		fVmInstallType = type;
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
		IStatus status = Status.OK_STATUS;
		if (fLibraryContentProvider.getLibraries().length == 0) { // && !isDefaultSystemLibrary()) {
			status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				JREMessages.VMLibraryBlock_Libraries_cannot_be_empty__1, null);
		}
		LibraryStandin[] standins = fLibraryContentProvider.getStandins();
		for (int i = 0; i < standins.length; i++) {
			IStatus st = standins[i].validate();
			if (!st.isOK()) {
				status = st;
				break;
			}
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
	
	/**
	 * Determines if the present setup is the default location s for this JRE
	 * @return true if the current set of locations are the defaults, false otherwise
	 */
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
	 * Returns the vm install associated with this library block.
	 * 
	 * @return vm install
	 */
	protected IVMInstall getVMInstall() {
		return fVmInstall;
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
		} 
		else if(source == fJavadocButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection(), SubElement.JAVADOC_URL);
		}
		else if(source == fSourceButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection(), SubElement.SOURCE_PATH);
		}
		else if (source == fDefaultButton) {
			restoreDefaultLibraries();
		}
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {}

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
	private void edit(IStructuredSelection selection, int type) {
		Object obj = selection.getFirstElement();
		LibraryStandin standin = null;
		if(obj instanceof LibraryStandin) {
			standin = (LibraryStandin) obj;
		}
		else if(obj instanceof SubElement){
			SubElement sub = (SubElement)obj;
			standin = sub.getParent();
		}
		if(standin != null) {
			LibraryLocation library = standin.toLibraryLocation();
			if (type == SubElement.JAVADOC_URL) {
				URL[] urls = BuildPathDialogAccess.configureJavadocLocation(fLibraryViewer.getControl().getShell(), library.getSystemLibraryPath().toOSString(), library.getJavadocLocation());
				if (urls != null) {
					fLibraryContentProvider.setJavadoc(urls[0], selection);
				}
			} 
			else if(type == SubElement.SOURCE_PATH){
				IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(library.getSystemLibraryPath());
				entry.setSourceAttachmentPath(library.getSystemLibrarySourcePath());
				entry.setSourceAttachmentRootPath(library.getPackageRootPath());
				IClasspathEntry classpathEntry = BuildPathDialogAccess.configureSourceAttachment(fLibraryViewer.getControl().getShell(), entry.getClasspathEntry()); 
				if (classpathEntry != null) {
					fLibraryContentProvider.setSourcePath(classpathEntry.getSourceAttachmentPath(), classpathEntry.getSourceAttachmentRootPath(), selection);
				}
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
		IStructuredSelection selection = (IStructuredSelection) fLibraryViewer.getSelection();
		fRemoveButton.setEnabled(!selection.isEmpty());
		boolean enableUp = true, 
				enableDown = true, 
				allSource = true, 
				allJavadoc = true,
				allRoots = true;
		Object[] libraries = fLibraryContentProvider.getElements(null);
		if (selection.isEmpty() || libraries.length == 0) {
			enableUp = false;
			enableDown = false;
		} else {
			Object first = libraries[0];
			Object last = libraries[libraries.length - 1];
			for (Iterator iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				Object lib;
				if (element instanceof SubElement) {
					allRoots = false;
					SubElement subElement= (SubElement)element;
					lib = (subElement).getParent().toLibraryLocation();
					if (subElement.getType() == SubElement.JAVADOC_URL) {
						allSource = false;
					} else {
						allJavadoc = false;
					}
				} else {
					lib = element;
					allSource = false;
					allJavadoc = false;
				}
				if (lib == first) {
					enableUp = false;
				}
				if (lib == last) {
					enableDown = false;
				}
			}
		}
		fUpButton.setEnabled(enableUp);
		fDownButton.setEnabled(enableDown);
		fJavadocButton.setEnabled(!selection.isEmpty() && (allJavadoc || allRoots));
		fSourceButton.setEnabled(!selection.isEmpty() && (allSource || allRoots));
	}
}
