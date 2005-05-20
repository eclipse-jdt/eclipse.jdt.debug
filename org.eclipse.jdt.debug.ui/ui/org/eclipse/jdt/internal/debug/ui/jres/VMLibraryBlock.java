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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
	
	public class SubElement {
		
		public static final int JAVADOC_URL= 1;
		public static final int SOURCE_PATH= 2;
		
		private LibraryLocation fParent;
		private int fType;

		public SubElement(LibraryLocation parent, int type) {
			fParent= parent;
			fType= type;
		}
		
		public LibraryLocation getParent() {
			return fParent;
		}
		
		public int getType() {
			return fType;
		}
	}
	
	public class LibraryLabelProvider extends LabelProvider {

		private ImageDescriptorRegistry fRegistry= JavaPlugin.getImageDescriptorRegistry();

		public Image getImage(Object element) {
			if (element instanceof LibraryLocation) {
				LibraryLocation library= (LibraryLocation) element;
				IPath sourcePath= library.getSystemLibrarySourcePath();
				String key = null;
				if (sourcePath != null && !Path.EMPTY.equals(sourcePath)) {
                    key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE_WITH_SOURCE;
				} else {
					key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
				}
				return JavaUI.getSharedImages().getImage(key);
			} else if (element instanceof SubElement) {
				if (((SubElement)element).getType() == SubElement.SOURCE_PATH) {
					return fRegistry.get(JavaPluginImages.DESC_OBJS_SOURCE_ATTACH_ATTRIB); // todo: change image
				}
				return fRegistry.get(JavaPluginImages.DESC_OBJS_JAVADOC_LOCATION_ATTRIB); // todo: change image
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof LibraryLocation) {
				return ((LibraryLocation)element).getSystemLibraryPath().toOSString();
			} else if (element instanceof SubElement) {
				SubElement subElement= (SubElement) element;
				StringBuffer text= new StringBuffer();
				if (subElement.getType() == SubElement.SOURCE_PATH) {
					text.append(JREMessages.VMLibraryBlock_0); //$NON-NLS-1$
					IPath systemLibrarySourcePath= subElement.getParent().getSystemLibrarySourcePath();
					if (systemLibrarySourcePath != null && !Path.EMPTY.equals(systemLibrarySourcePath)) {
						text.append(systemLibrarySourcePath.toOSString());
					} else {
						text.append(JREMessages.VMLibraryBlock_1); //$NON-NLS-1$
					}
				} else {
					text.append(JREMessages.VMLibraryBlock_2); //$NON-NLS-1$
					URL javadocLocation= subElement.getParent().getJavadocLocation();
					if (javadocLocation != null) {
						text.append(javadocLocation.toExternalForm());
					} else {
						text.append(JREMessages.VMLibraryBlock_1); //$NON-NLS-1$
					}
				}
				return text.toString();
			}
			return null;
		}

	}
	public class LibraryContentProvider implements ITreeContentProvider {
		
		private HashMap fChildren= new HashMap();

		private LibraryLocation[] fLibraries= new LibraryLocation[0];

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fLibraries;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof LibraryLocation) {
				LibraryLocation libraryLocation= (LibraryLocation) parentElement;
				Object[] children= (Object[])fChildren.get(libraryLocation);
				if (children == null) {
					children= new Object[] {new SubElement(libraryLocation, SubElement.SOURCE_PATH), new SubElement(libraryLocation, SubElement.JAVADOC_URL)};
					fChildren.put(libraryLocation, children);
				}
				return children;
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof SubElement) {
				return ((SubElement)element).getParent();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return element instanceof LibraryLocation;
		}

		public void setLibraries(LibraryLocation[] libs) {
			fLibraries= libs;
			fLibraryViewer.refresh();
		}

		public LibraryLocation[] getLibraries() {
			return fLibraries;
		}

		/**
		 * Returns the list of libraries in the given selection. SubElements
		 * are replaced by their parent libraries.
		 */
		private Set getSelectedLibraries(IStructuredSelection selection) {
			Set libraries= new HashSet();
			for (Iterator iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				if (element instanceof LibraryLocation) {
					libraries.add(element);
				} else if (element instanceof SubElement) {
					libraries.add(((SubElement)element).getParent());
				}
			}
			return libraries;
		}

		/**
		 * Move the libraries of the given selection up.
		 */
		public void up(IStructuredSelection selection) {
			Set libraries= getSelectedLibraries(selection);
			for (int i= 0; i < fLibraries.length - 1; i++) {
				if (libraries.contains(fLibraries[i + 1])) {
					LibraryLocation temp= fLibraries[i];
					fLibraries[i]= fLibraries[i + 1];
					fLibraries[i + 1]= temp;
				}
			}
			fLibraryViewer.refresh();
			fLibraryViewer.setSelection(selection);
		}

		/**
		 * Move the libraries of the given selection down.
		 */
		public void down(IStructuredSelection selection) {
			Set libraries= getSelectedLibraries(selection);
			for (int i= fLibraries.length - 1; i > 0; i--) {
				if (libraries.contains(fLibraries[i - 1])) {
					LibraryLocation temp= fLibraries[i];
					fLibraries[i]= fLibraries[i - 1];
					fLibraries[i - 1]= temp;
				}
			}
			fLibraryViewer.refresh();
			fLibraryViewer.setSelection(selection);
		}

		/**
		 * Remove the libraries contained in the given selection.
		 */
		public void remove(IStructuredSelection selection) {
			Set libraries= getSelectedLibraries(selection);
			LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length - libraries.size()];
			int k= 0;
			for (int i= 0; i < fLibraries.length; i++) {
				if (!libraries.contains(fLibraries[i])) {
					newLibraries[k++]= fLibraries[i];
				}
			}
			fLibraries= newLibraries;
			fLibraryViewer.refresh();
		}

		/**
		 * Add the given libraries before the selection, or after the existing libraries
		 * if the selection is empty.
		 */
		public void add(LibraryLocation[] libs, IStructuredSelection selection) {
			LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length + libs.length];
			if (selection.isEmpty()) {
				System.arraycopy(fLibraries, 0, newLibraries, 0, fLibraries.length);
				System.arraycopy(libs, 0, newLibraries, fLibraries.length, libs.length);
			} else {
				Object element= selection.getFirstElement();
				LibraryLocation firstLib;
				if (element instanceof LibraryLocation) {
					firstLib= (LibraryLocation) element;
				} else {
					firstLib= ((SubElement) element).getParent();
				}
				int i= 0;
				while (i < fLibraries.length && fLibraries[i] != firstLib) {
					newLibraries[i]= fLibraries[i++];
				}
				System.arraycopy(libs, 0, newLibraries, i, libs.length);
				System.arraycopy(fLibraries, i, newLibraries, i + libs.length, fLibraries.length - i);
			}
			fLibraries= newLibraries;
			fLibraryViewer.refresh();
			fLibraryViewer.setSelection(new StructuredSelection(libs), true);
		}

		/**
		 * Set the given URL as the javadoc location for the libraries contained in
		 * the given selection.
		 */
		public void setJavadoc(URL javadocLocation, IStructuredSelection selection) {
			Set libraries= getSelectedLibraries(selection);
			LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length];
			Object[] newSelection= new Object[libraries.size()];
			int j= 0;
			for (int i= 0; i < fLibraries.length; i++) {
				LibraryLocation library= fLibraries[i];
				if (libraries.contains(library)) {
					LibraryLocation lib= new LibraryLocation(library.getSystemLibraryPath(), library.getSystemLibrarySourcePath(), library.getPackageRootPath(), javadocLocation);
					newSelection[j++]= getChildren(lib)[1];
					newLibraries[i]= lib;
				} else {
					newLibraries[i]= library;
				}
			}
			fLibraries= newLibraries;
			fLibraryViewer.refresh();
			fLibraryViewer.setSelection(new StructuredSelection(newSelection));
		}

		/**
		 * Set the given paths as the source info for the libraries contained in
		 * the given selection.
		 */
		public void setSourcePath(IPath sourceAttachmentPath, IPath sourceAttachmentRootPath, IStructuredSelection selection) {
			Set libraries= getSelectedLibraries(selection);
			LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length];
			Object[] newSelection= new Object[libraries.size()];
			int j= 0;
			for (int i= 0; i < fLibraries.length; i++) {
				LibraryLocation library= fLibraries[i];
				if (libraries.contains(library)) {
					if (sourceAttachmentPath == null) {
						sourceAttachmentPath = Path.EMPTY;
					}
					if (sourceAttachmentRootPath == null) {
						sourceAttachmentRootPath = Path.EMPTY;
					}
					LibraryLocation lib= new LibraryLocation(library.getSystemLibraryPath(), sourceAttachmentPath, sourceAttachmentRootPath, library.getJavadocLocation());
					newSelection[j++]= getChildren(lib)[1];
					newLibraries[i]= lib;
				} else {
					newLibraries[i]= library;
				}
			}
			fLibraries= newLibraries;
			fLibraryViewer.refresh();
			fLibraryViewer.setSelection(new StructuredSelection(newSelection));
		}
		
	}
	
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
		
		fDefaultButton = new Button(comp, SWT.CHECK);
		fDefaultButton.setText(JREMessages.VMLibraryBlock_Use_default_system_libraries_1); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fDefaultButton.setLayoutData(gd);
		fDefaultButton.setFont(font);
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleDefaultButtonSelected();
			}
		});
		
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
		
		fAddButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_7); //$NON-NLS-1$
		fAddButton.addSelectionListener(this);
		
		fEditButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_8); //$NON-NLS-1$
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

		fRemoveButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_6); //$NON-NLS-1$
		fRemoveButton.addSelectionListener(this);
		
		fUpButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_4); //$NON-NLS-1$
		fUpButton.addSelectionListener(this);
		
		fDownButton= createPushButton(pathButtonComp, JREMessages.VMLibraryBlock_5); //$NON-NLS-1$
		fDownButton.addSelectionListener(this);

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
		}
		fDefaultButton.setSelection(vm == null || vm.getLibraryLocations() == null);
		if (isDefaultSystemLibrary()) {
			update();
		} else {
			LibraryLocation[] libs = vm.getLibraryLocations();
			fLibraryContentProvider.setLibraries(libs);
			updateButtons();
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
		IStatus status = null;
		if (useDefault) {
			if (getHomeDirectory() == null) {
				libs = new LibraryLocation[0];
			} else {
				libs = getVMInstallType().getDefaultLibraryLocations(getHomeDirectory());
			}
			fLibraryContentProvider.setLibraries(libs);
		}
		updateButtons();
		if (fLibraryContentProvider.getLibraries().length == 0 && !isDefaultSystemLibrary()) {
			status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				JREMessages.VMLibraryBlock_Libraries_cannot_be_empty__1, null); //$NON-NLS-1$
		} else if (status == null) {
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
			LibraryLocation[] libs = fLibraryContentProvider.getLibraries();
			vm.setLibraryLocations(libs);
		}		
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
		}
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
		dialog.setText(ActionMessages.AddExternalJar_Jar_Selection_3); //$NON-NLS-1$
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
		LibraryLocation library= firstElement.getParent();
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
		boolean useDefault= fDefaultButton.getSelection();
		fAddButton.setEnabled(!useDefault);
		fRemoveButton.setEnabled(!useDefault && !selection.isEmpty());
		boolean enableUp= true;
		boolean enableDown= true;
		boolean allSource= true;
		boolean allJavadoc= true;
		LibraryLocation[] libraries= fLibraryContentProvider.getLibraries();
		if (useDefault || selection.isEmpty() || libraries.length == 0) {
			enableUp= enableDown= false;
		} else {
			LibraryLocation first= libraries[0];
			LibraryLocation last= libraries[libraries.length - 1];
			for (Iterator iter= selection.iterator(); iter.hasNext();) {
				Object element= iter.next();
				LibraryLocation lib;
				if (element instanceof LibraryLocation) {
					lib= (LibraryLocation)element;
					allSource= allJavadoc= false;
				} else {
					SubElement subElement= (SubElement)element;
					lib= (subElement).getParent();
					if (subElement.getType() == SubElement.JAVADOC_URL) {
						allSource= false;
					} else {
						allJavadoc= false;
					}
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
		fEditButton.setEnabled(!useDefault && !selection.isEmpty() && (allSource || allJavadoc));
	}
}
