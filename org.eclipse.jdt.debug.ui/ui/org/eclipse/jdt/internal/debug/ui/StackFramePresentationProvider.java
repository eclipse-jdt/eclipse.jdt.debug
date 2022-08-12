/*******************************************************************************
 * Copyright (c) 2021 Zsombor Gegesy.
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

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

/**
 * Provides foreground and background colors for stack frames in a debug view. After usage, it needs to be closed, so it could unregister itself from
 * preference storage.
 *
 */
public final class StackFramePresentationProvider implements IPropertyChangeListener {

	private final static EnumMap<Category, String> fgKeyMap = new EnumMap<>(Map.of(
			Category.CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_FG_COLOR,
			Category.SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_FG_COLOR,
			Category.PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_FG_COLOR,
			Category.TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_FG_COLOR,
			Category.PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_FG_COLOR,
			Category.LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_FG_COLOR
	));

	private final static EnumMap<Category, String> bgKeyMap = new EnumMap<>(Map.of(
			Category.CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_BG_COLOR, //
			Category.SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_BG_COLOR,
			Category.PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_BG_COLOR,
			Category.TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_BG_COLOR,
			Category.PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_BG_COLOR,
			Category.LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_BG_COLOR
	));

	private final static Map<String, Category> prefConst = Map.of(
			IJDIPreferencesConstants.PREF_COLORIZE_CUSTOM_METHODS, Category.CUSTOM_FILTERED,
			IJDIPreferencesConstants.PREF_COLORIZE_SYNTHETIC_METHODS, Category.SYNTHETIC,
			IJDIPreferencesConstants.PREF_COLORIZE_PLATFORM_METHODS, Category.PLATFORM,
			IJDIPreferencesConstants.PREF_COLORIZE_TEST_METHODS, Category.TEST, //
			IJDIPreferencesConstants.PREF_COLORIZE_PRODUCTION_METHODS, Category.PRODUCTION,
			IJDIPreferencesConstants.PREF_COLORIZE_LIBRARY_METHODS, Category.LIBRARY
		);

	static class Filters {
		private final String[] filters;

		Filters(String[] filters) {
			this.filters = filters;
		}

