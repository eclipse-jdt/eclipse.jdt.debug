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

import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public class JavaStackFramePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

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

	public static final String PAGE_ID = "org.eclipse.jdt.debug.ui.JavaStackFramePreferencePage"; //$NON-NLS-1$

	//widgets
	private Button fColorizeStackFramesButton;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STACK_FRAMES_PREFERENCE_PAGE);
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
		fColorizeStackFramesButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStackFramesPreferencePage__Color_stack_frames, null, isColorizeStackFrames(), 2);
		fColorizeStackFramesButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setPageEnablement(fColorizeStackFramesButton.getSelection());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			}
		);
		initializeDialogUnits(container);

		SWTFactory.createLabel(container, DebugUIMessages.JavaStackFramesPreferencePage_Defined_stack_frame_filters_for_platform, 2);
		fPlatformStackFilterTable = new JavaFilterTable(this, FilterManager.PLATFORM_STACK_FRAMES, null);
		fPlatformStackFilterTable.createTable(container);

		SWTFactory.createLabel(container, DebugUIMessages.JavaStackFramesPreferencePage_Defined_custom_stack_frame_filters, 2);
		fCustomStackFilterTable = new JavaFilterTable(this, FilterManager.CUSTOM_STACK_FRAMES, null);
		fCustomStackFilterTable.createTable(container);

		createAppearanceList(container);

		setPageEnablement(fColorizeStackFramesButton.getSelection());
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
		gd.heightHint = convertHeightInCharsToPixels(6);
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
			new CategoryColors(IJavaStackFrame.Category.PLATFORM, DebugUIMessages.JavaStackFramesPreferencePage_category_platform),
			new CategoryColors(IJavaStackFrame.Category.SYNTHETIC, DebugUIMessages.JavaStackFramesPreferencePage_category_synthetic),
			new CategoryColors(IJavaStackFrame.Category.LIBRARY, DebugUIMessages.JavaStackFramesPreferencePage_category_library),
			new CategoryColors(IJavaStackFrame.Category.TEST, DebugUIMessages.JavaStackFramesPreferencePage_category_test),
			new CategoryColors(IJavaStackFrame.Category.PRODUCTION, DebugUIMessages.JavaStackFramesPreferencePage_category_production),
			new CategoryColors(IJavaStackFrame.Category.CUSTOM_FILTERED, DebugUIMessages.JavaStackFramesPreferencePage_category_custom_filter)
		);
		fAppearanceList.setInput(colors);
		fAppearanceList.setSelection(new StructuredSelection(colors.get(0)));
	}

	/**
	 * Enables or disables the widgets on the page, with the
	 * exception of <code>fUseStepFiltersButton</code> according
	 * to the passed boolean
	 * @param enabled the new enablement status of the page's widgets
	 * @since 3.2
	 */
	protected void setPageEnablement(boolean enabled) {
		fPlatformStackFilterTable.setPageEnablement(enabled);
		fCustomStackFilterTable.setPageEnablement(enabled);
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
		store.setValue(IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES, fColorizeStackFramesButton.getSelection());
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
		boolean enabled = isColorizeStackFrames();
		fColorizeStackFramesButton.setSelection(enabled);
		setPageEnablement(enabled);

		fPlatformStackFilterTable.performDefaults();
		fCustomStackFilterTable.performDefaults();
		super.performDefaults();
	}

	private boolean isColorizeStackFrames() {
		return getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES);
	}
}
