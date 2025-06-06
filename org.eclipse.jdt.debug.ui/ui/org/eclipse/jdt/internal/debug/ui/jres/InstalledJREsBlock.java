/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.DetectVMInstallationsJob;
import org.eclipse.jdt.internal.launching.MacInstalledJREs;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A composite that displays installed JRE's in a table. JREs can be
 * added, removed, edited, and searched for.
 * <p>
 * This block implements ISelectionProvider - it sends selection change events
 * when the checked JRE in the table changes, or when the "use default" button
 * check state changes.
 * </p>
 */
public class InstalledJREsBlock implements IAddVMDialogRequestor, ISelectionProvider {

	/**
	 * A listener to know if another page has added a VM install
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237709"
	 * @since 3.6.300
	 */
	class InstallListener implements IVMInstallChangedListener {

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
		 */
		@Override
		public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			//do nothing, we do not want other bundles changing the default JRE this way
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
		 */
		@Override
		public void vmChanged(PropertyChangeEvent event) {
			//do nothing, we do not want other bundles making VM install edits without user interaction
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
		 */
		@Override
		public void vmAdded(IVMInstall vm) {
			if(!fVMs.contains(vm)) {
				fVMs.add(vm);
				doRefresh();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
		 */
		@Override
		public void vmRemoved(IVMInstall vm) {
			//do nothing, we do not want other bundles removing VM installs without user interaction
		}

		/**
		 * Refreshes the VM listing after a VM install notification, might not happen on the UI thread
		 */
		void doRefresh() {
			Display display = Display.getDefault();
			if(display.getThread().equals(Thread.currentThread())) {
				fVMList.refresh();
			}
			else {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						fVMList.refresh();
					}
				});
			}
		}
	}

	/**
	 * Listener for VM changes while the page is open, to ensure we have the latest set
	 * of {@link IVMInstall}s at all times
	 *
	 * @since 3.6.300
	 */
	IVMInstallChangedListener fListener = new InstallListener();

	/**
	 * This block's control
	 */
	private Composite fControl;

	/**
	 * VMs being displayed
	 */
	private final List<IVMInstall> fVMs = new ArrayList<>();

	/**
	 * The main list control
	 */
	private CheckboxTableViewer fVMList;

	// Action buttons
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;
	private Button fCopyButton;
	private Button fSearchButton;
	private Button detectJvmButton;

	// index of column used for sorting
	private int fSortColumn = 0;

	/**
	 * Selection listeners (checked JRE changes)
	 */
	private final ListenerList<ISelectionChangedListener> fSelectionListeners = new ListenerList<>();

	/**
	 * Previous selection
	 */
	private ISelection fPrevSelection = new StructuredSelection();

    private Table fTable;

	// Make sure that VMStandin ids are unique if multiple calls to System.currentTimeMillis()
	// happen very quickly
	private static String fgLastUsedID;

	/**
	 * VM install type id for OSX VMs
	 */
	public static final String MACOSX_VM_TYPE_ID = "org.eclipse.jdt.internal.launching.macosx.MacOSXType"; //$NON-NLS-1$

	private String fVMListTimeStamp;