		public boolean match(String fqcName) {
			for (String filter : filters) {
				if (filter.endsWith("*")) { //$NON-NLS-1$
					if (fqcName.startsWith(filter.substring(0, filter.length() - 1))) {
						return true;
					}
				} else {
					if (filter.equals(fqcName)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private Filters platform;
	private Filters custom;
	private final IPreferenceStore store;
	private final EnumMap<Category, Boolean> enabledCategories;
	private boolean colorizeStackFrames;
	private boolean collapseStackFrames;

	public StackFramePresentationProvider(IPreferenceStore store) {
		this.store = store;
		platform = getActivePlatformFilters(store);
		custom = getActiveCustomFilters(store);
		store.addPropertyChangeListener(this);
		enabledCategories = new EnumMap<>(Category.class);
		for (var kv : prefConst.entrySet()) {
			boolean enabled = store.getBoolean(kv.getKey());
			enabledCategories.put(kv.getValue(), enabled);
		}
		colorizeStackFrames = store.getBoolean(IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES);
		collapseStackFrames = store.getBoolean(IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES);
	}

	private Filters getActivePlatformFilters(IPreferenceStore store) {
		return new Filters(JavaStackFramePreferencePage.PLATFORM_STACK_FRAMES.getActiveList(store));
	}

	private Filters getActiveCustomFilters(IPreferenceStore store) {
		return new Filters(JavaStackFramePreferencePage.CUSTOM_STACK_FRAMES.getActiveList(store));
	}

	public StackFramePresentationProvider() {
		this(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @return the foreground color of the stack frame's category, or null, if not defined.
	 */
	public Color getForeground(IJavaStackFrame frame) {
		Category cat = getCategory(frame);
		if (cat == null) {
			return null;
		}
		return getForegroundColor(cat);
	}

	/**
	 * @return the foreground color of the category, or null, if it's not defined.
	 */
	Color getForegroundColor(Category cat) {
		var key = fgKeyMap.get(cat);
		if (key != null) {
			return getColorRegistry().get(key);
		}
		return null;
	}

	private ColorRegistry getColorRegistry() {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
	}

	void setForegroundColor(Category cat, RGB color) {
		var key = fgKeyMap.get(cat);
		if (key != null) {
			getColorRegistry().put(key, color);
		}
	}

	void setBackgroundColor(Category cat, RGB color) {
		var key = bgKeyMap.get(cat);
		if (key != null) {
			getColorRegistry().put(key, color);
		}
	}

	/**
	 * @return the category specific image for the stack frame, or null, if there is no one defined.
	 */
	public ImageDescriptor getStackFrameImage(IJavaStackFrame frame) {
		var category = getCategory(frame);
		if (category != null) {
			switch (category) {
				case LIBRARY:
					return JavaPluginImages.DESC_OBJS_JAR;
				case TEST:
					return JavaPluginImages.DESC_OBJS_TEST_ATTRIB;
				case PRODUCTION:
					return JavaPluginImages.DESC_OBJS_CFILE;
				default:
					break;
			}
		}
		return null;
	}

	/**
	 * @return the background color assigned to the stack frame's category, or null, if there is no category matching.
	 */
	public Color getBackground(IJavaStackFrame frame) {
		Category cat = getCategory(frame);
		if (cat == null) {
			return null;
		}
		return getBackgroundColor(cat);
	}

	Color getBackgroundColor(Category cat) {
		var key = bgKeyMap.get(cat);
		if (key != null) {
			return getColorRegistry().get(key);
		}
		return null;
	}

	/**
	 * @return the {@link IJavaStackFrame.Category} which matches the rules.
	 */
	public IJavaStackFrame.Category getCategory(IJavaStackFrame frame) {
		if (frame.getCategory() != null) {
			return frame.getCategory();
		}
		IJavaStackFrame.Category result = Category.UNKNOWN;
		try {
			result = categorize(frame);
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		frame.setCategory(result);
		return result;
	}

	private boolean isEnabled(Category category) {
		return Boolean.TRUE.equals(enabledCategories.get(category));
	}

	/**
	 * Visible for testing!
	 */
	public IJavaStackFrame.Category categorize(IJavaStackFrame frame) throws DebugException {
		var refTypeName = frame.getReferenceType().getName();
		if (isEnabled(Category.CUSTOM_FILTERED) && custom.match(refTypeName)) {
			return Category.CUSTOM_FILTERED;
		}
		if (isEnabled(Category.SYNTHETIC) && frame.isSynthetic()) {
			return Category.SYNTHETIC;
		}
		if (isEnabled(Category.PLATFORM) && platform.match(refTypeName)) {
			return Category.PLATFORM;
		}

		return categorizeSourceElement(frame);
	}

	private Category categorizeSourceElement(IJavaStackFrame frame) {
		var sourceLocator = frame.getLaunch().getSourceLocator();
		if (sourceLocator == null) {
			return Category.UNKNOWN;
		}
		var source = sourceLocator.getSourceElement(frame);
		if (source == null) {
			return Category.UNKNOWN;
		}
		if (source instanceof IFile file) {
			if (isEnabled(Category.TEST)) {
				var jproj = JavaCore.create(file.getProject());
				var cp = jproj.findContainingClasspathEntry(file);
				if (cp != null && cp.isTest()) {
					return Category.TEST;
				}
			}
			if (isEnabled(Category.PRODUCTION)) {
				return Category.PRODUCTION;
			}
		} else if (source instanceof IClassFile && isEnabled(Category.LIBRARY)) {
			return Category.LIBRARY;
		}
		return Category.UNKNOWN;
	}

	public void close() {
		store.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getProperty();
		if (IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST.equals(prop)) {
			platform = getActivePlatformFilters(store);
		} else if (IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST.equals(prop)) {
			custom = getActiveCustomFilters(store);
		} else if (IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES.equals(prop)) {
			colorizeStackFrames = (Boolean) event.getNewValue();
		} else if (IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES.equals(prop)) {
			collapseStackFrames = (Boolean) event.getNewValue();
		} else {
			var category = prefConst.get(prop);
			if (category != null) {
				enabledCategories.put(category, (Boolean) event.getNewValue());
			}
		}
	}

	public boolean isColorizeStackFrames() {
		return colorizeStackFrames;
	}

	public boolean isCollapseStackFrames() {
		return collapseStackFrames;
	}

	static boolean isColorName(String propertyName) {
		return fgKeyMap.containsValue(propertyName) || bgKeyMap.containsValue(propertyName);
	}
}
