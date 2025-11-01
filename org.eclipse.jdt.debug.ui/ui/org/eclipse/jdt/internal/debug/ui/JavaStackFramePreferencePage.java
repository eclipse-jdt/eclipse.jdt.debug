/*******************************************************************************
 * Copyright (c) 2022, 2025 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.ButtonLabel;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.DialogLabels;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterTableConfig;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for Java stack frame categorization, located at the node Java > Debug > Stack Frames
 *
 * @since 3.15
 */
public class JavaStackFramePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static class ButtonWrapper<A> {
		private final Button button;
		protected final A key;

		ButtonWrapper(Composite container, String label, A key, boolean checked, Consumer<Boolean> stateChangeConsumer) {
			this.button = SWTFactory.createCheckButton(container, label, null, checked, 2);
			this.key = key;
			if (stateChangeConsumer != null) {
				widgetSelected(stateChangeConsumer);
			}
		}

		void setChecked(boolean checked) {
			button.setSelection(checked);
		}

		boolean isChecked() {
			return button.getSelection();
		}

		void setEnabled(boolean enabled) {
			this.button.setEnabled(enabled);
		}

		ButtonWrapper<A> widgetSelected(Consumer<Boolean> consumer) {
			this.button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					consumer.accept(button.getSelection());
				}
			});
			return this;
		}

	}

	private static class PreferenceButton extends ButtonWrapper<String> {

		PreferenceButton(Composite container, String label, IPreferenceStore store, String flag, Consumer<Boolean> stateChangeConsumer) {
			super(container, label, flag, store.getBoolean(flag), stateChangeConsumer);
		}

		boolean performDefault(IPreferenceStore store) {
			var defValue = store.getBoolean(key);
			setChecked(defValue);
			return defValue;
		}

		void performOk(IPreferenceStore store) {
			store.setValue(key, isChecked());
		}
	}

	private static class CategoryButton extends ButtonWrapper<Category> {

		CategoryButton(Composite container, String label, StackFrameCategorizer categorizer, Category category, Consumer<Boolean> stateChangeConsumer) {
			super(container, label, category, categorizer.isEnabled(category), stateChangeConsumer);
		}

		boolean performDefault(StackFrameCategorizer store) {
			var defValue = store.isEnabled(key);
			setChecked(defValue);
			return defValue;
		}

		void performOk(StackFrameCategorizer store) {
			store.setEnabled(key, isChecked());
		}

	}
	public static final String PAGE_ID = "org.eclipse.jdt.debug.ui.JavaStackFramePreferencePage"; //$NON-NLS-1$

	//widgets
	private PreferenceButton fCollapseStackFrames;
	private List<CategoryButton> categoryButtons;
	private CategoryButton fEnablePlatformButton;
	private CategoryButton fEnableCustomButton;
	private JavaFilterTable fPlatformStackFilterTable;
	private JavaFilterTable fCustomStackFilterTable;
	private StackFrameCategorizer categorizer;

	public JavaStackFramePreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setTitle(DebugUIMessages.JavaStackFramesPreferencePage_title);
		setDescription(DebugUIMessages.JavaStackFramesPreferencePage_description);
		this.categorizer = JDIDebugUIPlugin.getDefault().getStackFrameCategorizer();
		categoryButtons = new ArrayList<>();
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STACK_FRAMES_PREFERENCE_PAGE);
		// The main composite
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		createStepFilterPreferences(composite);
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {}

	/**
	 * Create a group to contain the step filter related widgets
	 */
	private void createStepFilterPreferences(Composite parent) {
		var store = getPreferenceStore();
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);

		fCollapseStackFrames = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage__Collapse_stack_frames, store, IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES, this::updateCheckboxes);
		initializeDialogUnits(container);

		this.fEnableCustomButton = new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_custom, categorizer, StackFrameCategorizer.CATEGORY_CUSTOM_FILTERED, //
				selected -> fCustomStackFilterTable.setEnabled(selected && isCategoryHandlingEnabled()));
		categoryButtons.add(this.fEnableCustomButton);

		fCustomStackFilterTable = new JavaFilterTable(new JavaFilterTable.FilterStorage() {

			@Override
			public Filter[] getStoredFilters(boolean defaults) {
				return combineFilterLists(categorizer.getActiveCustomStackFilter(), categorizer.getInactiveCustomStackFilter());
			}

			@Override
			public void setStoredFilters(IPreferenceStore store, Filter[] filters) {
				categorizer.setCustomFilters(filters);
			}

		},
				new FilterTableConfig() // configuration for the table
					.setAddFilter(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Filter))
					.setAddPackage(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Package))
					.setAddType(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Type))
					.setRemove(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Remove))
					.setSelectAll(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Enable_All))
					.setDeselectAll(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Disable_All))
					.setAddPackageDialog(new DialogLabels(DebugUIMessages.JavaStackFramesPreferencePage_Add_package_for_stack_filters, DebugUIMessages.JavaStackFramesPreferencePage_Select_a_package_for_stack_filter))
					.setAddTypeDialog(new DialogLabels(DebugUIMessages.JavaStackFramesPreferencePage_Add_type_for_stack_filters, DebugUIMessages.JavaStackFramesPreferencePage_Select_a_package_for_stack_filter))
					.setLabelText(DebugUIMessages.JavaStackFramesPreferencePage_Defined_custom_stack_frame_filters)
					.setHelpContextId(IJavaDebugHelpContextIds.JAVA_STACK_FRAMES_PREFERENCE_PAGE));
		fCustomStackFilterTable.createTable(container);

		categoryButtons.add(new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_synthetic, categorizer, StackFrameCategorizer.CATEGORY_SYNTHETIC, null));

		this.fEnablePlatformButton = new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_platform, categorizer, StackFrameCategorizer.CATEGORY_PLATFORM, //
				selected -> fPlatformStackFilterTable.setEnabled(selected && isCategoryHandlingEnabled()));
		categoryButtons.add(fEnablePlatformButton);

		fPlatformStackFilterTable = new JavaFilterTable(new JavaFilterTable.FilterStorage() {
			@Override
			public Filter[] getStoredFilters(boolean defaults) {
				return combineFilterLists(categorizer.getActivePlatformStackFilter(), categorizer.getInactivePlatformStackFilter());
			}

			@Override
			public void setStoredFilters(IPreferenceStore store, Filter[] filters) {
				categorizer.setPlatformFilters(filters);
			}

		},
				new FilterTableConfig()
					.setAddFilter(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Filter))
					.setAddPackage(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Package))
					.setAddType(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Add__Type))
					.setRemove(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Remove))
					.setSelectAll(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Enable_All))
					.setDeselectAll(new ButtonLabel(DebugUIMessages.JavaStackFramesPreferencePage_Disable_All))
					.setAddPackageDialog(new DialogLabels(DebugUIMessages.JavaStackFramesPreferencePage_Add_package_for_stack_filters,
							DebugUIMessages.JavaStackFramesPreferencePage_Select_a_package_for_stack_filter))
					.setAddTypeDialog(new DialogLabels(DebugUIMessages.JavaStackFramesPreferencePage_Add_type_for_stack_filters,
							DebugUIMessages.JavaStackFramesPreferencePage_Select_a_package_for_stack_filter))
					.setLabelText(DebugUIMessages.JavaStackFramesPreferencePage_Defined_stack_frame_filters_for_platform)
					.setHelpContextId(IJavaDebugHelpContextIds.JAVA_STACK_FRAMES_PREFERENCE_PAGE)
		);
		fPlatformStackFilterTable.createTable(container);

		categoryButtons.add(new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_test, categorizer, StackFrameCategorizer.CATEGORY_TEST, null));
		categoryButtons.add(new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_production, categorizer, StackFrameCategorizer.CATEGORY_PRODUCTION, null));
		categoryButtons.add(new CategoryButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_library, categorizer, StackFrameCategorizer.CATEGORY_LIBRARY, null));

		setPageEnablement(isCategoryHandlingEnabled());
	}

	private static Filter[] combineFilterLists(String[] activefilters, String[] inactivefilters) {
		Filter[] filters = new Filter[activefilters.length + inactivefilters.length];
		for (int i = 0; i < activefilters.length; i++) {
			filters[i] = new Filter(activefilters[i], true);
		}
		for (int i = 0; i < inactivefilters.length; i++) {
			filters[i + activefilters.length] = new Filter(inactivefilters[i], false);
		}
		return filters;
	}

	protected void updateCheckboxes(@SuppressWarnings("unused") boolean flag) {
		setPageEnablement(isCategoryHandlingEnabled());
	}

	private boolean isCategoryHandlingEnabled() {
		return fCollapseStackFrames.isChecked();
	}

	/**
	 * Enables or disables the widgets on the page according to the passed boolean
	 *
	 * @param enabled
	 *            the new enablement status of the page's widgets
	 */
	protected void setPageEnablement(boolean enabled) {
		fPlatformStackFilterTable.setEnabled(enabled && fEnablePlatformButton.isChecked());
		fCustomStackFilterTable.setEnabled(enabled && fEnableCustomButton.isChecked());
		for (var categoryButton : categoryButtons) {
			categoryButton.setEnabled(enabled);
		}
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		fCollapseStackFrames.performOk(store);
		fPlatformStackFilterTable.performOk(store);
		fCustomStackFilterTable.performOk(store);
		for (var categoryButton : categoryButtons) {
			categoryButton.performOk(categorizer);
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		var store = getPreferenceStore();
		boolean enabled = fCollapseStackFrames.performDefault(store);

		for (var categoryButton : categoryButtons) {
			categoryButton.performDefault(categorizer);
		}

		setPageEnablement(enabled);

		fPlatformStackFilterTable.performDefaults();
		fCustomStackFilterTable.performDefaults();
		super.performDefaults();
	}


}
