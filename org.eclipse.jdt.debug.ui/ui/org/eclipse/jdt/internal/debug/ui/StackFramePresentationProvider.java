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

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.launching.sourcelookup.advanced.AdvancedSourceLookup;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
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
public class StackFramePresentationProvider implements IPropertyChangeListener, AutoCloseable {

	private final static Map<Category, String> fgKeyMap = Map.of(
			Category.TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_FG_COLOR,
			Category.LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_FG_COLOR,
			Category.SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_FG_COLOR,
			Category.PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_FG_COLOR,
			Category.PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_FG_COLOR,
			Category.CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_FG_COLOR
		);

	private final static Map<Category, String> bgKeyMap = Map.of(
			Category.PLATFORM, IJDIPreferencesConstants.PREF_PLATFORM_STACK_FRAME_BG_COLOR,
			Category.LIBRARY, IJDIPreferencesConstants.PREF_LIB_STACK_FRAME_BG_COLOR,
			Category.PRODUCTION, IJDIPreferencesConstants.PREF_PRODUCTION_STACK_FRAME_BG_COLOR,
			Category.SYNTHETIC, IJDIPreferencesConstants.PREF_SYNT_STACK_FRAME_BG_COLOR,
			Category.TEST, IJDIPreferencesConstants.PREF_TEST_STACK_FRAME_BG_COLOR,
			Category.CUSTOM_FILTERED, IJDIPreferencesConstants.PREF_CUSTOM_FILTERED_STACK_FRAME_BG_COLOR
		);

	class Filters {
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

	public StackFramePresentationProvider(IPreferenceStore store) {
		this.store = store;
		platform = new Filters(FilterManager.PLATFORM_STACK_FRAMES.getActiveList(store));
		custom = new Filters(FilterManager.CUSTOM_STACK_FRAMES.getActiveList(store));
		store.addPropertyChangeListener(this);
	}

	public StackFramePresentationProvider() {
		this(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	public Color getForeground(IJavaStackFrame frame) {
		Category cat = getCategory(frame);
		if (cat == null) {
			return null;
		}
		return getForegroundColor(cat);
	}

	public Color getForegroundColor(Category cat) {
		ColorRegistry colorRegistry = getColorRegistry();
		var key = fgKeyMap.get(cat);
		if (key != null) {
			return colorRegistry.get(key);
		}
		return null;
	}

	private ColorRegistry getColorRegistry() {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
	}

	public void setForegroundColor(Category cat, RGB color) {
		var key = fgKeyMap.get(cat);
		if (key != null) {
			getColorRegistry().put(key, color);
		}
	}

	public void setBackgroundColor(Category cat, RGB color) {
		var key = bgKeyMap.get(cat);
		if (key != null) {
			getColorRegistry().put(key, color);
		}
	}

	public Color getBackground(IJavaStackFrame frame) {
		Category cat = getCategory(frame);
		if (cat == null) {
			return null;
		}
		return getBackgroundColor(cat);
	}

	public Color getBackgroundColor(Category cat) {
		ColorRegistry colorRegistry = getColorRegistry();
		var key = bgKeyMap.get(cat);
		if (key != null) {
			return colorRegistry.get(key);
		}
		return null;
	}

	private IJavaStackFrame.Category getCategory(IJavaStackFrame frame) {
		if (frame.getCategory() != null) {
			return frame.getCategory();
		}
		IJavaStackFrame.Category result = Category.UNKNOWN;
		try {
			result = categorize(frame.getReferenceType(), frame.isSynthetic());
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		frame.setCategory(result);
		return result;
	}

	/**
	 * Visible for testing!
	 */
	public IJavaStackFrame.Category categorize(IJavaReferenceType refType, boolean synthetic) throws DebugException {
		if (custom.match(refType.getName())) {
			return IJavaStackFrame.Category.CUSTOM_FILTERED;
		}
		if (synthetic) {
			return IJavaStackFrame.Category.SYNTHETIC;
		}
		if (platform.match(refType.getName())) {
			return IJavaStackFrame.Category.PLATFORM;
		}

		File location = AdvancedSourceLookup.getClassesLocation(refType);
		if (location != null) {
			// either a directory, or a jar file
			if (location.isDirectory()) {
				return categorizeDirectory(location);
			}
			if (location.getName().toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
				return IJavaStackFrame.Category.LIBRARY;
			}
		}

		return IJavaStackFrame.Category.UNKNOWN;
	}

	@SuppressWarnings("restriction")
	private Category categorizeDirectory(File location) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (IJavaProject project : JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects()) {
				for (IClasspathEntry classPathEntry : project.getRawClasspath()) {
					if (classPathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IPath outputLocation = classPathEntry.getOutputLocation();
						if (outputLocation == null) {
							outputLocation = project.getOutputLocation();
						}
						if (outputLocation != null) {
							if (isMatching(root, outputLocation, location)) {
								if (Boolean.parseBoolean(ClasspathEntry.getExtraAttribute(classPathEntry, "test"))) { //$NON-NLS-1$
									return Category.TEST;
								}
								return Category.PRODUCTION;
							}
						}
					}
				}
			}
			return Category.PRODUCTION;
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
			return Category.UNKNOWN;
		}
	}

	private boolean isMatching(IWorkspaceRoot root, IPath workspacePath, File classLocation) {
		IResource resource = root.findMember(workspacePath);
		if (resource == null) {
			return false;
		}
		IPath location = resource.getLocation();
		if (location == null) {
			return false;
		}
		return classLocation.equals(location.toFile());
	}

	@Override
	public void close() {
		store.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getProperty();
		if (IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST.equals(prop)) {
			platform = new Filters(FilterManager.PLATFORM_STACK_FRAMES.getActiveList(store));
		} else if (IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST.equals(prop)) {
			custom = new Filters(FilterManager.CUSTOM_STACK_FRAMES.getActiveList(store));
		}
	}
}
