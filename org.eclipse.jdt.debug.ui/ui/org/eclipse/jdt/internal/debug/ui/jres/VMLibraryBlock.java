/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.jres.LibraryContentProvider.SubElement;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.ExecutionEnvironmentDescription;
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
import org.eclipse.swt.widgets.FileDialog;

/**
 * Control used to edit the libraries associated with a VM install
 */
public class VMLibraryBlock extends AbstractVMInstallPage implements SelectionListener, ISelectionChangedListener {

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
	protected VMStandin fVmInstall;

	//widgets
	protected LibraryContentProvider fLibraryContentProvider;
	protected TreeViewer fLibraryViewer;
	private Button fUpButton;
	private Button fDownButton;
	private Button fRemoveButton;
	private Button fAddButton;
	private Button fJavadocButton;
	private Button fSourceButton;
	protected Button fDefaultButton;

	private final IStatus[] fLibStatus;

	private Button fAnnotationsButton;

	/**
	 * Constructs a new wizard page with the given name.
	 *
	 * @param pageName page name
	 */
	VMLibraryBlock() {
		super(JREMessages.VMLibraryBlock_2);
		fLibStatus = new IStatus[]{Status.OK_STATUS};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
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
		fJavadocButton.setEnabled(false);
		fJavadocButton.addSelectionListener(this);

		fSourceButton =  SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_11, JREMessages.VMLibraryBlock_18, null);
		fSourceButton.setEnabled(false);
		fSourceButton.addSelectionListener(this);

		fAnnotationsButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMExternalAnnsBlock_3, JREMessages.VMExternalAnnsBlock_4, null);
		fAnnotationsButton.setEnabled(false);
		fAnnotationsButton.addSelectionListener(this);

		fLibraryViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getViewer().getSelection();
				Object obj = sel.getFirstElement();
				if(obj instanceof SubElement) {
					edit(sel, ((SubElement)obj).getType());
				}
			}
		});
		fRemoveButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_6, JREMessages.VMLibraryBlock_12, null);
		fRemoveButton.setEnabled(false);
		fRemoveButton.addSelectionListener(this);
		fUpButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_4, JREMessages.VMLibraryBlock_13, null);
		fUpButton.setEnabled(false);
		fUpButton.addSelectionListener(this);
		fDownButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_5, JREMessages.VMLibraryBlock_14, null);
		fDownButton.setEnabled(false);
		fDownButton.addSelectionListener(this);
		fDefaultButton = SWTFactory.createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_9, JREMessages.VMLibraryBlock_15, null);
		fDefaultButton.addSelectionListener(this);

		setControl(comp);
	}

	/**
	 * The "default" button has been toggled
	 */
	private void restoreDefaultLibraries() {
		LibraryLocation[] libs = null;
		File installLocation = null;
		if (fVmInstall != null) {
			if (EEVMType.ID_EE_VM_TYPE.equals(fVmInstall.getVMInstallType().getId())) {
				File definitionFile = null;
				String path = fVmInstall.getAttribute(EEVMInstall.ATTR_DEFINITION_FILE);
				if (path != null) {
					definitionFile = new File(path);
				}
				if (definitionFile != null) {
					try {
						ExecutionEnvironmentDescription desc = new ExecutionEnvironmentDescription(definitionFile);
						libs = desc.getLibraryLocations();
					} catch (CoreException e) {
						libs = new LibraryLocation[0];
					}
				} else {
					libs = new LibraryLocation[0];
				}
			} else {
				installLocation = fVmInstall.getInstallLocation();
				if (installLocation == null) {
					libs = new LibraryLocation[0];
				} else {
					libs = fVmInstall.getVMInstallType().getDefaultLibraryLocations(installLocation);
				}
			}
		}
		fLibraryContentProvider.setLibraries(libs);
		update();
	}

	/**
	 * Updates buttons and status based on current libraries
	 */
	private void update() {
		updateButtons();
		IStatus status = Status.OK_STATUS;
		if (fLibraryContentProvider.getLibraries().length == 0) {
			status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, JREMessages.VMLibraryBlock_19, null);
		}
		LibraryStandin[] standins = fLibraryContentProvider.getStandins();
		for (int i = 0; i < standins.length; i++) {
			IStatus st = standins[i].validate();
			if (!st.isOK()) {
				status = st;
				break;
			}
		}
		fLibStatus[0] = status;
		if (status.isOK()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(status.getMessage());
			setPageComplete(false);
		}
		// must force since this page is a 'sub-page' and may not be considered the current page
		if (getContainer().getCurrentPage() != this) {
			getContainer().updateMessage();
			getContainer().updateButtons();
		}
	}

	/**
	 * Determines if the current libraries displayed to the user are the default location
	 * for the given vm working copy.
	 * @param vm the virtual machine to check for the default location
	 * @return true if the current set of locations are the defaults, false otherwise
	 */
	protected boolean isDefaultLocations(IVMInstall vm) {
		LibraryLocation[] libraryLocations = fLibraryContentProvider.getLibraries();

		if (vm == null || libraryLocations == null) {
			return true;
		}
		File installLocation = vm.getInstallLocation();
		if (installLocation != null) {
			LibraryLocation[] def = vm.getVMInstallType().getDefaultLibraryLocations(installLocation);
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

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		boolean completed = true;
		Object source= e.getSource();
		if (source == fUpButton) {
			fLibraryContentProvider.up((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fDownButton) {
			fLibraryContentProvider.down((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fRemoveButton) {
			fLibraryContentProvider.remove((IStructuredSelection) fLibraryViewer.getSelection());
		} else if (source == fAddButton) {
			completed = add((IStructuredSelection) fLibraryViewer.getSelection());
		}
		else if(source == fJavadocButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection(), SubElement.JAVADOC_URL);
		}
		else if(source == fSourceButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection(), SubElement.SOURCE_PATH);
		}
		else if (source == fAnnotationsButton) {
			edit((IStructuredSelection) fLibraryViewer.getSelection(), SubElement.EXTERNAL_ANNOTATIONS_PATH);
		}
		else if (source == fDefaultButton) {
			restoreDefaultLibraries();
		}
		if (completed) {
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	/**
	 * Adds external libraries to the JRE. Returns <code>true</code> if successful and
	 * <code>false</code> if canceled.
	 */
	private boolean add(IStructuredSelection selection) {
		IDialogSettings dialogSettings= JDIDebugUIPlugin.getDefault().getDialogSettings();
		String lastUsedPath= dialogSettings.get(LAST_PATH_SETTING);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(fLibraryViewer.getControl().getShell(), SWT.MULTI | SWT.SHEET);
		dialog.setText(JREMessages.VMLibraryBlock_10);
		dialog.setFilterExtensions(new String[] {"*.jar;*.zip","*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res == null) {
			return false;
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
		return true;
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
			switch (type) {
			case SubElement.JAVADOC_URL:
				URL[] urls = BuildPathDialogAccess.configureJavadocLocation(fLibraryViewer.getControl().getShell(), library.getSystemLibraryPath().toOSString(), library.getJavadocLocation());
				if (urls != null) {
					fLibraryContentProvider.setJavadoc(urls[0], selection);
				}
				break;
			case SubElement.SOURCE_PATH:
				{
					IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(library.getSystemLibraryPath());
					entry.setSourceAttachmentPath(library.getSystemLibrarySourcePath());
					entry.setSourceAttachmentRootPath(library.getPackageRootPath());
					IClasspathEntry classpathEntry = BuildPathDialogAccess.configureSourceAttachment(fLibraryViewer.getControl().getShell(), entry.getClasspathEntry());
					if (classpathEntry != null) {
						fLibraryContentProvider.setSourcePath(classpathEntry.getSourceAttachmentPath(), classpathEntry.getSourceAttachmentRootPath(), selection);
					}
					break;
				}
			case SubElement.EXTERNAL_ANNOTATIONS_PATH:
				{
					IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(library.getSystemLibraryPath());
					entry.setExternalAnnotationsPath(library.getExternalAnnotationsPath());
					IClasspathAttribute[] extraAttributes = entry.getClasspathEntry().getExtraAttributes();
					String annotationPathString = findClasspathAttribute(extraAttributes, IClasspathAttribute.EXTERNAL_ANNOTATION_PATH);
					IPath annotationPath = null == annotationPathString ? null : new Path(annotationPathString);
					IPath newPath = BuildPathDialogAccess.configureExternalAnnotationsAttachment(fLibraryViewer.getControl().getShell(), annotationPath);
					if (null == newPath) {
						return;
					}
					fLibraryContentProvider.setAnnotationsPath(newPath.segmentCount() == 0 ? null : newPath, selection);
					break;
				}
			default:
				break;
			}
		}
	}

	private static String findClasspathAttribute(IClasspathAttribute[] attributes, String name) {
		for(int i = attributes.length; --i >= 0;) {
			if(name.equals(attributes[i].getName())) {
				return attributes[i].getValue();
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
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
				allAnnotations = true,
				allRoots = true;
		Object[] libraries = fLibraryContentProvider.getElements(null);
		if (selection.isEmpty() || libraries.length == 0) {
			enableUp = false;
			enableDown = false;
		} else {
			Object first = libraries[0];
			Object last = libraries[libraries.length - 1];
			for (Iterator<?> iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				Object lib;
				if (element instanceof SubElement) {
					allRoots = false;
					SubElement subElement= (SubElement)element;
					lib = (subElement).getParent().toLibraryLocation();
					switch (subElement.getType()) {
						case SubElement.JAVADOC_URL:
							allSource = false;
							allAnnotations = false;
							break;
						case SubElement.SOURCE_PATH:
							allAnnotations = false;
							allJavadoc = false;
							break;
						case SubElement.EXTERNAL_ANNOTATIONS_PATH:
							allJavadoc = false;
							allSource = false;
							break;
					}
				} else {
					lib = element;
					allSource = false;
					allJavadoc = false;
					allAnnotations = false;
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
		fAnnotationsButton.setEnabled(!selection.isEmpty() && (allAnnotations || allRoots));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#finish()
	 */
	@Override
	public boolean finish() {
		if (fVmInstall != null) {
			if (isDefaultLocations(fVmInstall)) {
				fVmInstall.setLibraryLocations(null);
			} else {
				LibraryLocation[] libs = fLibraryContentProvider.getLibraries();
				fVmInstall.setLibraryLocations(libs);
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#getSelection()
	 */
	@Override
	public VMStandin getSelection() {
		return fVmInstall;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#setSelection(org.eclipse.jdt.launching.VMStandin)
	 */
	@Override
	public void setSelection(VMStandin vm) {
		super.setSelection(vm);
		LibraryLocation[] libraryLocations = null;
		if (vm == null) {
			libraryLocations = new LibraryLocation[0];
		} else {
			libraryLocations = JavaRuntime.getLibraryLocations(vm);
		}
		fVmInstall = vm;
		fLibraryContentProvider.setLibraries(libraryLocations);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage#getVMStatus()
	 */
	@Override
	protected IStatus[] getVMStatus() {
		return fLibStatus;
	}

}