	/**
	 * Content provider to show a list of JREs
	 */
	class JREsContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object input) {
			return fVMs.toArray();
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		@Override
		public void dispose() {
		}
	}

	/**
	 * Label provider for installed JREs table.
	 */
	class VMLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider, IColorProvider {

		Font bold = null;

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IVMInstall) {
				IVMInstall vm= (IVMInstall)element;
				switch(columnIndex) {
					case 0:
						if (JavaRuntime.isContributedVMInstall(vm.getId())) {
							return NLS.bind(JREMessages.InstalledJREsBlock_19, vm.getName());
						}
						if(fVMList.getChecked(element)) {
							return NLS.bind(JREMessages.InstalledJREsBlock_7, vm.getName());
						}
						return vm.getName();
					case 1:
						return vm.getInstallLocation().getAbsolutePath();
					case 2:
						return vm.getVMInstallType().getName();
				}
			}
			return element.toString();
		}

		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
			}
			return null;
		}

		@Override
		public Font getFont(Object element) {
			if(fVMList.getChecked(element)) {
				if (bold == null) {
					Font dialogFont = JFaceResources.getDialogFont();
					FontData[] fontData = dialogFont.getFontData();
					for (int i = 0; i < fontData.length; i++) {
						FontData data = fontData[i];
						data.setStyle(SWT.BOLD);
					}
					Display display = JDIDebugUIPlugin.getStandardDisplay();
					bold = new Font(display, fontData);
				}
				return bold;
			}
			return null;
		}

		@Override
		public void dispose() {
			if(bold != null) {
				bold.dispose();
			}
			super.dispose();
		}

		@Override
		public Color getForeground(Object element) {
			if (isUnmodifiable(element)) {
				Display display = Display.getCurrent();
				return display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			if (isUnmodifiable(element)) {
				Display display = Display.getCurrent();
				return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			}
			return null;
		}

		boolean isUnmodifiable(Object element) {
			if(element instanceof IVMInstall) {
				IVMInstall vm = (IVMInstall) element;
				return JavaRuntime.isContributedVMInstall(vm.getId());
			}
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		return new StructuredSelection(fVMList.getCheckedElements());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(fPrevSelection)) {
				fPrevSelection = selection;
				Object jre = ((IStructuredSelection)selection).getFirstElement();
				if (jre == null) {
					fVMList.setCheckedElements(new Object[0]);
				} else {
					fVMList.setCheckedElements(new Object[]{jre});
					fVMList.reveal(jre);
				}
				fVMList.refresh(true);
				fireSelectionChanged();
			}
		}
	}

	/**
	 * Creates this block's control in the given control.
	 *
	 * @param ancestor containing control
	 */
	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite parent= SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
		fControl = parent;

		SWTFactory.createLabel(parent, JREMessages.InstalledJREsBlock_15, 2);

		fTable= new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		fTable.setLayoutData(gd);
		fTable.setFont(font);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		TableColumn column = new TableColumn(fTable, SWT.NULL);
		column.setText(JREMessages.InstalledJREsBlock_0);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByName();
				fVMList.refresh(true);
			}
		});
		int defaultwidth = 350/3 +1;
		column.setWidth(defaultwidth);

		column = new TableColumn(fTable, SWT.NULL);
		column.setText(JREMessages.InstalledJREsBlock_1);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByLocation();
				fVMList.refresh(true);
			}
		});
		column.setWidth(defaultwidth);

		column = new TableColumn(fTable, SWT.NULL);
		column.setText(JREMessages.InstalledJREsBlock_2);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByType();
				fVMList.refresh(true);
			}
		});
		column.setWidth(defaultwidth);

		fVMList = new CheckboxTableViewer(fTable);
		fVMList.setLabelProvider(new VMLabelProvider());
		fVMList.setContentProvider(new JREsContentProvider());
		fVMList.setUseHashlookup(true);
		// by default, sort by name
		sortByName();

		fVMList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});

		fVMList.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setCheckedJRE((IVMInstall)event.getElement());
				} else {
					setCheckedJRE(null);
				}
			}
		});

		fVMList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent e) {
				if (!fVMList.getSelection().isEmpty()) {
					editVM();
				}
			}
		});
		fTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (fRemoveButton.isEnabled()){
						removeVMs();
					}
				}
			}
		});

		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);

		fAddButton = SWTFactory.createPushButton(buttons, JREMessages.InstalledJREsBlock_3, null);
		fAddButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				addVM();
			}
		});

		fEditButton= SWTFactory.createPushButton(buttons, JREMessages.InstalledJREsBlock_4, null);
		fEditButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				editVM();
			}
		});

		fCopyButton = SWTFactory.createPushButton(buttons, JREMessages.InstalledJREsBlock_16, null);
		fCopyButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				copyVM();
			}
		});

		fRemoveButton= SWTFactory.createPushButton(buttons, JREMessages.InstalledJREsBlock_5, null);
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				removeVMs();
			}
		});

		SWTFactory.createVerticalSpacer(parent, 1);

		fSearchButton = SWTFactory.createPushButton(buttons, JREMessages.InstalledJREsBlock_6, null);
		fSearchButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				search();
			}
		});
		detectJvmButton = SWTFactory.createPushButton(buttons, JREMessages.JREsPreferencePage_DetectJVMsNow, null);
		detectJvmButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				IRunnableWithProgress r = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						new DetectVMInstallationsJob().run(monitor);
					}
				};
				try {
					runWithStopButton(r);
				} catch (InvocationTargetException e) {
					JDIDebugUIPlugin.log(e);
				} catch (InterruptedException e) {
					// canceled
					return;
				}
				fillWithWorkspaceJREs();
				// fJREBlock.restoreColumnSettings(JDIDebugUIPlugin.getDefault().getDialogSettings(), IJavaDebugHelpContextIds.JRE_PREFERENCE_PAGE);
				// initDefaultVM();
				// fJREBlock.initializeTimeStamp();
			}
		});
		fillWithWorkspaceJREs();
		enableButtons();
		fAddButton.setEnabled(JavaRuntime.getVMInstallTypes().length > 0);

		JavaRuntime.addVMInstallChangedListener(fListener);
	}

	/**
	 * Adds a duplicate of the selected VM to the block
	 * @since 3.2
	 */
	protected void copyVM() {
        IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
        Iterator<IVMInstall> it = selection.iterator();

        ArrayList<VMStandin> newEntries = new ArrayList<>();
        while (it.hasNext()) {
            IVMInstall selectedVM = it.next();
            // duplicate & add VM
            VMStandin standin = new VMStandin(selectedVM, createUniqueId(selectedVM.getVMInstallType()));
            standin.setName(generateName(selectedVM.getName()));
			EditVMInstallWizard wizard = new EditVMInstallWizard(standin, fVMs.toArray(new IVMInstall[fVMs.size()]));
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			int dialogResult = dialog.open();
			if (dialogResult == Window.OK) {
				VMStandin result = wizard.getResult();
				if (result != null) {
					newEntries.add(result);
				}
			} else if (dialogResult == Window.CANCEL){
				// Canceling one wizard should cancel all subsequent wizards
				break;
			}
        }
        if (newEntries.size() > 0){
        	fVMs.addAll(newEntries);
        	fVMList.refresh();
        	fVMList.setSelection(new StructuredSelection(newEntries.toArray()));
        } else {
        	fVMList.setSelection(selection);
        }
        fVMList.refresh(true);
    }

	/**
	 * Compares the given name against current names and adds the appropriate numerical
	 * suffix to ensure that it is unique.
	 * @param name the name with which to ensure uniqueness
	 * @return the unique version of the given name
	 * @since 3.2
	 */
	public String generateName(String name){
            if (!isDuplicateName(name)) {
                return name;
            }
            if (name.matches(".*\\(\\d*\\)")) { //$NON-NLS-1$
                int start = name.lastIndexOf('(');
                int end = name.lastIndexOf(')');
                String stringInt = name.substring(start+1, end);
                int numericValue = Integer.parseInt(stringInt);
                String newName = name.substring(0, start+1) + (numericValue+1) + ")"; //$NON-NLS-1$
                return generateName(newName);
            }
            return generateName(name + " (1)"); //$NON-NLS-1$
        }

	/**
	 * Fire current selection
	 */
	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener listener : fSelectionListeners) {
			listener.selectionChanged(event);
		}
	}

	/**
	 * Sorts by VM type, and name within type.
	 */
	private void sortByType() {
		fVMList.setComparator(new ViewerComparator() {
			@Override
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

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 3;
	}

	/**
	 * Sorts by VM name.
	 */
	private void sortByName() {
		fVMList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IVMInstall) && (e2 instanceof IVMInstall)) {
					IVMInstall left= (IVMInstall)e1;
					IVMInstall right= (IVMInstall)e2;
					return left.getName().compareToIgnoreCase(right.getName());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 1;
	}

	/**
	 * Sorts by VM location.
	 */
	private void sortByLocation() {
		fVMList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if ((e1 instanceof IVMInstall) && (e2 instanceof IVMInstall)) {
					IVMInstall left= (IVMInstall)e1;
					IVMInstall right= (IVMInstall)e2;
					return left.getInstallLocation().getAbsolutePath().compareToIgnoreCase(right.getInstallLocation().getAbsolutePath());
				}
				return super.compare(viewer, e1, e2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		fSortColumn = 2;
	}

	/**
	 * Enables the buttons based on selected items counts in the viewer
	 */
	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		int selectionCount= selection.size();
		fEditButton.setEnabled(selectionCount == 1);
		fCopyButton.setEnabled(selectionCount > 0);
		if (selectionCount > 0 && selectionCount <= fVMList.getTable().getItemCount()) {
			Iterator<IVMInstall> iterator = selection.iterator();
			while (iterator.hasNext()) {
				IVMInstall install = iterator.next();
				if (JavaRuntime.isContributedVMInstall(install.getId())) {
					fRemoveButton.setEnabled(false);
					return;
				}
			}
			fRemoveButton.setEnabled(true);
		} else {
			fRemoveButton.setEnabled(false);
		}
	}

	/**
	 * Returns this block's control
	 *
	 * @return control
	 */
	public Control getControl() {
		return fControl;
	}

	/**
	 * Sets the JREs to be displayed in this block
	 *
	 * @param vms JREs to be displayed
	 */
	protected void setJREs(IVMInstall[] vms) {
		fVMs.clear();
		for (int i = 0; i < vms.length; i++) {
			fVMs.add(vms[i]);
		}
		fVMList.setInput(fVMs);
		fVMList.refresh();
	}

	/**
	 * Returns the JREs currently being displayed in this block
	 *
	 * @return JREs currently being displayed in this block
	 */
	public IVMInstall[] getJREs() {
		return fVMs.toArray(new IVMInstall[fVMs.size()]);
	}

	/**
	 * Bring up a wizard that lets the user create a new VM definition.
	 */
	private void addVM() {
		AddVMInstallWizard wizard = new AddVMInstallWizard(fVMs.toArray(new IVMInstall[fVMs.size()]));
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		if (dialog.open() == Window.OK) {
			VMStandin result = wizard.getResult();
			if (result != null) {
				fVMs.add(result);
				//refresh from model
				fVMList.refresh();
				if (fVMs.size() == 1) {
					// if this added JRE is the only JRE present, select it
					setCheckedJRE(result);
				}
				fVMList.setSelection(new StructuredSelection(result));
				//ensure labels are updated
				fVMList.refresh(true);
			}
		}
	}

	/**
	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
	 */
	@Override
	public void vmAdded(IVMInstall vm) {
		boolean makeselection = fVMs.size() < 1;
		fVMs.add(vm);
		//update from model
		fVMList.refresh();
		//update labels
		fVMList.refresh(true);
		//if we add a VM and none are selected, select one of them
		if(makeselection) {
			fireSelectionChanged();
		}
	}

	/**
	 * @see IAddVMDialogRequestor#isDuplicateName(String)
	 */
	@Override
	public boolean isDuplicateName(String name) {
		for (int i= 0; i < fVMs.size(); i++) {
			IVMInstall vm = fVMs.get(i);
			if (vm.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs the edit VM action when the Edit... button is pressed
	 */
	private void editVM() {
		IStructuredSelection selection= (IStructuredSelection)fVMList.getSelection();
		VMStandin vm= (VMStandin)selection.getFirstElement();
		if (vm == null) {
			return;
		}
		if (JavaRuntime.isContributedVMInstall(vm.getId())) {
			VMDetailsDialog dialog= new VMDetailsDialog(getShell(), vm);
			dialog.open();
		} else {
			EditVMInstallWizard wizard = new EditVMInstallWizard(vm, fVMs.toArray(new IVMInstall[fVMs.size()]));
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			if (dialog.open() == Window.OK) {
				VMStandin result = wizard.getResult();
				if (result != null) {
					// replace with the edited VM
					int index = fVMs.indexOf(vm);
					fVMs.remove(index);
					fVMs.add(index, result);
					fVMList.setSelection(new StructuredSelection(result));
					fVMList.refresh(true);
				}
			}
		}
	}

	/**
	 * Performs the remove VM(s) action when the Remove... button is pressed
	 */
	private void removeVMs() {
		IStructuredSelection selection= (IStructuredSelection)fVMList.getSelection();
		IVMInstall[] vms = new IVMInstall[selection.size()];
		Iterator<IVMInstall> iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			vms[i] = iter.next();
			i++;
		}
		removeJREs(vms);
	}

	/**
	 * Removes the given VMs from the table.
	 */
	public void removeJREs(IVMInstall[] vms) {
		for (int i = 0; i < vms.length; i++) {
			fVMs.remove(vms[i]);
		}
		fVMList.refresh();
		IStructuredSelection curr = (IStructuredSelection) getSelection();
		IVMInstall[] installs = getJREs();
		if(installs.length < 1) {
			fPrevSelection = null;
		}
		if (curr.size() == 0 && installs.length == 1) {
			// pick a default VM automatically
			setSelection(new StructuredSelection(installs[0]));
		} else {
			fireSelectionChanged();
		}
		fVMList.refresh(true);
	}

	/**
	 * Search for installed VMs in the file system
	 */
	protected void search() {
		// choose a root directory for the search
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
		dialog.setMessage(JREMessages.InstalledJREsBlock_9);
		dialog.setText(JREMessages.InstalledJREsBlock_10);

		String path = null;
		if (Platform.OS.isMac()) {
			String MAC_JAVA_SEARCH_PATH = "/Library/Java/JavaVirtualMachines"; //$NON-NLS-1$
			dialog.setFilterPath(MAC_JAVA_SEARCH_PATH);
			path = dialog.open();
			if (MAC_JAVA_SEARCH_PATH.equals(path)) {
				doMacSearch();
				return;
			}
		} else {
			path = dialog.open();
		}
		if (path == null) {
			return;
		}

		// ignore installed locations
		final Set<File> exstingLocations = new HashSet<>();
		for (IVMInstall vm : fVMs) {
			exstingLocations.add(vm.getInstallLocation());
		}

		// search
		final File rootDir = new File(path);
		final List<File> locations = new ArrayList<>();
		final List<IVMInstallType> types = new ArrayList<>();

		IRunnableWithProgress r = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(JREMessages.InstalledJREsBlock_11, IProgressMonitor.UNKNOWN);
				search(rootDir, locations, types, exstingLocations, monitor);
				monitor.done();
			}
		};

		try {
			runWithStopButton(r);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			// canceled
			return;
		}

		if (locations.isEmpty()) {
			String messagePath = path.replace("&", "&&"); // @see bug 29855  //$NON-NLS-1$//$NON-NLS-2$
			MessageDialog.openInformation(getShell(), JREMessages.InstalledJREsBlock_12, NLS.bind(JREMessages.InstalledJREsBlock_13, messagePath)); //
		} else {
			Iterator<IVMInstallType> iter2 = types.iterator();
			for(File location: locations) {
				IVMInstallType type = iter2.next();
				AbstractVMInstall vm = new VMStandin(type, createUniqueId(type));
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
					vm.setVMArgs(abs.getDefaultVMArguments(location));
				}
				vmAdded(vm);
			}
		}
	}

	private void runWithStopButton(IRunnableWithProgress r) throws InvocationTargetException, InterruptedException {
		ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell()) {
			/*
			 * Overridden createCancelButton to replace Cancel label with Stop label More accurately reflects action taken when button pressed. Bug
			 * [162902]
			 */
			@Override
			protected void createCancelButton(Composite parent) {
				cancel = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.STOP_LABEL, true);
				if (arrowCursor == null) {
					arrowCursor = new Cursor(cancel.getDisplay(), SWT.CURSOR_ARROW);
				}
				cancel.setCursor(arrowCursor);
				setOperationCancelButtonEnabled(enableCancelButton);
			}
		};
		progress.run(true, true, r);
	}

	/**
	 * Calls out to {@link MacVMSearch} to find all installed JREs in the standard
	 * Mac OS location
	 */
	private void doMacSearch() {
		final List<VMStandin> added = new ArrayList<>();
		IRunnableWithProgress r = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				Set<String> exists = new HashSet<>();
				for (IVMInstall vm : fVMs) {
					exists.add(vm.getId());
				}
				SubMonitor localmonitor = SubMonitor.convert(monitor, JREMessages.MacVMSearch_0, 5);
				VMStandin[] standins = null;
				try {
					standins = MacInstalledJREs.getInstalledJREs(localmonitor);
					for (int i = 0; i < standins.length; i++) {
						if (!exists.contains(standins[i].getId())) {
							added.add(standins[i]);
						}
					}
				}
				catch(CoreException ce) {
					JDIDebugUIPlugin.log(ce);
				}
				monitor.done();
			}
		};

		try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
            progress.run(true, true, r);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			// canceled
			return;
		}
		for(VMStandin vm: added) {
			vmAdded(vm);
		}
	}

	protected Shell getShell() {
		return getControl().getShell();
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
	 */
	protected void search(File directory, List<File> found, List<IVMInstallType> types, Set<File> ignore, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		String[] fileNames = directory.list();
		if (fileNames == null) {
			return; // not a directory
		}
		List<String> names = new ArrayList<>();
		names.add(null); // self
		names.addAll(List.of(fileNames));
		List<File> subDirs = new ArrayList<>();
		for (String name : names) {
			if (monitor.isCanceled()) {
				return;
			}
			File file = name == null ? directory : new File(directory, name);
			monitor.subTask(NLS.bind(JREMessages.InstalledJREsBlock_14, Integer.toString(found.size()),
					file.toPath().normalize().toAbsolutePath().toString().replace("&", "&&") )); // @see bug 29855 //$NON-NLS-1$ //$NON-NLS-2$
			IVMInstallType[] vmTypes = JavaRuntime.getVMInstallTypes();
			if (file.isDirectory()) {
				if (ignore.add(file)) {
					boolean validLocation = false;

					// Take the first VM install type that claims the location as a
					// valid VM install.  VM install types should be smart enough to not
					// claim another type's VM, but just in case...
					for (int j = 0; j < vmTypes.length; j++) {
						if (monitor.isCanceled()) {
							return;
						}
						IVMInstallType type = vmTypes[j];
						IStatus status = type.validateInstallLocation(file);
						if (status.isOK()) {
							String filePath = file.getPath();
							int index = filePath.lastIndexOf(File.separatorChar);
							File newFile = file;
							// remove bin folder from install location as java executables are found only under bin for Java 9 and above
							if (index > 0 && filePath.substring(index + 1).equals("bin")) { //$NON-NLS-1$
								newFile = new File(filePath.substring(0, index));
							}
							found.add(newFile);
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
			File subDir = subDirs.remove(0);
			search(subDir, found, types, ignore, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}

	}

	/**
	 * Sets the checked JRE, possible <code>null</code>
	 *
	 * @param vm JRE or <code>null</code>
	 */
	public void setCheckedJRE(IVMInstall vm) {
		if (vm == null) {
			setSelection(new StructuredSelection());
		} else {
			setSelection(new StructuredSelection(vm));
		}
	}

	/**
	 * Returns the checked JRE or <code>null</code> if none.
	 *
	 * @return the checked JRE or <code>null</code> if none
	 */
	public IVMInstall getCheckedJRE() {
		Object[] objects = fVMList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (IVMInstall)objects[0];
	}

	/**
	 * Persist table settings into the give dialog store, prefixed
	 * with the given key.
	 *
	 * @param settings dialog store
	 * @param qualifier key qualifier
	 */
	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
        int columnCount = fTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			settings.put(qualifier + ".columnWidth" + i, fTable.getColumn(i).getWidth());	 //$NON-NLS-1$
		}
		settings.put(qualifier + ".sortColumn", fSortColumn); //$NON-NLS-1$
	}

	/**
	 * Restore table settings from the given dialog store using the
	 * given key.
	 *
	 * @param settings dialog settings store
	 * @param qualifier key to restore settings from
	 */
	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
		fVMList.getTable().layout(true);
        restoreColumnWidths(settings, qualifier);
		try {
			fSortColumn = settings.getInt(qualifier + ".sortColumn"); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			fSortColumn = 1;
		}
		switch (fSortColumn) {
			case 1:
				sortByName();
				break;
			case 2:
				sortByLocation();
				break;
			case 3:
				sortByType();
				break;
		}
	}

	/**
	 * Restores the column widths from dialog settings
	 */
	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
        int columnCount = fTable.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            int width = -1;
            try {
                width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
            } catch (NumberFormatException e) {}

            if ((width <= 0) || (i == fTable.getColumnCount() - 1)) {
                fTable.getColumn(i).pack();
            } else {
                fTable.getColumn(i).setWidth(width);
            }
        }
	}

	/**
	 * Populates the JRE table with existing JREs defined in the workspace.
	 */
	protected void fillWithWorkspaceJREs() {
		// fill with JREs
		List<VMStandin> standins = new ArrayList<>();
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				standins.add(new VMStandin(install));
			}
		}
		setJREs(standins.toArray(new IVMInstall[standins.size()]));
	}

	/**
	 * Initializes time stamp with current JRE page details
	 */
	void initializeTimeStamp() {
		fVMListTimeStamp = getEncodedVMInstalls();
	}

	/**
	 * Disposes the block and any listeners
	 *
	 * @since 3.6.300
	 */
	public void dispose() {
		JavaRuntime.removeVMInstallChangedListener(fListener);
	}


	private StringBuilder appendVMAttributes(IVMInstall vmInstall, StringBuilder buf) {
		if (vmInstall != null) {
			String str = vmInstall.getName();
			buf.append('[').append(str.length()).append(']').append(str);
			str = vmInstall.getVMInstallType().getName();
			buf.append('[').append(str.length()).append(']').append(str);
			if (vmInstall.getVMArguments() != null && vmInstall.getVMArguments().length > 0) {
				buf.append('[').append(vmInstall.getVMArguments().length).append(']');
				for (int i = 0; i < vmInstall.getVMArguments().length; i++) {
					str = vmInstall.getVMArguments()[i];
					buf.append('[').append(str.length()).append(']').append(str);
				}
			}
			str = vmInstall.getInstallLocation().getAbsolutePath();
			buf.append('[').append(str.length()).append(']').append(str).append(';');
		} else {
			buf.append('[').append(']').append(';');
		}
		return buf;
	}

	private String getEncodedVMInstalls() {
		StringBuilder buf = new StringBuilder();
		IVMInstall vmInstall = getCheckedJRE();

		int nElements = fVMs.size();
		buf.append('[').append(nElements).append(']');
		for (int i = 0; i < nElements; i++) {
			IVMInstall elem = fVMs.get(i);
			if (elem == vmInstall)
			 {
				buf.append('[').append("defaultVM").append(']'); //$NON-NLS-1$
			}
			appendVMAttributes(elem, buf);
		}
		return buf.toString();
	}

	/**
	 * Checks if JRE block has changed.
	 *
	 * @return <code>true</code> if JRE block has changed, <code>false</code> otherwise
	 */
	public boolean hasChangesInDialog() {
		String currSettings = getEncodedVMInstalls();
		return !currSettings.equals(fVMListTimeStamp);
	}
}
