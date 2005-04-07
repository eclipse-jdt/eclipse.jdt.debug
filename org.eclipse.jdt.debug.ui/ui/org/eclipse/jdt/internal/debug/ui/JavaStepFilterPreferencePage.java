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
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public class JavaStepFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// Step filter widgets
	private CheckboxTableViewer fFilterViewer;
	private Table fFilterTable;
	private Button fAddPackageButton;
	private Button fAddTypeButton;
	private Button fRemoveFilterButton;
	private Button fAddFilterButton;
	private Button fFilterSyntheticButton;
	private Button fFilterStaticButton;
	private Button fFilterConstructorButton;
	
	private Button fEnableAllButton;
	private Button fDisableAllButton;
	
	private TableItem fNewTableItem;
	private Label fTableLabel;
	
	private StepFilterContentProvider fStepFilterContentProvider;
	
	public JavaStepFilterPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		// only used when the page is used programatically (edit step filters action)
		setTitle(DebugUIMessages.JavaStepFilterPreferencePage_title); //$NON-NLS-1$
		setDescription(DebugUIMessages.JavaStepFilterPreferencePage_description); //$NON-NLS-1$
	}

	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
		
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		createStepFilterPreferences(composite);

		return composite;	
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	private void handleFilterViewerKeyPress(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			removeFilters();
		}
	}
	
	/**
	 * Create a group to contain the step filter related widgetry
	 */
	private void createStepFilterPreferences(Composite parent) {
		Font font = parent.getFont();
		
		// top level container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		container.setFont(font);
				
		//table label
		fTableLabel= new Label(container, SWT.NONE);
		fTableLabel.setText(DebugUIMessages.JavaStepFilterPreferencePage_Defined_step_fi_lters__8); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fTableLabel.setLayoutData(gd);
		fTableLabel.setFont(font);
		
		fFilterViewer = CheckboxTableViewer.newCheckList(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fFilterViewer.setLabelProvider(new FilterLabelProvider());
		fFilterViewer.setSorter(new FilterViewerSorter());
		fStepFilterContentProvider = new StepFilterContentProvider(fFilterViewer);
		fFilterViewer.setContentProvider(fStepFilterContentProvider);
		// input just needs to be non-null
		fFilterViewer.setInput(this);
		
		// filter table
		fFilterTable= fFilterViewer.getTable();
		fFilterTable.setFont(font);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.widthHint=1; // just set it to something small and let ig "GRAB" the rest.
		fFilterTable.setLayoutData(gd);		
		
		TableLayout tableLayout= new TableLayout();
		ColumnWeightData columnLayoutData = new ColumnWeightData(100);	
		columnLayoutData.resizable=true;
		tableLayout.addColumnData(columnLayoutData);
		fFilterTable.setLayout(tableLayout);
				
		fFilterViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Filter filter = (Filter)event.getElement();
				fStepFilterContentProvider.toggleFilter(filter);
			}
		});
		fFilterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					fRemoveFilterButton.setEnabled(false);
				} else {
					fRemoveFilterButton.setEnabled(true);					
				}
			}
		});	
		fFilterViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				handleFilterViewerKeyPress(event);
			}
		});	
		
		createStepFilterButtons(container);
		createStepFilterCheckboxes(container);
	}
	
	private void createStepFilterCheckboxes(Composite container) {
		Font font = container.getFont();
		
		// filter synthetic checkbox
		fFilterSyntheticButton = new Button(container, SWT.CHECK);
		fFilterSyntheticButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Filter_s_ynthetic_methods__requires_VM_support__17); //$NON-NLS-1$
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fFilterSyntheticButton.setLayoutData(gd);
		fFilterSyntheticButton.setFont(font);
		
		// filter static checkbox
		fFilterStaticButton = new Button(container, SWT.CHECK);
		fFilterStaticButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Filter_static__initializers_18); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fFilterStaticButton.setLayoutData(gd);
		fFilterStaticButton.setFont(font);
		
		// filter constructor checkbox
		fFilterConstructorButton = new Button(container, SWT.CHECK);
		fFilterConstructorButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Filter_co_nstructors_19); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fFilterConstructorButton.setLayoutData(gd);
		fFilterConstructorButton.setFont(font);
		
		fFilterSyntheticButton.setSelection(getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS));
		fFilterStaticButton.setSelection(getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS));
		fFilterConstructorButton.setSelection(getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS));
	}
	
	private void createStepFilterButtons(Composite container) {
		Font font = container.getFont();
		initializeDialogUnits(container);
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
		
		// Add filter button
		fAddFilterButton = new Button(buttonContainer, SWT.PUSH);
		fAddFilterButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Add__Filter_9); //$NON-NLS-1$
		fAddFilterButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Key_in_the_name_of_a_new_step_filter_10); //$NON-NLS-1$
		setButtonLayoutData(fAddFilterButton);
		fAddFilterButton.setFont(font);
		fAddFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addFilter();
			}
		});
		
		// Add type button
		fAddTypeButton = new Button(buttonContainer, SWT.PUSH);
		fAddTypeButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Add__Type____11); //$NON-NLS-1$
		fAddTypeButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_Java_type_and_add_it_to_step_filters_12); //$NON-NLS-1$
		fAddTypeButton.setFont(font);
		setButtonLayoutData(fAddTypeButton);
		fAddTypeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addType();
			}
		});
		
		// Add package button
		fAddPackageButton = new Button(buttonContainer, SWT.PUSH);
		fAddPackageButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Add__Package____13); //$NON-NLS-1$
		fAddPackageButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_package_and_add_it_to_step_filters_14); //$NON-NLS-1$
		fAddPackageButton.setFont(font);
		setButtonLayoutData(fAddPackageButton);
		fAddPackageButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addPackage();
			}
		});
		
		// Remove button
		fRemoveFilterButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveFilterButton.setText(DebugUIMessages.JavaStepFilterPreferencePage__Remove_15); //$NON-NLS-1$
		fRemoveFilterButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Remove_all_selected_step_filters_16); //$NON-NLS-1$
		fRemoveFilterButton.setFont(font);
		setButtonLayoutData(fRemoveFilterButton);
		fRemoveFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeFilters();
			}
		});
		fRemoveFilterButton.setEnabled(false);
		
		// copied from ListDialogField.CreateSeparator()
		Label separator= new Label(buttonContainer, SWT.NONE);
		separator.setVisible(false);
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.heightHint= 4;
		separator.setLayoutData(gd);
				
		fEnableAllButton= new Button(buttonContainer, SWT.PUSH);
		fEnableAllButton.setText(DebugUIMessages.JavaStepFilterPreferencePage__Enable_All_1); //$NON-NLS-1$
		fEnableAllButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Enables_all_step_filters_2); //$NON-NLS-1$
		fEnableAllButton.setFont(font);
		setButtonLayoutData(fEnableAllButton);
		fEnableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				checkAllFilters(true);
			}
		});

		fDisableAllButton= new Button(buttonContainer, SWT.PUSH);
		fDisableAllButton.setText(DebugUIMessages.JavaStepFilterPreferencePage_Disa_ble_All_3); //$NON-NLS-1$
		fDisableAllButton.setToolTipText(DebugUIMessages.JavaStepFilterPreferencePage_Disables_all_step_filters_4); //$NON-NLS-1$
		fDisableAllButton.setFont(font);
		setButtonLayoutData(fDisableAllButton);
		fDisableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				checkAllFilters(false);
			}
		});
		
	}
		
	private void checkAllFilters(boolean check) {
		
		Object[] filters= fStepFilterContentProvider.getElements(null);
		for (int i= 0; i != filters.length; i++) {
			((Filter)filters[i]).setChecked(check);
		}		
			
		fFilterViewer.setAllChecked(check);
	}
	
	private void addFilter() {
		Shell shell = getShell();
		
		Filter[] existingFilters= (Filter[])fStepFilterContentProvider.getElements(null);
		Filter newFilter = CreateStepFilterDialog.showCreateStepFilterDialog(shell, existingFilters);
		if (newFilter != null)
			commitNewFilter(newFilter);
	}
	
	private void commitNewFilter(Filter newFilter) {
		String newFilterName = newFilter.getName();
		
	// if newFilter is a duplicate of existing, just set state of existing to checked.
		Object[] filters= fStepFilterContentProvider.getElements(null);
		for (int i = 0; i < filters.length; i++) {
			Filter filter = (Filter)filters[i];
			if (filter.getName().equals(newFilterName)) {
				fStepFilterContentProvider.setChecked(filter, true);
//				cleanupEditor();
				return;
			}	
		}
		fStepFilterContentProvider.addFilter(newFilterName, true);
		fNewTableItem = fFilterTable.getItem(0);
		fNewTableItem.setText(newFilterName);
		fFilterViewer.refresh();
//		cleanupEditor();
	}
	
	/**
	 * Cleanup all widgetry & resources used by the in-place editing
	 */
