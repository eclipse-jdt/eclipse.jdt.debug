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
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.ButtonLabel;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.DialogLabels;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterTableConfig;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for Java stack frame categorization, located at the node Java > Debug > Stack Frames
 *
 * @since 3.19
 */
public class JavaStackFramePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

	private static class CategoryColors {
		final IJavaStackFrame.Category category;
		final String key;
		Color fgColor;
		Color bgColor;

		CategoryColors(Category category, String key) {
			this.category = category;
			this.key = key;
		}

		Color getForegroundColor(StackFramePresentationProvider stackFramePresentationProvider) {
			if (fgColor != null) {
				return fgColor;
			}
			return stackFramePresentationProvider.getForegroundColor(category);
		}

		Color getBackgroundColor(StackFramePresentationProvider stackFramePresentationProvider) {
			if (bgColor != null) {
				return bgColor;
			}
			return stackFramePresentationProvider.getBackgroundColor(category);
		}

		void setToDefault() {
			this.fgColor = null;
			this.bgColor = null;
		}
	}

	private class StackFrameCategoryLabelProvider extends LabelProvider implements IColorProvider {

		@Override
		public String getText(Object element) {
			return ((CategoryColors) element).key;
		}

		@Override
		public Color getForeground(Object element) {
			return ((CategoryColors) element).getForegroundColor(stackFramePresentationProvider);
		}

		@Override
		public Color getBackground(Object element) {
			return ((CategoryColors) element).getBackgroundColor(stackFramePresentationProvider);
		}
	}

	private static class StackFrameCategoryContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return ((java.util.List<?>) inputElement).toArray();
		}
	}

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
	private PreferenceButton fColorizeStackFrames;
	private PreferenceButton fCollapseStackFrames;
	private List<CategoryButton> categoryButtons;
	private CategoryButton fEnablePlatformButton;
	private CategoryButton fEnableCustomButton;
	private JavaFilterTable fPlatformStackFilterTable;
	private JavaFilterTable fCustomStackFilterTable;
	private TableViewer fAppearanceList;
	private List<CategoryColors> colors;
	private ColorSelector fFgColorSelector;
	private ColorSelector fBgColorSelector;
	private StackFramePresentationProvider stackFramePresentationProvider;
	private StackFrameCategorizer categorizer;

	/**
	 * Constructor
	 */
	public JavaStackFramePreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setTitle(DebugUIMessages.JavaStackFramesPreferencePage_title);
		setDescription(DebugUIMessages.JavaStackFramesPreferencePage_description);
		stackFramePresentationProvider = new StackFramePresentationProvider(getPreferenceStore());
		this.categorizer = JDIDebugUIPlugin.getDefault().getStackFrameCategorizer();
		categoryButtons = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STACK_FRAMES_PREFERENCE_PAGE);
		// The main composite
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
	 * Create a group to contain the step filter related widgets
	 */
	private void createStepFilterPreferences(Composite parent) {
		var store = getPreferenceStore();
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
		fColorizeStackFrames = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage__Color_stack_frames, store, IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES, this::updateCheckboxes);

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

		createAppearanceList(container);

		setPageEnablement(isCategoryHandlingEnabled());
		initList();
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

	private void createAppearanceList(Composite container) {

		SWTFactory.createLabel(container, DebugUIMessages.JavaStackFramesPreferencePage_Appearance_of_stack_frames, 2);

		var editorComposite = new Composite(container, SWT.NONE);
		var layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		var gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		editorComposite.setLayoutData(gd);

		fAppearanceList = new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fAppearanceList.setLabelProvider(new StackFrameCategoryLabelProvider());
		fAppearanceList.setContentProvider(new StackFrameCategoryContentProvider());

		gd = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		gd.heightHint = convertHeightInCharsToPixels(8);
		fAppearanceList.getControl().setLayoutData(gd);
		fAppearanceList.addSelectionChangedListener(event -> {
			var selection = event.getStructuredSelection();
			var valid = !selection.isEmpty();
			fFgColorSelector.getButton().setEnabled(valid);
			fBgColorSelector.getButton().setEnabled(valid);
			if (valid) {
				CategoryColors category = (CategoryColors) selection.getFirstElement();
				var color = category.getForegroundColor(stackFramePresentationProvider);
				if (color != null) {
					fFgColorSelector.setColorValue(color.getRGB());
				}
				var bgColor = category.getBackgroundColor(stackFramePresentationProvider);
				if (bgColor != null) {
					fBgColorSelector.setColorValue(bgColor.getRGB());
				}
			}
		});


		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		SWTFactory.createLabel(stylesComposite, DebugUIMessages.JavaStackFramesPreferencePage_fg_color, 1);

		fFgColorSelector = createColorSelector(stylesComposite);
		fFgColorSelector.addListener(event -> {
			var selection = getSelected();
			if (selection != null) {
				selection.fgColor = toColor(event);
				fAppearanceList.update(selection, null);
			}
		});

		SWTFactory.createLabel(stylesComposite, DebugUIMessages.JavaStackFramesPreferencePage_bg_color, 1);

		fBgColorSelector = createColorSelector(stylesComposite);
		fBgColorSelector.addListener(event -> {
			var selection = getSelected();
			if (selection != null) {
				selection.bgColor = toColor(event);
				fAppearanceList.update(selection, null);
			}
		});

		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().addListener(this);
	}

	private CategoryColors getSelected() {
		var selection = fAppearanceList.getStructuredSelection().getFirstElement();
		if (selection instanceof CategoryColors) {
			return (CategoryColors) selection;
		}
		return null;
	}

	private Color toColor(PropertyChangeEvent event) {
		return new Color((RGB) event.getNewValue());
	}

	private ColorSelector createColorSelector(Composite stylesComposite) {
		var colorSelector = new ColorSelector(stylesComposite);
		Button button = colorSelector.getButton();
		var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		button.setLayoutData(gd);
		return colorSelector;
	}

	private void initList() {
		colors = List.of(
				new CategoryColors(StackFrameCategorizer.CATEGORY_CUSTOM_FILTERED, DebugUIMessages.JavaStackFramesPreferencePage_category_custom_filter), //
				new CategoryColors(StackFrameCategorizer.CATEGORY_SYNTHETIC, DebugUIMessages.JavaStackFramesPreferencePage_category_synthetic), //
				new CategoryColors(StackFrameCategorizer.CATEGORY_PLATFORM, DebugUIMessages.JavaStackFramesPreferencePage_category_platform), //
				new CategoryColors(StackFrameCategorizer.CATEGORY_TEST, DebugUIMessages.JavaStackFramesPreferencePage_category_test), //
				new CategoryColors(StackFrameCategorizer.CATEGORY_PRODUCTION, DebugUIMessages.JavaStackFramesPreferencePage_category_production), //
				new CategoryColors(StackFrameCategorizer.CATEGORY_LIBRARY, DebugUIMessages.JavaStackFramesPreferencePage_category_library) //
		);
		fAppearanceList.setInput(colors);
		fAppearanceList.setSelection(new StructuredSelection(colors.get(0)));
	}

	protected void updateCheckboxes(@SuppressWarnings("unused") boolean flag) {
		setPageEnablement(isCategoryHandlingEnabled());
	}

	private boolean isCategoryHandlingEnabled() {
		return fCollapseStackFrames.isChecked() || fColorizeStackFrames.isChecked();
	}

	/**
	 * Enables or disables the widgets on the page, with the
	 * exception of <code>fUseStepFiltersButton</code> according
	 * to the passed boolean
	 * @param enabled the new enablement status of the page's widgets
	 * @since 3.2
	 */
	protected void setPageEnablement(boolean enabled) {
		fPlatformStackFilterTable.setEnabled(enabled && fEnablePlatformButton.isChecked());
		fCustomStackFilterTable.setEnabled(enabled && fEnableCustomButton.isChecked());
		for (var categoryButton : categoryButtons) {
			categoryButton.setEnabled(enabled);
		}
		fAppearanceList.getControl().setEnabled(enabled);
		fFgColorSelector.getButton().setEnabled(enabled);
		fBgColorSelector.getButton().setEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		fPlatformStackFilterTable.performOk(store);
		fCustomStackFilterTable.performOk(store);
		fColorizeStackFrames.performOk(store);
		for (var categoryButton : categoryButtons) {
			categoryButton.performOk(categorizer);
		}
		for (var color : colors) {
			if (color.fgColor != null) {
				stackFramePresentationProvider.setForegroundColor(color.category, color.fgColor.getRGB());
			}
			if (color.bgColor != null) {
				stackFramePresentationProvider.setBackgroundColor(color.category, color.bgColor.getRGB());
			}
		}
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		var store = getPreferenceStore();
		boolean enabled = fColorizeStackFrames.performDefault(store) || fCollapseStackFrames.performDefault(store);

		for (var categoryButton : categoryButtons) {
			categoryButton.performDefault(categorizer);
		}
		for (var color : colors) {
			color.setToDefault();
		}
		fAppearanceList.update(colors.toArray(), null);
		fAppearanceList.setSelection(new StructuredSelection(colors.get(0)));

		setPageEnablement(enabled);

		fPlatformStackFilterTable.performDefaults();
		fCustomStackFilterTable.performDefaults();
		super.performDefaults();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		var propertyName = event.getProperty();
		if (StackFramePresentationProvider.isColorName(propertyName)) {
			fAppearanceList.update(colors.toArray(), null);
			fAppearanceList.setSelection(new StructuredSelection(colors.get(0)));
		}
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().removeListener(this);
		super.dispose();
	}
}
