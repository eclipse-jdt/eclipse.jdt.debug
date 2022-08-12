/*******************************************************************************
 * Copyright (c) 2022 Zsombor Gegesy and others.
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
import org.eclipse.jdt.internal.ui.filtertable.FilterManager;
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

	private static class PreferenceButton {
		private final Button button;
		private final String flag;

		PreferenceButton(Composite container, String label, IPreferenceStore store, String flag) {
			this.button = SWTFactory.createCheckButton(container, label, null, store.getBoolean(flag), 2);
			this.flag = flag;
		}

		boolean performDefault(IPreferenceStore store) {
			var defValue = store.getBoolean(flag);
			button.setSelection(defValue);
			return defValue;
		}

		void performOk(IPreferenceStore store) {
			store.setValue(flag, button.getSelection());
		}

		boolean isSelected() {
			return button.getSelection();
		}

		void setEnabled(boolean enabled) {
			this.button.setEnabled(enabled);
		}

		PreferenceButton widgetSelected(Consumer<Boolean> consumer) {
			this.button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					consumer.accept(button.getSelection());
				}
			});
			return this;
		}
	}

	public static final String PAGE_ID = "org.eclipse.jdt.debug.ui.JavaStackFramePreferencePage"; //$NON-NLS-1$
	final static FilterManager PLATFORM_STACK_FRAMES = new FilterManager(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, IJDIPreferencesConstants.PREF_INACTIVE_PLATFORM_FRAME_FILTER_LIST);
	final static FilterManager CUSTOM_STACK_FRAMES = new FilterManager(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, IJDIPreferencesConstants.PREF_INACTIVE_CUSTOM_FRAME_FILTER_LIST);

	//widgets
	private PreferenceButton fColorizeStackFrames;
	private PreferenceButton fCollapseStackFrames;
	private List<PreferenceButton> categoryButtons;
	private PreferenceButton fEnablePlatformButton;
	private PreferenceButton fEnableCustomButton;
	private JavaFilterTable fPlatformStackFilterTable;
	private JavaFilterTable fCustomStackFilterTable;
	private TableViewer fAppearanceList;
	private List<CategoryColors> colors;
	private ColorSelector fFgColorSelector;
	private ColorSelector fBgColorSelector;
	private StackFramePresentationProvider stackFramePresentationProvider;

	/**
	 * Constructor
	 */
	public JavaStackFramePreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setTitle(DebugUIMessages.JavaStackFramesPreferencePage_title);
		setDescription(DebugUIMessages.JavaStackFramesPreferencePage_description);
		stackFramePresentationProvider = new StackFramePresentationProvider(getPreferenceStore());
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
	 * Create a group to contain the step filter related widgetry
	 */
	private void createStepFilterPreferences(Composite parent) {
		var store = getPreferenceStore();
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
		fColorizeStackFrames = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage__Color_stack_frames, store, IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES);
		fColorizeStackFrames.widgetSelected(this::updateCheckboxes);

		fCollapseStackFrames = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage__Collapse_stack_frames, store, IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES);
		fCollapseStackFrames.widgetSelected(this::updateCheckboxes);
		initializeDialogUnits(container);

		this.fEnableCustomButton = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_custom, store, IJDIPreferencesConstants.PREF_COLORIZE_CUSTOM_METHODS) //
				.widgetSelected(selected -> fCustomStackFilterTable.setEnabled(selected && isCategoryHandlingEnabled()));
		categoryButtons.add(this.fEnableCustomButton);

		fCustomStackFilterTable = new JavaFilterTable(this, CUSTOM_STACK_FRAMES,
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

		categoryButtons.add(new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_synthetic, store, IJDIPreferencesConstants.PREF_COLORIZE_SYNTHETIC_METHODS));

		this.fEnablePlatformButton = new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_platform, store, IJDIPreferencesConstants.PREF_COLORIZE_PLATFORM_METHODS) //
				.widgetSelected(selected -> fPlatformStackFilterTable.setEnabled(selected && isCategoryHandlingEnabled()));
		categoryButtons.add(fEnablePlatformButton);

		fPlatformStackFilterTable = new JavaFilterTable(this, PLATFORM_STACK_FRAMES,
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

		categoryButtons.add(new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_test, store, IJDIPreferencesConstants.PREF_COLORIZE_TEST_METHODS));
		categoryButtons.add(new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_production, store, IJDIPreferencesConstants.PREF_COLORIZE_PRODUCTION_METHODS));
		categoryButtons.add(new PreferenceButton(container, DebugUIMessages.JavaStackFramesPreferencePage_Filter_library, store, IJDIPreferencesConstants.PREF_COLORIZE_LIBRARY_METHODS));

		createAppearanceList(container);

		setPageEnablement(isCategoryHandlingEnabled());
		initList();
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
				new CategoryColors(IJavaStackFrame.Category.CUSTOM_FILTERED, DebugUIMessages.JavaStackFramesPreferencePage_category_custom_filter), //
				new CategoryColors(IJavaStackFrame.Category.SYNTHETIC, DebugUIMessages.JavaStackFramesPreferencePage_category_synthetic), //
				new CategoryColors(IJavaStackFrame.Category.PLATFORM, DebugUIMessages.JavaStackFramesPreferencePage_category_platform), //
				new CategoryColors(IJavaStackFrame.Category.TEST, DebugUIMessages.JavaStackFramesPreferencePage_category_test), //
				new CategoryColors(IJavaStackFrame.Category.PRODUCTION, DebugUIMessages.JavaStackFramesPreferencePage_category_production), //
				new CategoryColors(IJavaStackFrame.Category.LIBRARY, DebugUIMessages.JavaStackFramesPreferencePage_category_library) //
		);
		fAppearanceList.setInput(colors);
		fAppearanceList.setSelection(new StructuredSelection(colors.get(0)));
	}

	protected void updateCheckboxes(@SuppressWarnings("unused") boolean flag) {
		setPageEnablement(isCategoryHandlingEnabled());
	}

	private boolean isCategoryHandlingEnabled() {
		return fCollapseStackFrames.isSelected() || fColorizeStackFrames.isSelected();
	}

	/**
	 * Enables or disables the widgets on the page, with the
	 * exception of <code>fUseStepFiltersButton</code> according
	 * to the passed boolean
	 * @param enabled the new enablement status of the page's widgets
	 * @since 3.2
	 */
	protected void setPageEnablement(boolean enabled) {
		fPlatformStackFilterTable.setEnabled(enabled && fEnablePlatformButton.isSelected());
		fCustomStackFilterTable.setEnabled(enabled && fEnableCustomButton.isSelected());
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
			categoryButton.performOk(store);
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
			categoryButton.performDefault(store);
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