//	private void cleanupEditor() {
//		if (fEditorText != null) {
//			fNewTableItem = null;
//			fTableEditor.setEditor(null, null, 0);	
//			fEditorText.dispose();
//			fEditorText = null;
//		}
//	}
	
	private void addType() {
		Shell shell= getShell();
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20; //$NON-NLS-1$
			String message= DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_type_selection_dialog_for_step_filters_21; //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}
	
		dialog.setTitle(DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.JavaStepFilterPreferencePage_Select_a_type_to_filter_when_stepping_23); //$NON-NLS-1$
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type = (IType)types[0];
			fStepFilterContentProvider.addFilter(type.getFullyQualifiedName(), true);
		}		
	}
	
	private void addPackage() {
		Shell shell= getShell();
		ElementListSelectionDialog dialog = null;
		try {
			dialog = JDIDebugUIPlugin.createAllPackagesDialog(shell, null, false);
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24; //$NON-NLS-1$
			String message= DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_package_selection_dialog_for_step_filters_25; //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;			
		}
		
		if (dialog == null) {
			return;
		}
		
		dialog.setTitle(DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.JavaStepFilterPreferencePage_Select_a_package_to_filter_when_stepping_27); //$NON-NLS-1$
		dialog.setMultipleSelection(true);
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		
		Object[] packages= dialog.getResult();
		if (packages != null) {
			for (int i = 0; i < packages.length; i++) {
				IJavaElement pkg = (IJavaElement)packages[i];
				
				String filter = pkg.getElementName() + ".*"; //$NON-NLS-1$
				fStepFilterContentProvider.addFilter(filter, true);
			}
		}		
	}
	
	/**
	 * Removes the selected filters.
	 */
	private void removeFilters() {
		IStructuredSelection selection = (IStructuredSelection)fFilterViewer.getSelection();		
		fStepFilterContentProvider.removeFilters(selection.toArray());
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fStepFilterContentProvider.saveFilters();
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/**
	 * Sets the default preferences.
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();	
	}
	
	private void setDefaultValues() {
		fStepFilterContentProvider.setDefaults();
	}
	
	/**
	 * Returns a list of active step filters.
	 * 
	 * @return list
	 */
	protected List createActiveStepFiltersList() {
		String[] strings = JavaDebugOptionsManager.parseList(getPreferenceStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
		return Arrays.asList(strings);
	}
	
	/**
	 * Returns a list of active step filters.
	 * 
	 * @return list
	 */
	protected List createInactiveStepFiltersList() {
		String[] strings = JavaDebugOptionsManager.parseList(getPreferenceStore().getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
		return Arrays.asList(strings);
	}
	
	/**
	 * Returns a list of the default active step filters.
	 * 
	 * @return list
	 */
	protected List createDefaultActiveStepFiltersList() {
		String[] strings = JavaDebugOptionsManager.parseList(getPreferenceStore().getDefaultString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
		return Arrays.asList(strings);
	}
	
	/**
	 * Returns a list of the default active step filters.
	 * 
	 * @return list
	 */
	protected List createDefaultInactiveStepFiltersList() {
		String[] strings = JavaDebugOptionsManager.parseList(getPreferenceStore().getDefaultString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
		return Arrays.asList(strings);
	}
	
	protected void updateActions() {
		if (fEnableAllButton != null) {
			boolean enabled= fFilterViewer.getTable().getItemCount() > 0;
			fEnableAllButton.setEnabled(enabled);
			fDisableAllButton.setEnabled(enabled);
		}
	}	
	
	/**
	 * Content provider for the table.  Content consists of instances of StepFilter.
	 */	
	protected class StepFilterContentProvider implements IStructuredContentProvider {
		
		private CheckboxTableViewer fViewer;
		private List fFilters;
		
		public StepFilterContentProvider(CheckboxTableViewer viewer) {
			fViewer = viewer;
			List active = createActiveStepFiltersList();
			List inactive = createInactiveStepFiltersList();
			populateFilters(active, inactive);
			updateActions();
		}
		
		public void setDefaults() {
			fViewer.remove(fFilters.toArray());			
			List active = createDefaultActiveStepFiltersList();
			List inactive = createDefaultInactiveStepFiltersList();
			populateFilters(active, inactive);		
							
			fFilterSyntheticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS));
			fFilterStaticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS));
			fFilterConstructorButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS));
		}
		
		protected void populateFilters(List activeList, List inactiveList) {
			fFilters = new ArrayList(activeList.size() + inactiveList.size());
			populateList(activeList, true);
			populateList(inactiveList, false);
		}
		
		protected void populateList(List list, boolean checked) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String name = (String)iterator.next();
				addFilter(name, checked);
			}			
		}
		
		public Filter addFilter(String name, boolean checked) {
			Filter filter = new Filter(name, checked);
			if (!fFilters.contains(filter)) {
				fFilters.add(filter);
				fViewer.add(filter);
				fViewer.setChecked(filter, checked);
			}
			updateActions();
			return filter;
		}
		
		public void saveFilters() {
			
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS, fFilterConstructorButton.getSelection());
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS, fFilterStaticButton.getSelection());
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS, fFilterSyntheticButton.getSelection());
							
			List active = new ArrayList(fFilters.size());
			List inactive = new ArrayList(fFilters.size());
			Iterator iterator = fFilters.iterator();
			while (iterator.hasNext()) {
				Filter filter = (Filter)iterator.next();
				String name = filter.getName();
				if (filter.isChecked()) {
					active.add(name);
				} else {
					inactive.add(name);
				}
			}
			String pref = JavaDebugOptionsManager.serializeList((String[])active.toArray(new String[active.size()]));
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, pref);
			pref = JavaDebugOptionsManager.serializeList((String[])inactive.toArray(new String[inactive.size()]));
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, pref);
		}
		
		public void removeFilters(Object[] filters) {
			for (int i = 0; i < filters.length; i++) {
				Filter filter = (Filter)filters[i];
				fFilters.remove(filter);
			}
			fViewer.remove(filters);
			updateActions();
		}
		
		public void setChecked(Filter filter, boolean checked) {
			filter.setChecked(checked);
			fViewer.setChecked(filter, checked);
		}
		
		public void toggleFilter(Filter filter) {
			boolean newState = !filter.isChecked();
			filter.setChecked(newState);
			fViewer.setChecked(filter, newState);
		}
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fFilters.toArray(new Filter[0]);
		}
		
		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}		
	}
}
