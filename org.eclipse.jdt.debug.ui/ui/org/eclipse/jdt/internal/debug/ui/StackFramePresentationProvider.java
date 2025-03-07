/*******************************************************************************
 * Copyright (c) 2021, 2025 Zsombor Gegesy and others.
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

import java.util.Map;
import java.util.Set;

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

	private static final Set<String> JAR_ICON = Set.of(StackFrameCategorizer.CATEGORY_LIBRARY.name(), StackFrameCategorizer.CATEGORY_SYNTHETIC.name(), StackFrameCategorizer.CATEGORY_PLATFORM.name());

	private final static Map<Category, String> fgKeyMap = Map.of(StackFrameCategorizer.CATEGORY_CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_FG_COLOR, //
			StackFrameCategorizer.CATEGORY_SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_FG_COLOR, //
			StackFrameCategorizer.CATEGORY_PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_FG_COLOR, //
			StackFrameCategorizer.CATEGORY_TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_FG_COLOR, //
			StackFrameCategorizer.CATEGORY_PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_FG_COLOR, //
			StackFrameCategorizer.CATEGORY_LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_FG_COLOR);

	private final static Map<Category, String> bgKeyMap = Map.of(StackFrameCategorizer.CATEGORY_CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_BG_COLOR, //
			StackFrameCategorizer.CATEGORY_SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_BG_COLOR, //
			StackFrameCategorizer.CATEGORY_PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_BG_COLOR, //
			StackFrameCategorizer.CATEGORY_TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_BG_COLOR, //
			StackFrameCategorizer.CATEGORY_PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_BG_COLOR, //
			StackFrameCategorizer.CATEGORY_LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_BG_COLOR);

	private final IPreferenceStore store;
	private boolean collapseStackFrames;
	private boolean colorizeStackFrames;

	public StackFramePresentationProvider(IPreferenceStore store) {
		this.store = store;
		store.addPropertyChangeListener(this);
		colorizeStackFrames = store.getBoolean(IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES);
		collapseStackFrames = store.getBoolean(IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES);
	}

	public StackFramePresentationProvider() {
		this(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @return the foreground color of the stack frame's category, or null, if not defined.
	 */
	public Color getForeground(IJavaStackFrame frame) {
		Category cat = frame.getCategory();
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

	/**
	 * Sets the foreground color for the given {@link IJavaStackFrame.Category}.
	 */
	void setForegroundColor(Category cat, RGB color) {
		var key = fgKeyMap.get(cat);
		if (key != null) {
			getColorRegistry().put(key, color);
		}
	}

	/**
	 * Sets the background color for the given {@link IJavaStackFrame.Category}.
	 */
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
		if (collapseStackFrames || colorizeStackFrames) {
			var category = frame.getCategory();
			if (category != null) {
				if (JAR_ICON.contains(category.name())) {
					return JavaPluginImages.DESC_OBJS_JAR;
				}
			}
		}
		return null;
	}

	/**
	 * @return the background color assigned to the stack frame's category, or null, if there is no category matching.
	 */
	public Color getBackground(IJavaStackFrame frame) {
		Category cat = frame.getCategory();
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
	 * Unsubscribes from the {@link IPreferenceStore} to not receive notifications.
	 */
	public void close() {
		store.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getProperty();
		if (IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES.equals(prop)) {
			collapseStackFrames = (Boolean) event.getNewValue();
		} else if (IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES.equals(prop)) {
			colorizeStackFrames = (Boolean) event.getNewValue();
		}
	}

	/**
	 * @return if stack frames should be collapsed.
	 */
	public boolean isCollapseStackFrames() {
		return collapseStackFrames;
	}

	/**
	 * @return if stack frames should be colored differently based on their category.
	 */
	public boolean isColorizeStackFrames() {
		return colorizeStackFrames;
	}

	static boolean isColorName(String propertyName) {
		return fgKeyMap.containsValue(propertyName) || bgKeyMap.containsValue(propertyName);
	}
}
