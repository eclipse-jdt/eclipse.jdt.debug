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
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Component to manage a list of Java filters, where a filter can be a Java class name, or a package name, and every filter can be active or inactive,
 * and they are stored in the preference store.
 *
 */
class JavaFilterTable {
	/**
	 * Content provider for the table. Content consists of instances of StepFilter.
	 *
	 * @since 3.2
	 */
	class StepFilterContentProvider implements IStructuredContentProvider {
		public StepFilterContentProvider() {
			initTableState(false);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getAllFiltersFromTable();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}

	static class ButtonLabel {
		final String label;
		final String tooltip;

		ButtonLabel(String label, String tooltip) {
			this.label = label;
			this.tooltip = tooltip;
		}
	}

	static class Dialog {
		final String title;
		final String message;

		Dialog(String title, String message) {
			this.title = title;
			this.message = message;
		}
	}

	static class FilterTableLabels {
		ButtonLabel addFilter = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage_Add__Filter_9, DebugUIMessages.JavaStepFilterPreferencePage_Key_in_the_name_of_a_new_step_filter_10);
		ButtonLabel addType = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage_Add__Type____11, DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_Java_type_and_add_it_to_step_filters_12);
		ButtonLabel addPackage = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage_Add__Package____13, DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_package_and_add_it_to_step_filters_14);
		ButtonLabel remove = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage__Remove_15, DebugUIMessages.JavaStepFilterPreferencePage_Remove_all_selected_step_filters_16);
		ButtonLabel selectAll = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage__Select_All_1, DebugUIMessages.JavaStepFilterPreferencePage_Selects_all_step_filters_2);
		ButtonLabel deselectAll = new ButtonLabel(DebugUIMessages.JavaStepFilterPreferencePage_Deselect_All_3, DebugUIMessages.JavaStepFilterPreferencePage_Deselects_all_step_filters_4);
		Dialog addTypeDialog = new Dialog(DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20, DebugUIMessages.JavaStepFilterPreferencePage_Select_a_type_to_filter_when_stepping_23);
		Dialog errorAddTypeDialog = new Dialog(DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20, DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_type_selection_dialog_for_step_filters_21);
		Dialog addPackageDialog = new Dialog(DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24, DebugUIMessages.JavaStepFilterPreferencePage_Select_a_package_to_filter_when_stepping_27);
		Dialog errorAddPackageDialog = new Dialog(DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24, DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_package_selection_dialog_for_step_filters_25);
	}

	private final PreferencePage preferencePage;
	private final FilterManager fFilterManager;
	private final FilterTableLabels labels;
	private CheckboxTableViewer fTableViewer;
	private Button fAddPackageButton;
	private Button fAddTypeButton;
	private Button fRemoveFilterButton;
	private Button fAddFilterButton;
	private Button fSelectAllButton;
	private Button fDeselectAllButton;


	JavaFilterTable(PreferencePage preferencePage, FilterManager filterManager, FilterTableLabels labels) {
		this.preferencePage = preferencePage;
		this.fFilterManager = filterManager;
		this.labels = labels != null ? labels : new FilterTableLabels();
	}

	void createTable(Composite container) {
		createFilterTableViewer(container);
		createStepFilterButtons(container);
	}

	private void createFilterTableViewer(Composite container) {
		fTableViewer = CheckboxTableViewer.newCheckList(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fTableViewer.getTable().setFont(container.getFont());
		fTableViewer.setLabelProvider(new FilterLabelProvider());
		fTableViewer.setComparator(new FilterViewerComparator());
		fTableViewer.setContentProvider(new StepFilterContentProvider());
		fTableViewer.setInput(getAllStoredFilters(false));
		fTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				((Filter) event.getElement()).setChecked(event.getChecked());
			}
		});
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				fRemoveFilterButton.setEnabled(!selection.isEmpty());
			}
		});
		fTableViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				handleFilterViewerKeyPress(event);
			}
		});
	}

	/**
	 * Creates the button for the step filter options
	 * @param container the parent container
	 */
	private void createStepFilterButtons(Composite container) {
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
	//Add filter button
	fAddFilterButton = SWTFactory.createPushButton(buttonContainer, labels.addFilter.label, labels.addFilter.tooltip, null);
		fAddFilterButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				addFilter();
			}
		});
	//Add type button
	fAddTypeButton = SWTFactory.createPushButton(buttonContainer, labels.addType.label, labels.addFilter.tooltip, null);
		fAddTypeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				addType();
			}
		});
	//Add package button
	fAddPackageButton = SWTFactory.createPushButton(buttonContainer, labels.addPackage.label, labels.addPackage.tooltip, null);
		fAddPackageButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				addPackage();
			}
		});
	//Remove button
	fRemoveFilterButton = SWTFactory.createPushButton(buttonContainer, labels.remove.label, labels.remove.tooltip, null);
		fRemoveFilterButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				removeFilters();
			}
		});
		fRemoveFilterButton.setEnabled(false);

		Label separator= new Label(buttonContainer, SWT.NONE);
		separator.setVisible(false);
		gd = new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.heightHint= 4;
		separator.setLayoutData(gd);
	//Select All button
	fSelectAllButton = SWTFactory.createPushButton(buttonContainer, labels.selectAll.label, labels.selectAll.tooltip, null);
		fSelectAllButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				fTableViewer.setAllChecked(true);
			}
		});
	//De-Select All button
	fDeselectAllButton = SWTFactory.createPushButton(buttonContainer, labels.deselectAll.label, labels.deselectAll.tooltip, null);
		fDeselectAllButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				fTableViewer.setAllChecked(false);
			}
		});

	}

	/**
	 * handles the filter button being clicked
	 *
	 * @param event
	 *            the clicked event
	 */
	private void handleFilterViewerKeyPress(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			removeFilters();
		}
	}

	/**
	 * Removes the currently selected filters.
	 */
	protected void removeFilters() {
		fTableViewer.remove(((IStructuredSelection) fTableViewer.getSelection()).toArray());
	}

	/**
	 * Allows a new filter to be added to the listing
	 */
	private void addFilter() {
		Filter newfilter = CreateStepFilterDialog.showCreateStepFilterDialog(preferencePage.getShell(), getAllFiltersFromTable());
		if (newfilter != null) {
			fTableViewer.add(newfilter);
			fTableViewer.setChecked(newfilter, true);
			fTableViewer.refresh(newfilter);
		}
	}

	/**
	 * add a new type to the listing of available filters
	 */
	private void addType() {
		try {
			SelectionDialog dialog = JavaUI.createTypeDialog(preferencePage.getShell(), PlatformUI.getWorkbench().getProgressService(), SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
			dialog.setTitle(labels.addTypeDialog.title);
			dialog.setMessage(labels.addTypeDialog.message);
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] types = dialog.getResult();
				if (types != null && types.length > 0) {
					IType type = (IType) types[0];
					addFilter(type.getFullyQualifiedName(), true);
				}
			}
		} catch (JavaModelException jme) {
			ExceptionHandler.handle(jme, labels.errorAddTypeDialog.title, labels.errorAddTypeDialog.message);
		}
	}

	/**
	 * add a new package to the list of all available package filters
	 */
	private void addPackage() {
		try {
			ElementListSelectionDialog dialog = JDIDebugUIPlugin.createAllPackagesDialog(preferencePage.getShell(), null, false);
			dialog.setTitle(labels.addPackageDialog.title);
			dialog.setMessage(labels.addPackageDialog.message);
			dialog.setMultipleSelection(true);
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] packages = dialog.getResult();
				if (packages != null) {
					IJavaElement pkg = null;
					for (int i = 0; i < packages.length; i++) {
						pkg = (IJavaElement) packages[i];
						String filter = pkg.getElementName() + ".*"; //$NON-NLS-1$
						addFilter(filter, true);
					}
				}
			}

		} catch (JavaModelException jme) {
			ExceptionHandler.handle(jme, labels.errorAddPackageDialog.title, labels.errorAddPackageDialog.message);
		}
	}

	/**
	 * Enables or disables the widgets on the page, with the exception of <code>fUseStepFiltersButton</code> according to the passed boolean
	 *
	 * @param enabled
	 *            the new enablement status of the page's widgets
	 * @since 3.2
	 */
	protected void setPageEnablement(boolean enabled) {
		fAddFilterButton.setEnabled(enabled);
		fAddPackageButton.setEnabled(enabled);
		fAddTypeButton.setEnabled(enabled);
		fDeselectAllButton.setEnabled(enabled);
		fSelectAllButton.setEnabled(enabled);
		fTableViewer.getTable().setEnabled(enabled);
		fRemoveFilterButton.setEnabled(enabled & !fTableViewer.getSelection().isEmpty());
	}

	/**
	 * initializes the checked state of the filters when the dialog opens
	 *
	 * @since 3.2
	 */
	private void initTableState(boolean defaults) {
		Filter[] filters = getAllStoredFilters(defaults);
		for (int i = 0; i < filters.length; i++) {
			fTableViewer.add(filters[i]);
			fTableViewer.setChecked(filters[i], filters[i].isChecked());
		}
	}

	/**
	 * Returns all of the committed filters
	 *
	 * @return an array of committed filters
	 * @since 3.2
	 */
	private Filter[] getAllStoredFilters(boolean defaults) {
		return fFilterManager.getAllStoredFilters(preferencePage.getPreferenceStore(), defaults);
	}

	/**
	 * adds a single filter to the viewer
	 * @param filter the new filter to add
	 * @param checked the checked state of the new filter
	 * @since 3.2
	 */
	protected void addFilter(String filter, boolean checked) {
		if(filter != null) {
			Filter f = new Filter(filter, checked);
			fTableViewer.add(f);
			fTableViewer.setChecked(f, checked);
		}
	}

	/**
	 * returns all of the filters from the table, this includes ones that have not yet been saved
	 *
	 * @return a possibly empty lits of filters fron the table
	 * @since 3.2
	 */
	protected Filter[] getAllFiltersFromTable() {
		TableItem[] items = fTableViewer.getTable().getItems();
		Filter[] filters = new Filter[items.length];
		for (int i = 0; i < items.length; i++) {
			filters[i] = (Filter) items[i].getData();
			filters[i].setChecked(items[i].getChecked());
		}
		return filters;
	}

	protected void performDefaults() {
		fTableViewer.getTable().removeAll();
		initTableState(true);
	}

	protected void performOk(IPreferenceStore store) {
		fFilterManager.save(store, getAllFiltersFromTable());
	}

}
