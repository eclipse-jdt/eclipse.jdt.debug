/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
/*
 * The page for setting the default Java runtime preference.
 */
public class VMPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
																	IAddVMDialogRequestor {	
	// The main list control
	private CheckboxTableViewer fVMList;
	
	// Action buttons
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;
	private Button fSearchButton;	
	
	private IVMInstallType[] fVMTypes;
	
	// the VMs defined when this page was opened
	private VMDefinitionsContainer fOriginalVMs;
	
	// The VMs currently in the Checkbox table
	private List fVMStandins;

	// Make sure that VMStandin ids are unique if multiple calls to System.currentTimeMillis()
	// happen very quickly
	private static String fgLastUsedID;

	public VMPreferencePage() {
		super();
		
		// only used when page is shown programatically
		setTitle(LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"));	 //$NON-NLS-1$
				
		
		setDescription(LauncherMessages.getString("vmPreferencePage.message")); //$NON-NLS-1$
		fVMTypes = JavaRuntime.getVMInstallTypes();
		fOriginalVMs = new VMDefinitionsContainer();
		IVMInstall def = JavaRuntime.getDefaultVMInstall();
		if (def != null) {
			fOriginalVMs.setDefaultVMInstallCompositeID(JavaRuntime.getCompositeIdFromVM(def));
		}
		for (int i = 0; i < fVMTypes.length; i++) {
			IVMInstall[] vms = fVMTypes[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				fOriginalVMs.addVM(vms[j]);
			}
		}
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Set the VM list that will be input for the main list control.
	 */
	private void populateVMList() {
		
		// force auto-dection to occurr before populating the VM list.
		JavaRuntime.getDefaultVMInstall();
		
		// Retrieve all known VM installs from each vm install type
		fVMStandins= new ArrayList();
		for (int i= 0; i < fVMTypes.length; i++) {
			IVMInstall[] vmInstalls= fVMTypes[i].getVMInstalls();
			for (int j = 0; j < vmInstalls.length; j++) {
				fVMStandins.add(new VMStandin(vmInstalls[j]));
			}
		}
		
		// Set the input of the main list control
		fVMList.setInput(fVMStandins);
		
		// Set up the default VM
		initDefaultVM();
	}
	
	/**
	 * Find & verify the default VM.
	 */
	private void initDefaultVM() {
		IVMInstall realDefault= JavaRuntime.getDefaultVMInstall();
		if (realDefault != null) {
			Iterator iter= fVMStandins.iterator();
			while (iter.hasNext()) {
				IVMInstall fakeVM= (IVMInstall)iter.next();
				if (isSameVM(fakeVM, realDefault)) {
					verifyDefaultVM(fakeVM);
					break;
				}
			}
		}
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {
		Font font= ancestor.getFont();
		initializeDialogUnits(ancestor);
		
		noDefaultAndApplyButton();
		
		Composite parent= new Composite(ancestor, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);		
		
		GridData data;
		
		Label tableLabel = new Label(parent, SWT.NONE);
		tableLabel.setText(LauncherMessages.getString("VMPreferencePage.Installed_&JREs__1")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		tableLabel.setLayoutData(data);
		
		Table table= new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		data= new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setFont(font);
				
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		

		TableLayout tableLayout= new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1= new TableColumn(table, SWT.NULL);
		column1.setText(LauncherMessages.getString("vmPreferencePage.jreType")); //$NON-NLS-1$
	
		TableColumn column2= new TableColumn(table, SWT.NULL);
		column2.setText(LauncherMessages.getString("vmPreferencePage.jreName")); //$NON-NLS-1$
		
		TableColumn column3= new TableColumn(table, SWT.NULL);
		column3.setText(LauncherMessages.getString("vmPreferencePage.jreLocation")); //$NON-NLS-1$

		fVMList= new CheckboxTableViewer(table);
		
		fVMList.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IVMInstall) && (e2 instanceof IVMInstall)) {
					IVMInstall left= (IVMInstall)e1;
					IVMInstall right= (IVMInstall)e2;
					String leftType= left.getVMInstallType().getName();
					String rightType= right.getVMInstallType().getName();
					int res= leftType.compareToIgnoreCase(rightType);
					if (res != 0) {
						return res;
					}
					return left.getName().compareToIgnoreCase(right.getName());
				}
				return super.compare(viewer, e1, e2);
			}
			
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});		
			
		fVMList.setLabelProvider(new VMLabelProvider());
		fVMList.setContentProvider(new ListContentProvider(fVMList, Collections.EMPTY_LIST));
		
		fVMList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});
		
		fVMList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				IVMInstall vm=  (IVMInstall)event.getElement();
				if (event.getChecked()) {
					verifyDefaultVM(vm);
				}
				fVMList.setCheckedElements(new Object[] { vm });
			}
		});
		
		fVMList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				editVM();
			}
		});
		
		Composite buttons= new Composite(parent, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		buttons.setLayout(layout);
		
		fAddButton= new Button(buttons, SWT.PUSH);
		setButtonLayoutData(fAddButton);
		fAddButton.setFont(font);
		fAddButton.setText(LauncherMessages.getString("vmPreferencePage.add")); //$NON-NLS-1$
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addVM();
			}
		});
		
		fEditButton= new Button(buttons, SWT.PUSH);
		setButtonLayoutData(fEditButton);
		fEditButton.setFont(font);
		fEditButton.setText(LauncherMessages.getString("vmPreferencePage.edit")); //$NON-NLS-1$
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editVM();
			}
		});
		
		fRemoveButton= new Button(buttons, SWT.PUSH);
		fRemoveButton.setFont(font);
		setButtonLayoutData(fRemoveButton);
		fRemoveButton.setText(LauncherMessages.getString("vmPreferencePage.remove")); //$NON-NLS-1$
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeVMs();
			}
		});
		
		fSearchButton = new Button(buttons, SWT.PUSH);
		fSearchButton.setFont(font);
		setButtonLayoutData(fSearchButton);
		fSearchButton.setText(LauncherMessages.getString("VMPreferencePage.&Search..._1")); //$NON-NLS-1$
		fSearchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				search();
			}
		});		
		
		configureTableResizing(parent, buttons, table, column1, column2, column3);
		
		populateVMList();
		enableButtons();
		WorkbenchHelp.setHelp(parent, IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);		

		return parent;
	}

	/**
	 * Correctly resizes the table so no phantom columns appear
	 */
	protected void configureTableResizing(final Composite parent, final Composite buttons, final Table table, final TableColumn column1, final TableColumn column2, final TableColumn column3) {
		parent.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	Rectangle area = parent.getClientArea();
			    Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			    int width = area.width - 2 * table.getBorderWidth();
			    if (preferredSize.y > area.height) {
			     	// Subtract the scrollbar width from the total column width
			     	// if a vertical scrollbar will be required
			     	Point vBarSize = table.getVerticalBar().getSize();
			     	width -= vBarSize.x;
			    }
			    width-= buttons.getSize().x;
			    Point oldSize = table.getSize();
			    if (oldSize.x > width) {
			    	// table is getting smaller so make the columns
					// smaller first and then resize the table to
				    // match the client area width
				    column1.setWidth(width/4);
				    column2.setWidth(width/4);
			    	column3.setWidth(width - (column1.getWidth() + column2.getWidth()));
				    table.setSize(width, area.height);
			    } else {
			    	// table is getting bigger so make the table
			    	// bigger first and then make the columns wider
			    	// to match the client area width
			      	table.setSize(width, area.height);
				    column1.setWidth(width/4);
				    column2.setWidth(width/4);
				    column3.setWidth(width - (column1.getWidth() + column2.getWidth()));
			     }
		    }
		});
	}
			
	/**
	 * @see IAddVMDialogRequestor#isDuplicateName(String)
	 */
	public boolean isDuplicateName(String name) {
		for (int i= 0; i < fVMStandins.size(); i++) {
			IVMInstall vm = (IVMInstall)fVMStandins.get(i);
			if (vm.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
			
	/**
	 * Bring up a dialog that lets the user create a new VM definition.
	 */
	private void addVM() {
		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, null);
		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.addJRE.title")); //$NON-NLS-1$
		if (dialog.open() != AddVMDialog.OK) {
			return;
		}
		fVMList.refresh();
	}
	
	/**
	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
		fVMStandins.add(vm);
		fVMList.refresh();
		if (getCurrentDefaultVM() == null) {
			verifyDefaultVM(vm);
		}
	}
	
	private void removeVMs() {
		IStructuredSelection selection= (IStructuredSelection)fVMList.getSelection();
		Iterator elements= selection.iterator();
		while (elements.hasNext()) {
			Object o= elements.next();
			fVMStandins.remove(o);
		}
		fVMList.refresh();
		// this is order dependent. Must first refresh to work with 
		// the new state of affairs
		if (getCurrentDefaultVM() == null) {
			if (fVMList.getTable().getItemCount() > 0) {
				verifyDefaultVM((IVMInstall)fVMList.getElementAt(0));
			}
		}
	}
		
	private void editVM() {
		IStructuredSelection selection= (IStructuredSelection)fVMList.getSelection();
		// assume it's length one, otherwise this will not be called
		IVMInstall vm= (IVMInstall)selection.getFirstElement();
		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, vm);
		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.editJRE.title")); //$NON-NLS-1$
		if (dialog.open() != AddVMDialog.OK) {
			return;
		}
		fVMList.refresh(vm);
	}
	
	private boolean isSameVM(IVMInstall left, IVMInstall right) {
		if (left == right) {
			return true;
		}
		if (left != null && right != null) {
			return left.getId().equals(right.getId());
		}
		return false;
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		// Create a VM definition container
		VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
		
		// Set the default VM Id on the container
		IVMInstall defaultVM = getCurrentDefaultVM();
		String defaultVMId = JavaRuntime.getCompositeIdFromVM(defaultVM);
		vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
		
		// Set the VMs on the container
		vmContainer.addVMList(fVMStandins);
		
		// determine if a build is required
		boolean buildRequired = false;
		try {
			buildRequired = isBuildRequired(fOriginalVMs, vmContainer);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		} 
		boolean build = false;
		if (buildRequired) {
			// prompt the user to do a full build
			MessageDialog messageDialog = new MessageDialog(getShell(), LauncherMessages.getString("VMPreferencePage.JRE_Settings_Changed_1"), null,  //$NON-NLS-1$
			LauncherMessages.getString("VMPreferencePage.The_JRE_settings_have_changed._A_full_build_is_required_to_make_the_changes_effective._Do_the_full_build_now__2"), //$NON-NLS-1$
			MessageDialog.QUESTION, new String[] {LauncherMessages.getString("VMPreferencePage.&Yes_3"), LauncherMessages.getString("VMPreferencePage.&No_4"), LauncherMessages.getString("VMPreferencePage.&Cancel_1")}, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			int button = messageDialog.open();
			if (button == 2) {
				return false;
			}
			build = button == 0;			
		}
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		// do a build if required
		if (build) {
			buildWorkspace();
		}
		
		return super.performOk();
	}	
	
	private void saveVMDefinitions(VMDefinitionsContainer container) {
		// Generate XML for the VM defs and save it as the new value of the VM preference
		try {
			String vmDefXML = container.getAsXML();
			JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_VM_XML, vmDefXML);
			JavaRuntime.savePreferences();
		} catch (IOException ioe) {
			JDIDebugUIPlugin.log(ioe);
		}			
	}
	
	protected IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	private void buildWorkspace() {
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException{
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// opearation canceled by user
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"), LauncherMessages.getString("VMPreferencePage.Build_failed._1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}			
	
	private IVMInstall getCurrentDefaultVM() {
		Object[] checked= fVMList.getCheckedElements();
		if (checked.length > 0) {
			return (IVMInstall)checked[0];
		}
		return null;
	}

	private void enableButtons() {
		fAddButton.setEnabled(fVMTypes.length > 0);
		int selectionCount= ((IStructuredSelection)fVMList.getSelection()).size();
		fEditButton.setEnabled(selectionCount == 1);
		fRemoveButton.setEnabled(selectionCount > 0 && selectionCount < fVMList.getTable().getItemCount());
	}
	
	/**
	 * Verify that the specified VM can be a valid default VM.  This amounts to verifying
	 * that all of the VM's library locations exist on the file system.  If this fails,
	 * remove the VM from the table and try to set another default.
	 */
	private void verifyDefaultVM(IVMInstall vm) {
		if (vm != null) {
			
			// Verify that all of the specified VM's library locations actually exist
			LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vm);
			boolean exist = true;
			for (int i = 0; i < locations.length; i++) {
				exist = exist && new File(locations[i].getSystemLibraryPath().toOSString()).exists();
			}
			
			// If all library locations exist, check the corresponding entry in the list,
			// otherwise remove the VM
			if (exist) {
				fVMList.setCheckedElements(new Object[] { vm });
			} else {
				fVMList.remove(vm);
				fVMStandins.remove(vm);
				IVMInstall def = JavaRuntime.getDefaultVMInstall();
				if (def == null) {
					fVMList.setCheckedElements(new Object[0]);
				} else {
					fVMList.setChecked(def, true);
				}
				ErrorDialog.openError(getControl().getShell(), LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"), LauncherMessages.getString("VMPreferencePage.Installed_JRE_location_no_longer_exists.__JRE_will_be_removed_2"), new Status(IStatus.ERROR, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, LauncherMessages.getString("VMPreferencePage.JRE_removed_3"), null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
		} else {
			fVMList.setCheckedElements(new Object[0]);
		}
	}
	
	/**
	 * @see IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(LauncherMessages.getString("vmPreferencePage.title")); //$NON-NLS-1$
		}
	}

	/**
	 * Search for installed VMs in the file system
	 */
	public void search() {
		
		// choose a root directory for the search 
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LauncherMessages.getString("VMPreferencePage.Select_a_directory_to_search_in._2")); //$NON-NLS-1$
		dialog.setText(LauncherMessages.getString("VMPreferencePage.Directory_Selection_3")); //$NON-NLS-1$
		String path = dialog.open();
		if (path == null) {
			return;
		}
		
		// ignore installed locations
		final Set exstingLocations = new HashSet();
		Iterator iter = fVMStandins.iterator();
		while (iter.hasNext()) {
			exstingLocations.add(((IVMInstall)iter.next()).getInstallLocation());
		}
		
		// search
		final File rootDir = new File(path);
		final List locations = new ArrayList();
		final List types = new ArrayList();
		ProgressMonitorDialog pm = new ProgressMonitorDialog(getShell());

		IRunnableWithProgress r = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(LauncherMessages.getString("VMPreferencePage.Searching..._4"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
				search(rootDir, locations, types, exstingLocations, monitor);
				monitor.done();
			}
		};
		
		try {
			pm.run(true, true, r);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			// cancelled
			return;
		}
		
		if (locations.isEmpty()) {
			MessageDialog.openInformation(getShell(), LauncherMessages.getString("VMPreferencePage.Information_1"), MessageFormat.format(LauncherMessages.getString("VMPreferencePage.No_JREs_found_in_{0}_2"), new String[]{path})); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			iter = locations.iterator();
			Iterator iter2 = types.iterator();
			while (iter.hasNext()) {
				File location = (File)iter.next();
				IVMInstallType type = (IVMInstallType)iter2.next();
				IVMInstall vm = new VMStandin(type, createUniqueId(type));
				String name = location.getName();
				String nameCopy = new String(name);
				int i = 1;
				while (isDuplicateName(nameCopy)) {
					nameCopy = name + '(' + i++ + ')'; 
				}
				vm.setName(nameCopy);
				vm.setInstallLocation(location);
				if (type instanceof AbstractVMInstallType) {
					//set default java doc location
					AbstractVMInstallType abs = (AbstractVMInstallType)type;
					vm.setJavadocLocation(abs.getDefaultJavadocLocation(location));
				}
				vmAdded(vm);
			}
		}
		
	}
	
	/**
	 * Find a unique VM id.  Check existing 'real' VMs, as well as the last id used for
	 * a VMStandin.
	 */
	private String createUniqueId(IVMInstallType vmType) {
		String id= null;
		do {
			id= String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null || id.equals(fgLastUsedID));
		fgLastUsedID = id;
		return id;
	}	
	
	/**
	 * Searches the specified directory recursively for installed VMs, adding each
	 * detected VM to the <code>found</code> list. Any directories specified in
	 * the <code>ignore</code> are not traversed.
	 * 
	 * @param directory
	 * @param found
	 * @param types
	 * @param ignore
	 */
	protected void search(File directory, List found, List types, Set ignore, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		String[] names = directory.list();
		List subDirs = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if (monitor.isCanceled()) {
				return;
			}
			File file = new File(directory, names[i]);
			try {
				monitor.subTask(MessageFormat.format(LauncherMessages.getString("VMPreferencePage.Found__{0}_-_Searching_{1}_7"), new String[]{Integer.toString(found.size()), file.getCanonicalPath()})); //$NON-NLS-1$
			} catch (IOException e) {
			}			
			if (file.isDirectory()) {
				if (!ignore.contains(file)) {
					boolean validLocation = false;
					
					// Take the first VM install type that claims the location as a
					// valid VM install.  VM install types should be smart enough to not
					// claim another type's VM, but just in case...
					for (int j = 0; j < fVMTypes.length; j++) {
						if (monitor.isCanceled()) {
							return;
						}
						IVMInstallType type = fVMTypes[j];
						IStatus status = type.validateInstallLocation(file);
						if (status.isOK()) {
							found.add(file);
							types.add(type);
							validLocation = true;
							break;
						}
					}
					if (!validLocation) {
						subDirs.add(file);
					}
				}
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = (File)subDirs.remove(0);
			search(subDir, found, types, ignore, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}
		
	}
	
	/**
	 * Returns whether a re-build is required based on the previous and current
	 * VM definitions.
	 * 
	 * @param prev VMs defined in the workspace
	 * @param curr VMs that will be defined in the workspace
	 * @return whether the new JRE definitions required the workspace to be
	 * built
	 */
	private boolean isBuildRequired(VMDefinitionsContainer prev, VMDefinitionsContainer curr) throws CoreException {
		String prevDef = prev.getDefaultVMInstallCompositeID();
		String currDef = curr.getDefaultVMInstallCompositeID();
		
		boolean defaultChanged = !isEqual(prevDef, currDef);
		
		IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
		
		//if the default VM changed, see if any projects reference it
		if (defaultChanged) {
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				IClasspathEntry[] entries = project.getRawClasspath();
				for (int j = 0; j < entries.length; j++) {
					IClasspathEntry entry = entries[j];
					switch (entry.getEntryKind()) {
						case IClasspathEntry.CPE_VARIABLE:
							IPath path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRELIB_VARIABLE)) {
								// a project references the default JRE via JRE_LIB
								return true;
							}
							break;
						case IClasspathEntry.CPE_CONTAINER:
							path = entry.getPath();
							if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
								// a project references the default JRE via JRE_CONTAIER
								return true;
							}
							break;
					};
				}
			}
		}
		
		// otherwise, if a referenced VM is removed or there is a library
		// change in a referenced VM, a build is required 
		List futureVMs = curr.getVMList();
		for (int i = 0; i < projects.length; i++) {
			IJavaProject project = projects[i];
			IVMInstall prevVM = JavaRuntime.getVMInstall(project);
			if (prevVM != null) {
				int index  = futureVMs.indexOf(prevVM);
				if (index >= 0) {
					IVMInstall futureVM = (IVMInstall)futureVMs.get(index);
					// the VM still exists, see if the libraries changed
					LibraryLocation[] prevLibs = JavaRuntime.getLibraryLocations(prevVM);
					LibraryLocation[] newLibs = JavaRuntime.getLibraryLocations(futureVM);
					if (prevLibs.length == newLibs.length) {
						for (int j = 0; j < newLibs.length; j++) {
							LibraryLocation newLib = newLibs[j];
							LibraryLocation prevLib = prevLibs[j];
							String newPath = newLib.getSystemLibraryPath().toOSString();
							String prevPath = prevLib.getSystemLibraryPath().toOSString();
							if (!newPath.equalsIgnoreCase(prevPath)) {
								// different libs or ordering, a re-build is required
								return true;
							}
						} 
					} else {
						// different number of libraries, a re-build is required
						return true;
					}
				} else {
					// the VM no longer exists, a re-build will be required
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	private boolean isEqual(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return (a.equals(b));
	}
	
}
