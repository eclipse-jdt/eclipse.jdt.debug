package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A preference page for collecting values that govern how Java variables are
 * presented.  The main part of this pref page is a grid of checkboxes.  The
 * rows represent access types (public, protected, etc.) and the columns
 * represent 'modes' (static, final, etc.).  A check mark means that the
 * corresponding access/mode pair will be shown.
 */
public class JavaVariablesFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * This listener responds to selection events for the +/- buttons at the top
	 * of each column and the beginning of each row of the filter grid.  The +
	 * buttons select all of the checkboxes in the corresponding row/column, and
	 * the - button deselects all of the checkboxes.
	 */
	private class PlusMinusButtonListener implements SelectionListener {
				
		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent evt) {
			Button button = (Button) evt.getSource();
			boolean isPlusButton = ((Boolean)button.getData(PLUS_KEY)).booleanValue();
			boolean isColumnButton = ((Boolean)button.getData(COLUMN_KEY)).booleanValue();
			int rowCol = ((Integer)button.getData(ROW_COL_KEY)).intValue();
			if (isColumnButton) {
				processCol(isPlusButton, rowCol);
			} else {
				processRow(isPlusButton, rowCol);
			}
		}
		
		private void processRow(boolean select, int row) {
			for (int j = 0; j < JDIDebugUIPlugin.fgAccessModifierNames.length; j++) {
				Button button = fCheckboxes[row][j];
				button.setSelection(select);
			}
		}
		
		private void processCol(boolean select, int col) {
			for (int i = 0; i < JDIDebugUIPlugin.fgModeModifierNames.length; i++) {
				Button button = fCheckboxes[i][col];
				button.setSelection(select);
			}			
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent evt) {
		}

	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	private static final int HORIZONTAL_GRID_SPACING = 10;
	private static final int VERTICAL_GRID_SPACING = 10;
	
	private static final String COLUMN_KEY = "column"; //$NON-NLS-1$
	private static final String PLUS_KEY = "plus"; //$NON-NLS-1$
	private static final String ROW_COL_KEY = "row_col"; //$NON-NLS-1$

	private String fPreferencePrefix;
	
	private StructuredViewer fViewer;
	
	private boolean fRunningInPreferenceDialog = false;
	
	public static final Image fgPlusSignImage = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_PLUS_SIGN);
	public static final Image fgMinusSignImage = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_MINUS_SIGN);
	
	private static Image[] fgAccessImages;
	
	private Button[][] fCheckboxes = new Button[JDIDebugUIPlugin.fgModeModifierNames.length][JDIDebugUIPlugin.fgAccessModifierNames.length];
	private Button fHexButton;
	private Button fCharButton;
	private Button fUnsignedButton;	
	
	static {
		fgAccessImages = new Image[JDIDebugUIPlugin.fgAccessModifierNames.length];
		fgAccessImages[0] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
		fgAccessImages[1] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);
		fgAccessImages[2] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PROTECTED);
		fgAccessImages[3] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PRIVATE);
		fgAccessImages[4] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);		
	}
	
	public JavaVariablesFilterPreferencePage() {
		this(null, JDIDebugUIPlugin.DEFAULT_VARIABLES_FILTER_PREFIX, true);
	}
	
	public JavaVariablesFilterPreferencePage(StructuredViewer viewer, String preferencePrefix) {
		this(viewer, preferencePrefix, false);
	}
	
	public JavaVariablesFilterPreferencePage(StructuredViewer viewer, String preferencePrefix, boolean runningInPrefDialog) {
		setViewer(viewer);
		fRunningInPreferenceDialog = runningInPrefDialog;
		if (isRunningInPreferenceDialog()) {
			setDescription(DebugUIMessages.getString("JavaVariablesFilterPreferencePage.Default_filter_settings_for_Java_variable_views_1")); //$NON-NLS-1$
		}
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setPreferencePrefix(preferencePrefix);		
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		setTitle(DebugUIMessages.getString("JavaVariablesFilterPreferencePage.Java_Variable_Filter_Preferences_1")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IJavaDebugHelpContextIds.JAVA_VARIABLES_FILTER_PREFERENCE_PAGE);

		GridData gd;
		Label label;
		
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		top.setLayout(topLayout);
		gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		top.setLayoutData(gd);
		top.setFont(parent.getFont());		

		createSpacer(top, 1);
				
		Group composite = new Group(top, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		int numColumns = JDIDebugUIPlugin.fgAccessModifierNames.length + 1;
		compositeLayout.numColumns = numColumns;
		compositeLayout.marginWidth = 0;
		compositeLayout.marginHeight = 0;
		composite.setLayout(compositeLayout);
		gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(gd);
		composite.setFont(top.getFont());
		composite.setText(DebugUIMessages.getString("JavaVariablesFilterPreferencePage.&Variable_Filters_1")); //$NON-NLS-1$
		
		// Create header images
		createSpacer(composite, 1);
		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
			label = new Label(composite, SWT.NONE);
			label.setImage(fgAccessImages[i]);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			label.setLayoutData(gd);
			label.setFont(composite.getFont());
		}		
				
		// Create column headers for the checkbox table
		createSpacer(composite, 1);
		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
			label = new Label(composite, SWT.NONE);
			label.setText(JDIDebugUIPlugin.fgAccessModifierNames[i]);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			label.setLayoutData(gd);
			label.setFont(composite.getFont());
		}

		// Create one listener for all +/- buttons
		PlusMinusButtonListener buttonListener = new PlusMinusButtonListener();
		
		// Create the +/- buttons for the columns
		createSpacer(composite, 1);
		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
			Composite buttonComp = createPlusMinusButtons(composite, true, buttonListener, i);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			buttonComp.setLayoutData(gd);
		}
		
		// Create the rows of the checkbox table
		for (int i = 0; i < JDIDebugUIPlugin.fgModeModifierNames.length; i++) {
			
			// Create a container for the row label and +/- buttons
			Composite rowComp = new Composite(composite, SWT.NONE);
			GridLayout rowLayout = new GridLayout();
			rowLayout.numColumns = 2;
			rowLayout.marginHeight = 0;
			rowLayout.marginWidth = 0;
			rowComp.setLayout(rowLayout);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
			rowComp.setLayoutData(gd);
			rowComp.setFont(composite.getFont());
			
			// Create the row label
			label = new Label(rowComp, SWT.NONE);
			label.setText(JDIDebugUIPlugin.fgAccessibleModeModifierNames[i]);
			label.setFont(rowComp.getFont());
			
			// Create the +/- buttons
			Composite buttonComp = createPlusMinusButtons(rowComp, false, buttonListener, i);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			buttonComp.setLayoutData(gd);
			
			// Create the checkboxes for the row
			for (int j = 0; j < JDIDebugUIPlugin.fgAccessModifierNames.length; j++) {
				String prefName = JDIDebugUIPlugin.generateVariableFilterPreferenceName(i, j, getPreferencePrefix());
				Button button = new Button(composite, SWT.CHECK);
				gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
				button.setLayoutData(gd);
				button.setData(prefName);
				fCheckboxes[i][j] = button;
			}
		}
		
		createSpacer(composite, numColumns);
		
		createSpacer(top, 1);
		
		// Create a group for the 3 primitive display options
		Group primitiveGroup = new Group(top, SWT.NONE);
		GridLayout primitiveLayout = new GridLayout();
		primitiveGroup.setLayout(primitiveLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		primitiveGroup.setLayoutData(gd);
		primitiveGroup.setFont(top.getFont());
		primitiveGroup.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Primitive_type_display_options_2")); //$NON-NLS-1$

		// Create the 3 primitive display checkboxes
		fHexButton = new Button(primitiveGroup, SWT.CHECK);
		fHexButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_&hexadecimal_values_(byte,_short,_char,_int,_long)_3")); //$NON-NLS-1$
		fCharButton = new Button(primitiveGroup, SWT.CHECK);
		fCharButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_ASCII_&character_values_(byte,_short,_int,_long)_4")); //$NON-NLS-1$
		fUnsignedButton = new Button(primitiveGroup, SWT.CHECK);
		fUnsignedButton.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Display_&unsigned_values_(byte)_5")); //$NON-NLS-1$
		
		createSpacer(top, 1);
		
		setValues();
		
		return composite;
	}
	
	/**
	 * Create +/- buttons, put them in a new Composite and return that
	 * Composite.  
	 */
	private Composite createPlusMinusButtons(Composite parent, boolean column, PlusMinusButtonListener buttonListener, int rowCol) {
		Composite buttonComp = new Composite(parent, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 2;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 5;
		buttonComp.setLayout(buttonLayout);
		
		Boolean columnBoolean = column ? Boolean.TRUE : Boolean.FALSE;
		Integer rowColInteger = new Integer(rowCol);
		
		Button plusButton = new Button(buttonComp, SWT.PUSH);
		plusButton.setImage(fgPlusSignImage);
		plusButton.setData(PLUS_KEY, Boolean.TRUE);
		plusButton.setData(COLUMN_KEY, columnBoolean);
		plusButton.setData(ROW_COL_KEY, rowColInteger);
		plusButton.addSelectionListener(buttonListener);
		plusButton.setFont(parent.getFont());
		
		Button minusButton = new Button(buttonComp, SWT.PUSH);
		minusButton.setImage(fgMinusSignImage);
		minusButton.setData(PLUS_KEY, Boolean.FALSE);
		minusButton.setData(COLUMN_KEY, columnBoolean);
		minusButton.setData(ROW_COL_KEY, rowColInteger);
		minusButton.addSelectionListener(buttonListener);		
		minusButton.setFont(parent.getFont());
	
		return buttonComp;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		storeValues();
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
		StructuredViewer viewer = getViewer();
		if (viewer != null) {
			applyFilterToViewer(viewer, getPreferencePrefix());
		}
		return true;
	}

	private void createSpacer(Composite parent, int numColumns) {
		Label label = new Label(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		label.setLayoutData(gd);
	}

	private void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}

	private StructuredViewer getViewer() {
		return fViewer;
	}

	private void setPreferencePrefix(String preferencePrefix) {
		fPreferencePrefix = preferencePrefix;
	}

	private String getPreferencePrefix() {
		return fPreferencePrefix;
	}
	
	private boolean isRunningInPreferenceDialog() {
		return fRunningInPreferenceDialog;
	}
	
	/**
	 * Read the current preference values out of the preference store and set
	 * the values on the corresponding widgets.
	 */
	private void setValues() {
		IPreferenceStore store = getPreferenceStore();		
		String prefix = getPreferencePrefix();
		
		for (int row = 0; row < JDIDebugUIPlugin.fgModeModifierNames.length; row++) {
			for (int col = 0; col < JDIDebugUIPlugin.fgAccessModifierNames.length; col++) {
				Button checkbox = fCheckboxes[row][col];
				String prefName = (String) checkbox.getData();
				boolean value = store.getBoolean(prefName);
				checkbox.setSelection(value);
			}
		}
		
		fHexButton.setSelection(store.getBoolean(JDIDebugUIPlugin.getShowHexPreferenceKey(prefix)));
		fCharButton.setSelection(store.getBoolean(JDIDebugUIPlugin.getShowCharPreferenceKey(prefix)));
		fUnsignedButton.setSelection(store.getBoolean(JDIDebugUIPlugin.getShowUnsignedPreferenceKey(prefix)));		
	}
	
	/**
	 * Read the values out of the widgets on this page and set the corresponding
	 * preference values in the preference store.
	 */
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		String prefix = getPreferencePrefix();

		for (int row = 0; row < JDIDebugUIPlugin.fgModeModifierNames.length; row++) {
			for (int col = 0; col < JDIDebugUIPlugin.fgAccessModifierNames.length; col++) {
				Button checkbox = fCheckboxes[row][col];
				String prefName = (String) checkbox.getData();
				store.setValue(prefName, checkbox.getSelection());
			}
		}
		
		store.setValue(JDIDebugUIPlugin.getShowHexPreferenceKey(prefix), fHexButton.getSelection());
		store.setValue(JDIDebugUIPlugin.getShowCharPreferenceKey(prefix), fCharButton.getSelection());
		store.setValue(JDIDebugUIPlugin.getShowUnsignedPreferenceKey(prefix), fUnsignedButton.getSelection());				
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}
	
	private void setDefaultValues() {
		IPreferenceStore store = getPreferenceStore();
		String prefix = getPreferencePrefix();

		for (int row = 0; row < JDIDebugUIPlugin.fgModeModifierNames.length; row++) {
			for (int col = 0; col < JDIDebugUIPlugin.fgAccessModifierNames.length; col++) {
				Button checkbox = fCheckboxes[row][col];
				boolean value = false;
				if (isRunningInPreferenceDialog()) {
					String prefName = (String) checkbox.getData();
					value = store.getDefaultBoolean(prefName);
				} else {
					String prefName = JDIDebugUIPlugin.generateVariableFilterPreferenceName(row, col, JDIDebugUIPlugin.DEFAULT_VARIABLES_FILTER_PREFIX);
					value = store.getBoolean(prefName);
				}
				checkbox.setSelection(value);
			}
		}
		
		boolean hexValue = false, charValue = false, unsignedValue = false;
		if (isRunningInPreferenceDialog()) {
			hexValue = store.getDefaultBoolean(JDIDebugUIPlugin.getShowHexPreferenceKey(prefix));
			charValue = store.getDefaultBoolean(JDIDebugUIPlugin.getShowCharPreferenceKey(prefix));
			unsignedValue = store.getDefaultBoolean(JDIDebugUIPlugin.getShowUnsignedPreferenceKey(prefix));						
		} else {
			hexValue = store.getBoolean(JDIDebugUIPlugin.getShowHexPreferenceKey(JDIDebugUIPlugin.DEFAULT_VARIABLES_FILTER_PREFIX));
			charValue = store.getBoolean(JDIDebugUIPlugin.getShowCharPreferenceKey(JDIDebugUIPlugin.DEFAULT_VARIABLES_FILTER_PREFIX));
			unsignedValue = store.getBoolean(JDIDebugUIPlugin.getShowUnsignedPreferenceKey(JDIDebugUIPlugin.DEFAULT_VARIABLES_FILTER_PREFIX));									
		}
		fHexButton.setSelection(hexValue);
		fCharButton.setSelection(charValue);
		fUnsignedButton.setSelection(unsignedValue);									
	}

	/**
	 * Apply a new filter to the viewer.  If one is already present, refresh it.
	 */
	public static void applyFilterToViewer(StructuredViewer viewer, String preferencePrefix) {
		JavaVariablesViewerFilter filter = retrieveViewerFilter(viewer);
		boolean refreshRequired = false;
		if (filter == null) {
			filter = new JavaVariablesViewerFilter(preferencePrefix);
			viewer.addFilter(filter);
		} else {
			filter.resetState();
			refreshRequired = true;
		}
		
		ILabelProvider labelProvider= (ILabelProvider) viewer.getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation) labelProvider;			
			IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
			boolean showHex = store.getBoolean(JDIDebugUIPlugin.getShowHexPreferenceKey(preferencePrefix));
			boolean showChar = store.getBoolean(JDIDebugUIPlugin.getShowCharPreferenceKey(preferencePrefix));
			boolean showUnsigned = store.getBoolean(JDIDebugUIPlugin.getShowUnsignedPreferenceKey(preferencePrefix));			
			debugLabelProvider.setAttribute(JDIModelPresentation.SHOW_HEX_VALUES, (showHex ? Boolean.TRUE : Boolean.FALSE));			
			debugLabelProvider.setAttribute(JDIModelPresentation.SHOW_CHAR_VALUES, (showChar ? Boolean.TRUE : Boolean.FALSE));			
			debugLabelProvider.setAttribute(JDIModelPresentation.SHOW_UNSIGNED_VALUES, (showUnsigned ? Boolean.TRUE : Boolean.FALSE));						
			refreshRequired = true;
		}
		
		if (refreshRequired) {
			viewer.refresh();
		}
	}

	/**
	 * Find & return the first instance of
	 * <code>JavaVariablesViewerFilter</code> that is registered as a filter on
	 * the viewer.
	 */
	public static JavaVariablesViewerFilter retrieveViewerFilter(StructuredViewer viewer) {
		ViewerFilter[] filters = viewer.getFilters();
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] instanceof JavaVariablesViewerFilter) {
				return (JavaVariablesViewerFilter) filters[i];
			}
		}
		return null;
	}	
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
