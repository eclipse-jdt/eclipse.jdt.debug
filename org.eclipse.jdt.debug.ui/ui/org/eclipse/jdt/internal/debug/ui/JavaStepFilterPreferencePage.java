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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for Java step filtering, located at the node Java > Debug > Step Filtering
 *
 * @since 3.0
 */
public class JavaStepFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "org.eclipse.jdt.debug.ui.JavaStepFilterPreferencePage"; //$NON-NLS-1$

	//widgets
	private JavaFilterTable fStepFilterTable;
	private Button fUseStepFiltersButton;
	private Button fFilterSyntheticButton;
	private Button fFilterStaticButton;
	private Button fFilterGetterButton;
	private Button fFilterSetterButton;
	private Button fFilterConstructorButton;
	private Button fStepThruButton;

	/**
	 * Constructor
	 */
	public JavaStepFilterPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setTitle(DebugUIMessages.JavaStepFilterPreferencePage_title);
		setDescription(DebugUIMessages.JavaStepFilterPreferencePage_description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
	//The main composite
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		createStepFilterPreferences(composite);
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {}

	/**
	 * Create a group to contain the step filter related widgetry
	 */
	private void createStepFilterPreferences(Composite parent) {
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
		fUseStepFiltersButton = SWTFactory.createCheckButton(container,	DebugUIMessages.JavaStepFilterPreferencePage__Use_step_filters,	null, DebugUITools.isUseStepFilters(), 2);
		fUseStepFiltersButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setPageEnablement(fUseStepFiltersButton.getSelection());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			}
		);
		SWTFactory.createLabel(container, DebugUIMessages.JavaStepFilterPreferencePage_Defined_step_fi_lters__8, 2);
		initializeDialogUnits(container);
		this.fStepFilterTable = new JavaFilterTable(this, FilterManager.STEP_FILTERS, null);
		fStepFilterTable.createTable(container);

		createStepFilterCheckboxes(container);

		setPageEnablement(fUseStepFiltersButton.getSelection());
	}

	/**
	 * Enables or disables the widgets on the page, with the
	 * exception of <code>fUseStepFiltersButton</code> according
	 * to the passed boolean
	 * @param enabled the new enablement status of the page's widgets
	 * @since 3.2
	 */
	protected void setPageEnablement(boolean enabled) {
		fFilterConstructorButton.setEnabled(enabled);
		fStepThruButton.setEnabled(enabled);
		fFilterGetterButton.setEnabled(enabled);
		fFilterSetterButton.setEnabled(enabled);
		fFilterStaticButton.setEnabled(enabled);
		fFilterSyntheticButton.setEnabled(enabled);
		fStepFilterTable.setPageEnablement(enabled);
	}

	/**
	 * create the checked preferences for the page
	 * @param container the parent container
	 */
	private void createStepFilterCheckboxes(Composite container) {
		fFilterSyntheticButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_Filter_s_ynthetic_methods__requires_VM_support__17,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS), 2);
		fFilterStaticButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_Filter_static__initializers_18,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS), 2);
		fFilterConstructorButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_Filter_co_nstructors_19,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS), 2);
		fFilterGetterButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_Filter_getters,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_GETTERS), 2);
		fFilterSetterButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_Filter_setters,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_SETTERS), 2);
		fStepThruButton = SWTFactory.createCheckButton(container,
				DebugUIMessages.JavaStepFilterPreferencePage_0,
				null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS), 2);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		DebugUITools.setUseStepFilters(fUseStepFiltersButton.getSelection());
		IPreferenceStore store = getPreferenceStore();
		fStepFilterTable.performOk(store);
		store.setValue(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS, fFilterConstructorButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS, fFilterStaticButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_FILTER_GETTERS, fFilterGetterButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_FILTER_SETTERS, fFilterSetterButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS, fFilterSyntheticButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS, fStepThruButton.getSelection());
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// Cannot use DebugUITools.isUseStepFilters() as this not give the default value, no API from Platform to get the default value
		fUseStepFiltersButton.setSelection(false);
		setPageEnablement(false);
		fFilterSyntheticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS));
		fFilterStaticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS));
		fFilterConstructorButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS));
		fFilterGetterButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_GETTERS));
		fFilterSetterButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_SETTERS));
		fStepThruButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS));

		fStepFilterTable.performDefaults();
		super.performDefaults();
	}

}
