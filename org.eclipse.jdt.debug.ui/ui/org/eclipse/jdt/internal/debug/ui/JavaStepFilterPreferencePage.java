/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class JavaStepFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private static final String DEFAULT_NEW_FILTER_TEXT = ""; //$NON-NLS-1$

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
	
	private Text fEditorText;
	private String fInvalidEditorText= null;
	private TableEditor fTableEditor;
	private TableItem fNewTableItem;
	private Filter fNewStepFilter;
	private Label fTableLabel;
	
	private StepFilterContentProvider fStepFilterContentProvider;
	
	public JavaStepFilterPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.getString("JavaStepFilterPreferencePage.description")); //$NON-NLS-1$
	}

	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
		
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
		fTableLabel.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Defined_step_fi&lters__8")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fTableLabel.setLayoutData(gd);
		fTableLabel.setFont(font);
		
		// filter table
		fFilterTable= new Table(container, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fFilterTable.setLayoutData(gd);
		fFilterTable.setFont(font);
		
		TableLayout tableLayout= new TableLayout();
		ColumnLayoutData[] columnLayoutData= new ColumnLayoutData[1];
		columnLayoutData[0]= new ColumnWeightData(100);		
		tableLayout.addColumnData(columnLayoutData[0]);
		fFilterTable.setLayout(tableLayout);
		new TableColumn(fFilterTable, SWT.NONE);
		fFilterViewer = new CheckboxTableViewer(fFilterTable);
		fTableEditor = new TableEditor(fFilterTable);
		fFilterViewer.setLabelProvider(new FilterLabelProvider());
		fFilterViewer.setSorter(new FilterViewerSorter());
		fStepFilterContentProvider = new StepFilterContentProvider(fFilterViewer);
		fFilterViewer.setContentProvider(fStepFilterContentProvider);
		// input just needs to be non-null
		fFilterViewer.setInput(this);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		fFilterViewer.getTable().setLayoutData(gd);
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
		fFilterSyntheticButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Filter_s&ynthetic_methods_(requires_VM_support)_17")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fFilterSyntheticButton.setLayoutData(gd);
		fFilterSyntheticButton.setFont(font);
		
		// filter static checkbox
		fFilterStaticButton = new Button(container, SWT.CHECK);
		fFilterStaticButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Filter_static_&initializers_18")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fFilterStaticButton.setLayoutData(gd);
		fFilterStaticButton.setFont(font);
		
		// filter constructor checkbox
		fFilterConstructorButton = new Button(container, SWT.CHECK);
		fFilterConstructorButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Filter_co&nstructors_19")); //$NON-NLS-1$
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
		fAddFilterButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_&Filter_9")); //$NON-NLS-1$
		fAddFilterButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Key_in_the_name_of_a_new_step_filter_10")); //$NON-NLS-1$
		setButtonLayoutData(fAddFilterButton);
		fAddFilterButton.setFont(font);
		fAddFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				editFilter();
			}
		});
		
		// Add type button
		fAddTypeButton = new Button(buttonContainer, SWT.PUSH);
		fAddTypeButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_&Type..._11")); //$NON-NLS-1$
		fAddTypeButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Choose_a_Java_type_and_add_it_to_step_filters_12")); //$NON-NLS-1$
		fAddTypeButton.setFont(font);
		setButtonLayoutData(fAddTypeButton);
		fAddTypeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addType();
			}
		});
		
		// Add package button
		fAddPackageButton = new Button(buttonContainer, SWT.PUSH);
		fAddPackageButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_&Package..._13")); //$NON-NLS-1$
		fAddPackageButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Choose_a_package_and_add_it_to_step_filters_14")); //$NON-NLS-1$
		fAddPackageButton.setFont(font);
		setButtonLayoutData(fAddPackageButton);
		fAddPackageButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addPackage();
			}
		});
		
		// Remove button
		fRemoveFilterButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveFilterButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.&Remove_15")); //$NON-NLS-1$
		fRemoveFilterButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Remove_all_selected_step_filters_16")); //$NON-NLS-1$
		fRemoveFilterButton.setFont(font);
		setButtonLayoutData(fRemoveFilterButton);
		fRemoveFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeFilters();
			}
		});
		fRemoveFilterButton.setEnabled(false);
		
		fEnableAllButton= new Button(buttonContainer, SWT.PUSH);
		fEnableAllButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.&Enable_All_1")); //$NON-NLS-1$
		fEnableAllButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Enables_all_step_filters_2")); //$NON-NLS-1$
		fEnableAllButton.setFont(font);
		setButtonLayoutData(fEnableAllButton);
		fEnableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				checkAllFilters(true);
			}
		});

		fDisableAllButton= new Button(buttonContainer, SWT.PUSH);
		fDisableAllButton.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Disa&ble_All_3")); //$NON-NLS-1$
		fDisableAllButton.setToolTipText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Disables_all_step_filters_4")); //$NON-NLS-1$
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
	
	/**
	 * Create a new filter in the table (with the default 'new filter' value),
	 * then open up an in-place editor on it.
	 */
	private void editFilter() {
		// if a previous edit is still in progress, finish it
		if (fEditorText != null) {
			validateChangeAndCleanup();
		}
		
		fNewStepFilter = fStepFilterContentProvider.addFilter(DEFAULT_NEW_FILTER_TEXT, true);		
		fNewTableItem = fFilterTable.getItem(0);
		
		// create & configure Text widget for editor
		// Fix for bug 1766.  Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't.  Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		int textStyles = SWT.SINGLE | SWT.LEFT;
		if (!SWT.getPlatform().equals("motif")) {  //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		fEditorText = new Text(fFilterTable, textStyles);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fEditorText.setLayoutData(gd);
		fEditorText.setFont(fFilterTable.getFont());
		
		// set the editor
		fTableEditor.horizontalAlignment = SWT.LEFT;
		fTableEditor.grabHorizontal = true;
		fTableEditor.setEditor(fEditorText, fNewTableItem, 0);
		
		// get the editor ready to use
		fEditorText.setText(fNewStepFilter.getName());
		fEditorText.selectAll();
		setEditorListeners(fEditorText);
		fEditorText.setFocus();
	}
	
	private void setEditorListeners(Text text) {
		// CR means commit the changes, ESC means abort and don't commit
		text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {				
				if (event.character == SWT.CR) {
					if (fInvalidEditorText != null) {
						fEditorText.setText(fInvalidEditorText);
						fInvalidEditorText= null;
					} else {
						validateChangeAndCleanup();
					}
				} else if (event.character == SWT.ESC) {
					removeNewFilter();
					cleanupEditor();
				}
			}
		});
		// Consider loss of focus on the editor to mean the same as CR
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				if (fInvalidEditorText != null) {
					fEditorText.setText(fInvalidEditorText);
					fInvalidEditorText= null;
				} else {
					validateChangeAndCleanup();
				}
			}
		});
		// Consume traversal events from the text widget so that CR doesn't 
		// traverse away to dialog's default button.  Without this, hitting
		// CR in the text field closes the entire dialog.
		text.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}
	
	private void validateChangeAndCleanup() {
		String trimmedValue = fEditorText.getText().trim();
		// if the new value is blank, remove the filter
		if (trimmedValue.length() < 1) {
			removeNewFilter();
		}
		// if it's invalid, beep and leave sitting in the editor
		else if (!validateEditorInput(trimmedValue)) {
			fInvalidEditorText= trimmedValue;
			fEditorText.setText(DebugUIMessages.getString("JavaStepFilterPreferencePage.Invalid_step_filter._Return_to_continue;_escape_to_exit._1")); //$NON-NLS-1$
			getShell().getDisplay().beep();			
			return;
		// otherwise, commit the new value if not a duplicate
		} else {		
			
			Object[] filters= fStepFilterContentProvider.getElements(null);
			for (int i = 0; i < filters.length; i++) {
				Filter filter = (Filter)filters[i];
				if (filter.getName().equals(trimmedValue)) {
					removeNewFilter();
					cleanupEditor();
					return;
				}	
			}
			fNewTableItem.setText(trimmedValue);
			fNewStepFilter.setName(trimmedValue);
			fFilterViewer.refresh();
		}
		cleanupEditor();
	}
	
	/**
	 * Cleanup all widgetry & resources used by the in-place editing
	 */
	private void cleanupEditor() {
		if (fEditorText != null) {
			fNewStepFilter = null;
			fNewTableItem = null;
			fTableEditor.setEditor(null, null, 0);	
			fEditorText.dispose();
			fEditorText = null;
		}
	}
	
	private void removeNewFilter() {
		fStepFilterContentProvider.removeFilters(new Object[] {fNewStepFilter});
	}
	
	/**
	 * A valid step filter is simply one that is a valid Java identifier.
	 * and, as defined in the JDI spec, the regular expressions used for
	 * step filtering must be limited to exact matches or patterns that
	 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
	 * as corresponding to an existing type or package (and this is probably not
	 * even desirable).  
	 */
	private boolean validateEditorInput(String trimmedValue) {
		char firstChar= trimmedValue.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			if (!(firstChar == '*')) {
				return false;
			}
		}
		int length= trimmedValue.length();
		for (int i= 1; i < length; i++) {
			char c= trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1)) {
					continue;
				}
				if (c == '*' && i == (length - 1)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}
	
	private void addType() {
		Shell shell= getShell();
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, new ProgressMonitorDialog(shell),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_type_to_step_filters_20"); //$NON-NLS-1$
			String message= DebugUIMessages.getString("JavaStepFilterPreferencePage.Could_not_open_type_selection_dialog_for_step_filters_21"); //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}
	
		dialog.setTitle(DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_type_to_step_filters_20")); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.getString("JavaStepFilterPreferencePage.Select_a_type_to_filter_when_stepping_23")); //$NON-NLS-1$
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
			String title= DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_package_to_step_filters_24"); //$NON-NLS-1$
			String message= DebugUIMessages.getString("JavaStepFilterPreferencePage.Could_not_open_package_selection_dialog_for_step_filters_25"); //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;			
		}
	
		dialog.setTitle(DebugUIMessages.getString("JavaStepFilterPreferencePage.Add_package_to_step_filters_24")); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.getString("JavaStepFilterPreferencePage.Select_a_package_to_filter_when_stepping_27")); //$NON-NLS-1$
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
		
		public void toggleFilter(Filter filter) {
			boolean newState = !filter.isChecked();
			filter.setChecked(newState);
			fViewer.setChecked(filter, newState);
		}
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fFilters.toArray();
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
